package com.joker.homeledger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.joker.homeledger.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM account WHERE isDisabled = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeEnabledAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account ORDER BY sortOrder ASC, id ASC")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT COUNT(1) FROM transaction_record WHERE accountId = :accountId")
    suspend fun countRelatedTransactions(accountId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<AccountEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReplace(items: List<AccountEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: AccountEntity): Long

    @Update
    suspend fun update(item: AccountEntity)

    @Query(
        """
        UPDATE account
        SET balanceCent = balanceCent + :deltaCent, updatedAt = :updatedAt
        WHERE id = :accountId
        """
    )
    suspend fun adjustBalance(accountId: Long, deltaCent: Long, updatedAt: Long)

    @Query("UPDATE account SET balanceCent = :balanceCent, updatedAt = :updatedAt WHERE id = :accountId")
    suspend fun setBalance(accountId: Long, balanceCent: Long, updatedAt: Long)

    @Query("DELETE FROM account WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(1) FROM account")
    suspend fun totalCount(): Int

    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AccountEntity?

    @Query(
        """
        SELECT COUNT(1) FROM account
        WHERE name = :name AND id != :excludeId
        """
    )
    suspend fun countByName(name: String, excludeId: Long): Int

    @Query("SELECT * FROM account ORDER BY id ASC")
    suspend fun listAll(): List<AccountEntity>

    @Query("DELETE FROM account")
    suspend fun clearAll()
}
