package com.joker.homeledger.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joker.homeledger.core.common.StatsPeriodFilter
import com.joker.homeledger.core.common.toLedgerFilter
import com.joker.homeledger.core.data.repository.TransactionRepository
import com.joker.homeledger.core.model.CategoryExpenseItem
import com.joker.homeledger.core.model.Summary
import com.joker.homeledger.core.model.TrendPoint
import com.joker.homeledger.core.navigation.LedgerFilterCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val period: StatsPeriodFilter = StatsPeriodFilter(),
    val summary: Summary = Summary(0, 0),
    val categoryRank: List<CategoryExpenseItem> = emptyList(),
    val trend: List<TrendPoint> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val ledgerFilterCoordinator: LedgerFilterCoordinator
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun updatePeriod(period: StatsPeriodFilter) {
        _uiState.update { it.copy(period = period) }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val period = _uiState.value.period
            val summary = transactionRepository.getSummary(period)
            val categoryRank = transactionRepository.getCategoryExpenseRank(period)
            val trend = transactionRepository.getTrendForStats(period)
            _uiState.update {
                it.copy(
                    summary = summary,
                    categoryRank = categoryRank,
                    trend = trend,
                    isLoading = false
                )
            }
        }
    }

    fun prepareLedgerDrillDown(categoryId: Long) {
        ledgerFilterCoordinator.setPending(_uiState.value.period.toLedgerFilter(categoryId))
    }
}
