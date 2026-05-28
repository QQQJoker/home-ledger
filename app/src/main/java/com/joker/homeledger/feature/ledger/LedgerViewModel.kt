package com.joker.homeledger.feature.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joker.homeledger.core.common.DateRangeFilter
import com.joker.homeledger.core.common.DateLabelUtils
import com.joker.homeledger.core.data.repository.AccountRepository
import com.joker.homeledger.core.data.repository.CategoryRepository
import com.joker.homeledger.core.data.repository.TransactionRepository
import com.joker.homeledger.core.database.entity.AccountEntity
import com.joker.homeledger.core.database.entity.CategoryEntity
import com.joker.homeledger.core.database.model.TransactionListItem
import com.joker.homeledger.core.model.LedgerFilter
import com.joker.homeledger.core.model.TransactionType
import com.joker.homeledger.core.model.TypeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class LedgerSection(
    val title: String,
    val items: List<TransactionListItem>
)

data class LedgerUiState(
    val filter: LedgerFilter = LedgerFilter(),
    val sections: List<LedgerSection> = emptyList(),
    val expenseCategories: List<CategoryEntity> = emptyList(),
    val incomeCategories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val showFilterPanel: Boolean = false
)

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository
) : ViewModel() {
    private val filterState = MutableStateFlow(LedgerFilter())
    private val showFilterPanel = MutableStateFlow(false)

    private val categoriesFlow = combine(
        categoryRepository.observeAllByType(TransactionType.EXPENSE.name),
        categoryRepository.observeAllByType(TransactionType.INCOME.name)
    ) { expense, income -> expense to income }

    private val transactionsFlow = filterState.flatMapLatest { filter ->
        transactionRepository.observeWithDetails(filter)
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        filterState,
        showFilterPanel,
        transactionsFlow,
        categoriesFlow,
        accountRepository.observeAll()
    ) { filter, showPanel, transactions, (expenseCats, incomeCats), accounts ->
        val grouped = transactions.groupBy { DateLabelUtils.dayGroupLabel(it.transaction.occurredAt) }
            .map { (title, items) -> LedgerSection(title = title, items = items) }
        LedgerUiState(
            filter = filter,
            sections = grouped,
            expenseCategories = expenseCats,
            incomeCategories = incomeCats,
            accounts = accounts,
            showFilterPanel = showPanel
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LedgerUiState()
    )

    fun toggleFilterPanel() {
        showFilterPanel.update { !it }
    }

    fun updateDateRange(dateRange: DateRangeFilter) {
        filterState.update { it.copy(dateRange = dateRange) }
    }

    fun updateTypeFilter(typeFilter: TypeFilter) {
        filterState.update { it.copy(typeFilter = typeFilter) }
    }

    fun updateCategoryFilter(categoryId: Long?) {
        filterState.update { it.copy(categoryId = categoryId) }
    }

    fun updateAccountFilter(accountId: Long?) {
        filterState.update { it.copy(accountId = accountId) }
    }

    fun clearFilters() {
        filterState.value = LedgerFilter()
    }
}
