package com.joker.homeledger.core.database.di

import android.content.Context
import androidx.room.Room
import com.joker.homeledger.core.database.HomeLedgerDatabase
import com.joker.homeledger.core.database.migration.MIGRATION_1_2
import com.joker.homeledger.core.database.dao.AccountDao
import com.joker.homeledger.core.database.dao.CategoryDao
import com.joker.homeledger.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HomeLedgerDatabase {
        return Room.databaseBuilder(
            context,
            HomeLedgerDatabase::class.java,
            "home_ledger.db"
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    fun provideTransactionDao(db: HomeLedgerDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: HomeLedgerDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideAccountDao(db: HomeLedgerDatabase): AccountDao = db.accountDao()
}
