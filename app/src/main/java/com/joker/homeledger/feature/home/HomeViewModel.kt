package com.joker.homeledger.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joker.homeledger.core.common.PeriodPreset
import com.joker.homeledger.core.data.repository.TransactionRepository
import com.joker.homeledger.core.database.model.TransactionListItem
import com.joker.homeledger.core.model.Summary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val summary: Summary = Summary(incomeCent = 0, expenseCent = 0),
    val latestTransactions: List<TransactionListItem> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = transactionRepository
        .observeRecentWithDetails(10)
        .flatMapLatest { latest ->
            flow {
                val summary = transactionRepository.getSummaryForPeriod(PeriodPreset.THIS_MONTH)
                emit(HomeUiState(summary = summary, latestTransactions = latest))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
}
