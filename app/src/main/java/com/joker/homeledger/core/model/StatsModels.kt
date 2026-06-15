package com.joker.homeledger.core.model

data class CategoryExpenseItem(
    val categoryId: Long,
    val categoryName: String,
    val totalCent: Long
)

data class TrendPoint(
    val bucket: String,
    val incomeCent: Long,
    val expenseCent: Long
)

data class MonthlyCompareItem(
    val monthKey: String,
    val incomeCent: Long,
    val expenseCent: Long
)
