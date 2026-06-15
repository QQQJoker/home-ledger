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
import com.joker.homeledger.core.navigation.LedgerFilterCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LedgerSection(
    val title: String,
    val dailyExpenseCent: Long,
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
    private val ledgerFilterCoordinator: LedgerFilterCoordinator,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository
) : ViewModel() {
    private val filterState = MutableStateFlow(LedgerFilter())
    private val showFilterPanel = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            ledgerFilterCoordinator.pendingFilter.collect { pending ->
                if (pending != null) {
                    applyFilter(pending)
                }
            }
        }
    }

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
        val zone = ZoneId.systemDefault()
        val grouped = transactions
            .groupBy { Instant.ofEpochMilli(it.transaction.occurredAt).atZone(zone).toLocalDate() }
            .entries
            .sortedByDescending { it.key }
            .map { (_, items) ->
                val sortedItems = items.sortedByDescending { it.transaction.occurredAt }
                val dailyExpenseCent = sortedItems
                    .filter { it.transaction.type == TransactionType.EXPENSE.name }
                    .sumOf { it.transaction.amountCent }
                LedgerSection(
                    title = DateLabelUtils.dayGroupLabel(sortedItems.first().transaction.occurredAt),
                    dailyExpenseCent = dailyExpenseCent,
                    items = sortedItems
                )
            }
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

    fun applyFilter(filter: LedgerFilter, showPanel: Boolean = true) {
        filterState.value = filter
        showFilterPanel.value = showPanel
        ledgerFilterCoordinator.clearPending()
    }
}
