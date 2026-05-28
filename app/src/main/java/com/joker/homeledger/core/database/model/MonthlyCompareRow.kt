package com.joker.homeledger.core.database.model

data class MonthlyCompareRow(
    val monthKey: String,
    val incomeCent: Long?,
    val expenseCent: Long?
)
