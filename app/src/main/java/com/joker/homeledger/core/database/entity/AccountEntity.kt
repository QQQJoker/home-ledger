package com.joker.homeledger.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "account",
    indices = [Index(value = ["name"], unique = true)]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String? = null,
    val color: Int? = null,
    val isPreset: Boolean,
    val isDisabled: Boolean = false,
    val balanceCent: Long = 0L,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)
