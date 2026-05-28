package com.joker.homeledger.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    indices = [
        Index(value = ["type", "name"], unique = true),
        Index(value = ["type", "isHidden", "sortOrder"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val icon: String? = null,
    val color: Int? = null,
    val isPreset: Boolean,
    val isHidden: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)
