package com.joker.homeledger.core.database.model

data class DailyTrendRow(
    val bucket: String,
    val incomeCent: Long?,
    val expenseCent: Long?
)
