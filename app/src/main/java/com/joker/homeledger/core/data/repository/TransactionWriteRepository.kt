package com.joker.homeledger.core.data.repository

import androidx.room.withTransaction
import com.joker.homeledger.core.database.HomeLedgerDatabase
import com.joker.homeledger.core.database.dao.AccountDao
import com.joker.homeledger.core.database.dao.TransactionDao
import com.joker.homeledger.core.database.entity.TransactionEntity
import com.joker.homeledger.core.model.TransactionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionWriteRepository @Inject constructor(
    private val database: HomeLedgerDatabase,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {
    suspend fun addTransaction(item: TransactionEntity): Long {
        return database.withTransaction {
            val id = transactionDao.insert(item)
            accountDao.adjustBalance(
                accountId = item.accountId,
                deltaCent = balanceDelta(item.type, item.amountCent),
                updatedAt = System.currentTimeMillis()
            )
            id
        }
    }

    suspend fun updateTransaction(old: TransactionEntity, new: TransactionEntity) {
        database.withTransaction {
            accountDao.adjustBalance(
                accountId = old.accountId,
                deltaCent = -balanceDelta(old.type, old.amountCent),
                updatedAt = System.currentTimeMillis()
            )
            transactionDao.update(new)
            accountDao.adjustBalance(
                accountId = new.accountId,
                deltaCent = balanceDelta(new.type, new.amountCent),
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    suspend fun deleteTransaction(item: TransactionEntity) {
        database.withTransaction {
            accountDao.adjustBalance(
                accountId = item.accountId,
                deltaCent = -balanceDelta(item.type, item.amountCent),
                updatedAt = System.currentTimeMillis()
            )
            transactionDao.deleteById(item.id)
        }
    }

    private fun balanceDelta(type: String, amountCent: Long): Long {
        return if (type == TransactionType.INCOME.name) amountCent else -amountCent
    }
}
