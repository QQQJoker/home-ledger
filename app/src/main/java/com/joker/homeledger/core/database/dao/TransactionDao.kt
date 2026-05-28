package com.joker.homeledger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.joker.homeledger.core.database.entity.TransactionEntity
import com.joker.homeledger.core.database.model.DailyTrendRow
import com.joker.homeledger.core.database.model.MonthlyCompareRow
import com.joker.homeledger.core.database.model.CategoryExpenseRow
import com.joker.homeledger.core.database.model.CategoryExpenseWithNameRow
import com.joker.homeledger.core.database.model.SummaryRow
import com.joker.homeledger.core.database.model.TransactionListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT t.*, c.name AS categoryName, a.name AS accountName
        FROM transaction_record t
        INNER JOIN category c ON t.categoryId = c.id
        INNER JOIN account a ON t.accountId = a.id
        ORDER BY t.occurredAt DESC, t.id DESC
        LIMIT :limit
        """
    )
    fun observeRecentWithDetails(limit: Int): Flow<List<TransactionListItem>>

    @Query(
        """
        SELECT t.*, c.name AS categoryName, a.name AS accountName
        FROM transaction_record t
        INNER JOIN category c ON t.categoryId = c.id
        INNER JOIN account a ON t.accountId = a.id
        WHERE (:startAt IS NULL OR t.occurredAt >= :startAt)
          AND (:endAt IS NULL OR t.occurredAt <= :endAt)
          AND (:typeFilter = 'ALL' OR t.type = :typeFilter)
          AND (:categoryId < 0 OR t.categoryId = :categoryId)
          AND (:accountId < 0 OR t.accountId = :accountId)
        ORDER BY t.occurredAt DESC, t.id DESC
        """
    )
    fun observeWithDetailsFiltered(
        startAt: Long?,
        endAt: Long?,
        typeFilter: String,
        categoryId: Long,
        accountId: Long
    ): Flow<List<TransactionListItem>>

    @Query(
        """
        SELECT t.*, c.name AS categoryName, a.name AS accountName
        FROM transaction_record t
        INNER JOIN category c ON t.categoryId = c.id
        INNER JOIN account a ON t.accountId = a.id
        WHERE t.id = :id
        LIMIT 1
        """
    )
    suspend fun getWithDetailsById(id: Long): TransactionListItem?

    @Query("SELECT * FROM transaction_record WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: TransactionEntity): Long

    @Update
    suspend fun update(item: TransactionEntity)

    @Query("DELETE FROM transaction_record WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT
            SUM(CASE WHEN type = 'INCOME' THEN amountCent ELSE 0 END) AS incomeCent,
            SUM(CASE WHEN type = 'EXPENSE' THEN amountCent ELSE 0 END) AS expenseCent
        FROM transaction_record
        WHERE occurredAt BETWEEN :startAt AND :endAt
        """
    )
    suspend fun loadSummary(startAt: Long, endAt: Long): SummaryRow

    @Query(
        """
        SELECT categoryId, SUM(amountCent) AS totalCent
        FROM transaction_record
        WHERE type = 'EXPENSE' AND occurredAt BETWEEN :startAt AND :endAt
        GROUP BY categoryId
        ORDER BY totalCent DESC
        """
    )
    suspend fun loadExpenseByCategory(startAt: Long, endAt: Long): List<CategoryExpenseRow>

    @Query(
        """
        SELECT c.name AS categoryName, SUM(t.amountCent) AS totalCent
        FROM transaction_record t
        INNER JOIN category c ON t.categoryId = c.id
        WHERE t.type = 'EXPENSE' AND t.occurredAt BETWEEN :startAt AND :endAt
        GROUP BY t.categoryId
        ORDER BY totalCent DESC
        """
    )
    suspend fun loadExpenseByCategoryWithName(
        startAt: Long,
        endAt: Long
    ): List<CategoryExpenseWithNameRow>

    @Query(
        """
        SELECT
            strftime('%Y-%m-%d', occurredAt / 1000, 'unixepoch', 'localtime') AS bucket,
            SUM(CASE WHEN type = 'INCOME' THEN amountCent ELSE 0 END) AS incomeCent,
            SUM(CASE WHEN type = 'EXPENSE' THEN amountCent ELSE 0 END) AS expenseCent
        FROM transaction_record
        WHERE occurredAt BETWEEN :startAt AND :endAt
        GROUP BY bucket
        ORDER BY bucket ASC
        """
    )
    suspend fun loadDailyTrend(startAt: Long, endAt: Long): List<DailyTrendRow>

    @Query(
        """
        SELECT
            strftime('%Y-%m', occurredAt / 1000, 'unixepoch', 'localtime') AS bucket,
            SUM(CASE WHEN type = 'INCOME' THEN amountCent ELSE 0 END) AS incomeCent,
            SUM(CASE WHEN type = 'EXPENSE' THEN amountCent ELSE 0 END) AS expenseCent
        FROM transaction_record
        WHERE occurredAt BETWEEN :startAt AND :endAt
        GROUP BY bucket
        ORDER BY bucket ASC
        """
    )
    suspend fun loadMonthlyTrend(startAt: Long, endAt: Long): List<DailyTrendRow>

    @Query(
        """
        SELECT
            strftime('%Y', occurredAt / 1000, 'unixepoch', 'localtime') AS bucket,
            SUM(CASE WHEN type = 'INCOME' THEN amountCent ELSE 0 END) AS incomeCent,
            SUM(CASE WHEN type = 'EXPENSE' THEN amountCent ELSE 0 END) AS expenseCent
        FROM transaction_record
        GROUP BY bucket
        ORDER BY bucket ASC
        """
    )
    suspend fun loadYearlyTrend(): List<DailyTrendRow>

    @Query(
        """
        SELECT
            strftime('%Y-%m', occurredAt / 1000, 'unixepoch', 'localtime') AS monthKey,
            SUM(CASE WHEN type = 'INCOME' THEN amountCent ELSE 0 END) AS incomeCent,
            SUM(CASE WHEN type = 'EXPENSE' THEN amountCent ELSE 0 END) AS expenseCent
        FROM transaction_record
        GROUP BY monthKey
        ORDER BY monthKey DESC
        LIMIT :limit
        """
    )
    suspend fun loadMonthlyCompare(limit: Int): List<MonthlyCompareRow>

    @Query("SELECT * FROM transaction_record ORDER BY id ASC")
    suspend fun listAll(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReplace(items: List<TransactionEntity>)

    @Query("DELETE FROM transaction_record")
    suspend fun clearAll()

    @Query(
        """
        SELECT COALESCE(
            SUM(
                CASE
                    WHEN type = 'INCOME' THEN amountCent
                    ELSE -amountCent
                END
            ),
            0
        )
        FROM transaction_record
        WHERE accountId = :accountId
        """
    )
    suspend fun sumBalanceDeltaByAccount(accountId: Long): Long
}
