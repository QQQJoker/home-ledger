package com.joker.homeledger.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.joker.homeledger.core.database.dao.AccountDao
import com.joker.homeledger.core.database.dao.CategoryDao
import com.joker.homeledger.core.database.dao.TransactionDao
import com.joker.homeledger.core.database.entity.AccountEntity
import com.joker.homeledger.core.database.entity.CategoryEntity
import com.joker.homeledger.core.database.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, AccountEntity::class],
    version = 2,
    exportSchema = false
)
abstract class HomeLedgerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
}
