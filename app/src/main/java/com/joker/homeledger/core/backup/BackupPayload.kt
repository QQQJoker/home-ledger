package com.joker.homeledger.core.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val schemaVersion: Int,
    val exportedAt: Long,
    val categories: List<BackupCategory>,
    val accounts: List<BackupAccount>,
    val transactions: List<BackupTransaction>
)

@Serializable
data class BackupCategory(
    val id: Long,
    val name: String,
    val type: String,
    val icon: String?,
    val color: Int?,
    val isPreset: Boolean,
    val isHidden: Boolean,
    val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupAccount(
    val id: Long,
    val name: String,
    val icon: String?,
    val color: Int?,
    val isPreset: Boolean,
    val isDisabled: Boolean,
    val sortOrder: Int,
    val balanceCent: Long = 0L,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupTransaction(
    val id: Long,
    val type: String,
    val amountCent: Long,
    val categoryId: Long,
    val accountId: Long,
    val occurredAt: Long,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
)
