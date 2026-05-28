package com.joker.homeledger.core.model

data class Summary(
    val incomeCent: Long,
    val expenseCent: Long
) {
    val balanceCent: Long = incomeCent - expenseCent
}
