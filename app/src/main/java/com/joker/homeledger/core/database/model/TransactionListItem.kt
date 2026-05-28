package com.joker.homeledger.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.joker.homeledger.core.database.entity.TransactionEntity

data class TransactionListItem(
    @Embedded val transaction: TransactionEntity,
    @ColumnInfo(name = "categoryName") val categoryName: String,
    @ColumnInfo(name = "accountName") val accountName: String
)
