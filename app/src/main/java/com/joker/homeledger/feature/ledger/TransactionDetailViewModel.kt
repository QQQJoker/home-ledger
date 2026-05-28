package com.joker.homeledger.feature.ledger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joker.homeledger.core.common.DateLabelUtils
import com.joker.homeledger.core.common.MoneyFormatter
import com.joker.homeledger.core.data.repository.TransactionRepository
import com.joker.homeledger.core.database.model.TransactionListItem
import com.joker.homeledger.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionDetailUiState(
    val loading: Boolean = true,
    val item: TransactionListItem? = null,
    val showDeleteDialog: Boolean = false,
    val message: String? = null,
    val deleted: Boolean = false
) {
    val typeLabel: String
        get() = if (item?.transaction?.type == TransactionType.INCOME.name) "收入" else "支出"

    val amountText: String
        get() = item?.transaction?.amountCent?.let { "¥${MoneyFormatter.centToYuan(it)}" } ?: ""

    val occurredText: String
        get() = item?.transaction?.occurredAt?.let {
            "${DateLabelUtils.dayGroupLabel(it)} ${DateLabelUtils.timeLabel(it)}"
        } ?: ""
}

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: -1L
    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val item = transactionRepository.getWithDetailsById(transactionId)
            _uiState.update { it.copy(loading = false, item = item) }
        }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            runCatching {
                val entity = transactionRepository.getById(transactionId)
                    ?: error("记录不存在")
                transactionRepository.deleteTransaction(entity)
            }.onSuccess {
                _uiState.update { it.copy(showDeleteDialog = false, deleted = true) }
            }.onFailure {
                _uiState.update { it.copy(showDeleteDialog = false, message = "删除失败") }
            }
        }
    }
}
