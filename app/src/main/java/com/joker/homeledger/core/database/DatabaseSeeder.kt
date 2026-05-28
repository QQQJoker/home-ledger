package com.joker.homeledger.core.database

import android.content.Context
import com.joker.homeledger.core.data.repository.AccountRepository
import com.joker.homeledger.core.database.dao.AccountDao
import com.joker.homeledger.core.database.dao.CategoryDao
import com.joker.homeledger.core.database.entity.AccountEntity
import com.joker.homeledger.core.database.entity.CategoryEntity
import com.joker.homeledger.core.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext context: Context,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val accountRepository: AccountRepository
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun seedIfNeeded(now: Long) {
        if (categoryDao.totalCount() == 0) {
            categoryDao.insertAll(defaultCategories(now))
        }
        if (accountDao.totalCount() == 0) {
            accountDao.insertAll(defaultAccounts(now))
        }
        if (!prefs.getBoolean(KEY_BALANCE_SYNCED_V2, false)) {
            accountRepository.recalculateBalancesFromTransactions()
            prefs.edit().putBoolean(KEY_BALANCE_SYNCED_V2, true).apply()
        }
    }

    private fun defaultCategories(now: Long): List<CategoryEntity> {
        val expense = listOf("餐饮", "交通", "住房", "购物", "教育", "医疗", "娱乐", "人情", "其他")
        val income = listOf("工资", "奖金", "理财", "红包", "其他")
        return buildList {
            expense.forEachIndexed { index, name ->
                add(
                    CategoryEntity(
                        name = name,
                        type = TransactionType.EXPENSE.name,
                        isPreset = true,
                        sortOrder = index,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
            income.forEachIndexed { index, name ->
                add(
                    CategoryEntity(
                        name = name,
                        type = TransactionType.INCOME.name,
                        isPreset = true,
                        sortOrder = index,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
        }
    }

    private fun defaultAccounts(now: Long): List<AccountEntity> {
        val names = listOf("现金", "微信", "支付宝", "银行卡")
        return names.mapIndexed { index, name ->
            AccountEntity(
                name = name,
                isPreset = true,
                balanceCent = 0L,
                sortOrder = index,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "home_ledger_db_seed"
        private const val KEY_BALANCE_SYNCED_V2 = "balance_synced_v2"
    }
}
