package com.joker.homeledger.core.common

import com.joker.homeledger.core.model.LedgerFilter
import com.joker.homeledger.core.model.TypeFilter

fun StatsPeriodFilter.toLedgerDateRangeFilter(): DateRangeFilter {
    val range = resolveRange()
    return DateRangeFilter(
        preset = PeriodPreset.ALL,
        useCustomRange = true,
        customStartAt = range.startAt,
        customEndAt = range.endAt
    )
}

fun StatsPeriodFilter.toLedgerFilter(categoryId: Long): LedgerFilter {
    return LedgerFilter(
        dateRange = toLedgerDateRangeFilter(),
        typeFilter = TypeFilter.EXPENSE,
        categoryId = categoryId
    )
}
