package com.joker.homeledger.core.navigation

import com.joker.homeledger.core.model.LedgerFilter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class LedgerFilterCoordinator @Inject constructor() {
    private val _pendingFilter = MutableStateFlow<LedgerFilter?>(null)
    val pendingFilter: StateFlow<LedgerFilter?> = _pendingFilter.asStateFlow()

    fun setPending(filter: LedgerFilter) {
        _pendingFilter.value = filter
    }

    fun clearPending() {
        _pendingFilter.value = null
    }
}
