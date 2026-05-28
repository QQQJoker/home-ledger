package com.joker.homeledger.core.backup

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import com.joker.homeledger.core.database.HomeLedgerDatabase
import com.joker.homeledger.core.database.dao.AccountDao
import com.joker.homeledger.core.database.dao.CategoryDao
import com.joker.homeledger.core.database.dao.TransactionDao
import com.joker.homeledger.core.database.entity.AccountEntity
import com.joker.homeledger.core.database.entity.CategoryEntity
import com.joker.homeledger.core.database.entity.TransactionEntity
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.serialization.json.Json

@Singleton
class BackupManager @Inject constructor(
    private val database: HomeLedgerDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) {
    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    suspend fun exportBackup(contentResolver: ContentResolver, uri: Uri, password: String) {
        val payload = buildPayload()
        val plainBytes = json.encodeToString(BackupPayload.serializer(), payload).encodeToByteArray()
        val encrypted = encrypt(plainBytes, password)
        contentResolver.openOutputStream(uri)?.use { output ->
            output.write(encrypted)
            output.flush()
        } ?: error("无法写入备份文件")
    }

    suspend fun importBackup(contentResolver: ContentResolver, uri: Uri, password: String) {
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("无法读取备份文件")
        val plain = decrypt(bytes, password)
        val payload = json.decodeFromString(BackupPayload.serializer(), plain.decodeToString())
        if (payload.schemaVersion != SCHEMA_VERSION) {
            error("备份版本不兼容")
        }
        database.withTransaction {
            transactionDao.clearAll()
            categoryDao.clearAll()
            accountDao.clearAll()

            categoryDao.insertAllReplace(
                payload.categories.map {
                    CategoryEntity(
                        id = it.id,
                        name = it.name,
                        type = it.type,
                        icon = it.icon,
                        color = it.color,
                        isPreset = it.isPreset,
                        isHidden = it.isHidden,
                        sortOrder = it.sortOrder,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                }
            )
            accountDao.insertAllReplace(
                payload.accounts.map {
                    AccountEntity(
                        id = it.id,
                        name = it.name,
                        icon = it.icon,
                        color = it.color,
                        isPreset = it.isPreset,
                        isDisabled = it.isDisabled,
                        sortOrder = it.sortOrder,
                        balanceCent = it.balanceCent,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                }
            )
            transactionDao.insertAllReplace(
                payload.transactions.map {
                    TransactionEntity(
                        id = it.id,
                        type = it.type,
                        amountCent = it.amountCent,
                        categoryId = it.categoryId,
                        accountId = it.accountId,
                        occurredAt = it.occurredAt,
                        note = it.note,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                }
            )
        }
    }

    private suspend fun buildPayload(): BackupPayload {
        return BackupPayload(
            schemaVersion = SCHEMA_VERSION,
            exportedAt = System.currentTimeMillis(),
            categories = categoryDao.listAll().map {
                BackupCategory(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    icon = it.icon,
                    color = it.color,
                    isPreset = it.isPreset,
                    isHidden = it.isHidden,
                    sortOrder = it.sortOrder,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            },
            accounts = accountDao.listAll().map {
                BackupAccount(
                    id = it.id,
                    name = it.name,
                    icon = it.icon,
                    color = it.color,
                    isPreset = it.isPreset,
                    isDisabled = it.isDisabled,
                    sortOrder = it.sortOrder,
                    balanceCent = it.balanceCent,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            },
            transactions = transactionDao.listAll().map {
                BackupTransaction(
                    id = it.id,
                    type = it.type,
                    amountCent = it.amountCent,
                    categoryId = it.categoryId,
                    accountId = it.accountId,
                    occurredAt = it.occurredAt,
                    note = it.note,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        )
    }

    private fun encrypt(plain: ByteArray, password: String): ByteArray {
        val salt = Random.nextBytes(16)
        val iv = Random.nextBytes(12)
        val key = deriveAesKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plain)

        return buildList<Byte> {
            addAll(MAGIC.toList())
            addAll(salt.toList())
            addAll(iv.toList())
            addAll(encrypted.toList())
        }.toByteArray()
    }

    private fun decrypt(raw: ByteArray, password: String): ByteArray {
        require(raw.size > MAGIC.size + 16 + 12) { "备份文件格式错误" }
        val magic = raw.copyOfRange(0, MAGIC.size)
        require(magic.contentEquals(MAGIC)) { "备份文件头无效" }
        val saltStart = MAGIC.size
        val ivStart = saltStart + 16
        val dataStart = ivStart + 12
        val salt = raw.copyOfRange(saltStart, ivStart)
        val iv = raw.copyOfRange(ivStart, dataStart)
        val encrypted = raw.copyOfRange(dataStart, raw.size)

        val key = deriveAesKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted)
    }

    private fun deriveAesKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    companion object {
        private val MAGIC = "HLBK1".encodeToByteArray()
        private const val SCHEMA_VERSION = 2
    }
}
