package com.joker.homeledger.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joker.homeledger.core.common.MoneyFormatter
import com.joker.homeledger.core.data.repository.AccountRepository
import com.joker.homeledger.core.database.entity.AccountEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountDialogState(
    val showDialog: Boolean = false,
    val editingAccount: AccountEntity? = null,
    val inputName: String = "",
    val inputBalance: String = "0"
)

data class AccountManageUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val message: String? = null,
    val dialog: AccountDialogState = AccountDialogState()
)

@HiltViewModel
class AccountManageViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val dialogState = MutableStateFlow(AccountDialogState())
    private val messageState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AccountManageUiState> = combine(
        accountRepository.observeAll(),
        dialogState,
        messageState
    ) { accounts, dialog, message ->
        AccountManageUiState(
            accounts = accounts,
            message = message,
            dialog = dialog
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountManageUiState())

    fun openAddDialog() {
        dialogState.value = AccountDialogState(showDialog = true, inputBalance = "0")
    }

    fun openEditDialog(account: AccountEntity) {
        dialogState.value = AccountDialogState(
            showDialog = true,
            editingAccount = account,
            inputName = account.name,
            inputBalance = MoneyFormatter.centToYuan(account.balanceCent).toPlainString()
        )
    }

    fun updateInputName(name: String) {
        dialogState.value = dialogState.value.copy(inputName = name)
    }

    fun updateInputBalance(balance: String) {
        if (!balance.matches(Regex("^\\d{0,9}(\\.\\d{0,2})?$"))) return
        dialogState.value = dialogState.value.copy(inputBalance = balance)
    }

    fun dismissDialog() {
        dialogState.value = AccountDialogState()
    }

    fun saveAccount() {
        val dialog = dialogState.value
        if (!dialog.showDialog) return
        val trimmed = dialog.inputName.trim()
        if (trimmed.isEmpty()) {
            messageState.value = "账户名称不能为空"
            return
        }
        val balanceCent = runCatching { MoneyFormatter.yuanToCent(dialog.inputBalance) }.getOrNull()
        if (balanceCent == null || balanceCent < 0L) {
            messageState.value = "余额格式不正确"
            return
        }
        viewModelScope.launch {
            val editing = dialog.editingAccount
            val excludeId = editing?.id ?: -1L
            if (accountRepository.isNameDuplicated(trimmed, excludeId)) {
                messageState.value = "账户名称已存在"
                return@launch
            }
            val now = System.currentTimeMillis()
            runCatching {
                if (editing == null) {
                    val maxOrder = uiState.value.accounts.maxOfOrNull { it.sortOrder } ?: 0
                    accountRepository.addAccount(
                        AccountEntity(
                            name = trimmed,
                            isPreset = false,
                            balanceCent = balanceCent,
                            sortOrder = maxOrder + 1,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                } else {
                    accountRepository.updateAccount(
                        editing.copy(
                            name = trimmed,
                            balanceCent = balanceCent,
                            updatedAt = now
                        )
                    )
                }
            }.onSuccess {
                messageState.value = "保存成功"
                dismissDialog()
            }.onFailure {
                messageState.value = "保存失败"
            }
        }
    }

    fun toggleDisabled(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.setDisabled(account, !account.isDisabled)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            if (!accountRepository.canDelete(account.id)) {
                messageState.value = "该账户下仍有流水，无法删除，可改为停用"
                return@launch
            }
            runCatching { accountRepository.deleteAccount(account.id) }
                .onSuccess { messageState.value = "已删除" }
                .onFailure { messageState.value = "删除失败" }
        }
    }
}
