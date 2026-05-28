package com.joker.homeledger.core.model

import com.joker.homeledger.core.common.DateRangeFilter

data class LedgerFilter(
    val dateRange: DateRangeFilter = DateRangeFilter(),
    val typeFilter: TypeFilter = TypeFilter.ALL,
    val categoryId: Long? = null,
    val accountId: Long? = null
)

enum class TypeFilter(val label: String) {
    ALL("全部"),
    INCOME("收入"),
    EXPENSE("支出")
}
