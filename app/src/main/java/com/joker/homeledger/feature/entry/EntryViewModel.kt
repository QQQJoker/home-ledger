package com.joker.homeledger.feature.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joker.homeledger.core.common.MoneyFormatter
import com.joker.homeledger.core.data.preferences.UserPreferences
import com.joker.homeledger.core.data.repository.AccountRepository
import com.joker.homeledger.core.data.repository.CategoryRepository
import com.joker.homeledger.core.data.repository.TransactionRepository
import com.joker.homeledger.core.database.entity.AccountEntity
import com.joker.homeledger.core.database.entity.TransactionEntity
import com.joker.homeledger.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntryUiState(
    val isEditMode: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountInput: String = "",
    val noteInput: String = "",
    val categoryOptions: List<Pair<Long, String>> = emptyList(),
    val accountOptions: List<Pair<Long, String>> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedAccountId: Long? = null,
    val occurredAtMillis: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val shouldClose: Boolean = false
)

@HiltViewModel
class EntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val editingId: Long = savedStateHandle.get<Long>("transactionId") ?: -1L
    private var editingEntity: TransactionEntity? = null

    private val localState = MutableStateFlow(
        EntryUiState(
            isEditMode = editingId > 0,
            selectedAccountId = userPreferences.getLastAccountId()
        )
    )

    private val categoriesFlow = localState.map { it.type }.flatMapLatest { type ->
        categoryRepository.observeVisibleByType(type.name)
    }

    val uiState: StateFlow<EntryUiState> = combine(
        localState,
        categoriesFlow,
        accountRepository.observeEnabled()
    ) { state, categories, accounts ->
        val categoryOptions = categories.map { it.id to it.name }
        val accountOptions = accounts.map { it.toOptionLabel() }
        val lastAccount = userPreferences.getLastAccountId()
        state.copy(
            categoryOptions = categoryOptions,
            accountOptions = accountOptions,
            selectedCategoryId = state.selectedCategoryId ?: categoryOptions.firstOrNull()?.first,
            selectedAccountId = state.selectedAccountId ?: lastAccount ?: accountOptions.firstOrNull()?.first
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EntryUiState(isEditMode = editingId > 0)
    )

    init {
        if (editingId > 0) {
            viewModelScope.launch {
                val entity = transactionRepository.getById(editingId) ?: return@launch
                editingEntity = entity
                localState.update {
                    it.copy(
                        isEditMode = true,
                        type = TransactionType.valueOf(entity.type),
                        amountInput = MoneyFormatter.centToYuan(entity.amountCent).toPlainString(),
                        noteInput = entity.note.orEmpty(),
                        selectedCategoryId = entity.categoryId,
                        selectedAccountId = entity.accountId,
                        occurredAtMillis = entity.occurredAt
                    )
                }
            }
        }
    }

    fun updateType(type: TransactionType) {
        localState.update {
            it.copy(
                type = type,
                selectedCategoryId = null,
                errorMessage = null,
                saveSuccess = false,
                shouldClose = false
            )
        }
    }

    fun updateAmount(value: String) {
        if (!value.matches(Regex("^\\d{0,9}(\\.\\d{0,2})?$"))) return
        localState.update { it.copy(amountInput = value, errorMessage = null, saveSuccess = false, shouldClose = false) }
    }

    fun updateNote(value: String) {
        if (value.length > 500) return
        localState.update { it.copy(noteInput = value, errorMessage = null, saveSuccess = false, shouldClose = false) }
    }

    fun updateCategory(id: Long) {
        localState.update { it.copy(selectedCategoryId = id, errorMessage = null, saveSuccess = false, shouldClose = false) }
    }

    fun updateAccount(id: Long) {
        localState.update { it.copy(selectedAccountId = id, errorMessage = null, saveSuccess = false, shouldClose = false) }
    }

    fun updateOccurredAt(millis: Long) {
        localState.update { it.copy(occurredAtMillis = millis, errorMessage = null, saveSuccess = false, shouldClose = false) }
    }

    fun save() {
        val state = uiState.value
        val categoryId = state.selectedCategoryId
        val accountId = state.selectedAccountId
        if (state.amountInput.isBlank()) {
            localState.update { it.copy(errorMessage = "请输入金额") }
            return
        }
        if (categoryId == null) {
            localState.update { it.copy(errorMessage = "请选择分类") }
            return
        }
        if (accountId == null) {
            localState.update { it.copy(errorMessage = "请选择账户") }
            return
        }
        val amountCent = runCatching { MoneyFormatter.yuanToCent(state.amountInput) }.getOrNull()
        if (amountCent == null || amountCent <= 0L) {
            localState.update { it.copy(errorMessage = "金额格式不正确") }
            return
        }
        localState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false, shouldClose = false) }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = editingEntity
            runCatching {
                if (existing != null) {
                    transactionRepository.updateTransaction(
                        old = existing,
                        new = existing.copy(
                            type = state.type.name,
                            amountCent = amountCent,
                            categoryId = categoryId,
                            accountId = accountId,
                            occurredAt = state.occurredAtMillis,
                            note = state.noteInput.ifBlank { null },
                            updatedAt = now
                        )
                    )
                } else {
                    transactionRepository.addTransaction(
                        TransactionEntity(
                            type = state.type.name,
                            amountCent = amountCent,
                            categoryId = categoryId,
                            accountId = accountId,
                            occurredAt = state.occurredAtMillis,
                            note = state.noteInput.ifBlank { null },
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                }
            }.onSuccess {
                userPreferences.setLastAccountId(accountId)
                editingEntity = null
                localState.update {
                    it.copy(
                        amountInput = if (existing != null) it.amountInput else "",
                        noteInput = if (existing != null) it.noteInput else "",
                        isSaving = false,
                        saveSuccess = true,
                        shouldClose = existing != null,
                        errorMessage = null
                    )
                }
            }.onFailure {
                localState.update { current ->
                    current.copy(isSaving = false, errorMessage = "保存失败，请重试")
                }
            }
        }
    }

    private fun AccountEntity.toOptionLabel(): Pair<Long, String> {
        return id to "${name}（¥${MoneyFormatter.centToYuan(balanceCent)}）"
    }
}
