package com.joker.homeledger.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_record",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["occurredAt"]),
        Index(value = ["type", "occurredAt"]),
        Index(value = ["categoryId", "occurredAt"]),
        Index(value = ["accountId", "occurredAt"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val amountCent: Long,
    val categoryId: Long,
    val accountId: Long,
    val occurredAt: Long,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
