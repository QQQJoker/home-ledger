package com.joker.homeledger.core.database.model

data class CategoryExpenseWithNameRow(
    val categoryId: Long,
    val categoryName: String,
    val totalCent: Long
)
