package com.joker.homeledger.core.data.repository

import com.joker.homeledger.core.database.dao.AccountDao
import com.joker.homeledger.core.database.dao.TransactionDao
import com.joker.homeledger.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {
    fun observeAll(): Flow<List<AccountEntity>> = accountDao.observeAllAccounts()

    fun observeEnabled(): Flow<List<AccountEntity>> = accountDao.observeEnabledAccounts()

    suspend fun getById(id: Long): AccountEntity? = accountDao.getById(id)

    suspend fun isNameDuplicated(name: String, excludeId: Long = -1L): Boolean =
        accountDao.countByName(name.trim(), excludeId) > 0

    suspend fun addAccount(item: AccountEntity): Long = accountDao.insert(item)

    suspend fun updateAccount(item: AccountEntity) = accountDao.update(item)

    suspend fun canDelete(id: Long): Boolean = accountDao.countRelatedTransactions(id) == 0

    suspend fun deleteAccount(id: Long) = accountDao.deleteById(id)

    suspend fun setDisabled(account: AccountEntity, disabled: Boolean) {
        accountDao.update(
            account.copy(
                isDisabled = disabled,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun recalculateBalancesFromTransactions() {
        val now = System.currentTimeMillis()
        accountDao.listAll().forEach { account ->
            val delta = transactionDao.sumBalanceDeltaByAccount(account.id)
            accountDao.setBalance(account.id, delta, now)
        }
    }
}
