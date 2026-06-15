package com.joker.homeledger.core.data.repository

import com.joker.homeledger.core.common.DateRangeFilter
import com.joker.homeledger.core.common.PeriodPreset
import com.joker.homeledger.core.common.StatsPeriodFilter
import com.joker.homeledger.core.common.StatsScope
import com.joker.homeledger.core.common.TimeRangeUtils
import com.joker.homeledger.core.database.dao.TransactionDao
import com.joker.homeledger.core.database.entity.TransactionEntity
import com.joker.homeledger.core.database.model.TransactionListItem
import com.joker.homeledger.core.model.CategoryExpenseItem
import com.joker.homeledger.core.model.LedgerFilter
import com.joker.homeledger.core.model.MonthlyCompareItem
import com.joker.homeledger.core.model.Summary
import com.joker.homeledger.core.model.TrendPoint
import com.joker.homeledger.core.model.TypeFilter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val transactionWriteRepository: TransactionWriteRepository
) {
    fun observeRecentWithDetails(limit: Int = 10): Flow<List<TransactionListItem>> =
        transactionDao.observeRecentWithDetails(limit)

    fun observeWithDetails(filter: LedgerFilter): Flow<List<TransactionListItem>> {
        val range = filter.dateRange.resolveRange()
        val typeFilter = when (filter.typeFilter) {
            TypeFilter.ALL -> "ALL"
            TypeFilter.INCOME -> "INCOME"
            TypeFilter.EXPENSE -> "EXPENSE"
        }
        return transactionDao.observeWithDetailsFiltered(
            startAt = range?.startAt,
            endAt = range?.endAt,
            typeFilter = typeFilter,
            categoryId = filter.categoryId ?: -1L,
            accountId = filter.accountId ?: -1L
        )
    }

    fun observeWithDetails(dateRange: DateRangeFilter, typeFilter: TypeFilter, categoryId: Long?, accountId: Long?): Flow<List<TransactionListItem>> {
        return observeWithDetails(
            LedgerFilter(
                dateRange = dateRange,
                typeFilter = typeFilter,
                categoryId = categoryId,
                accountId = accountId
            )
        )
    }

    suspend fun getWithDetailsById(id: Long): TransactionListItem? =
        transactionDao.getWithDetailsById(id)

    suspend fun getById(id: Long): TransactionEntity? = transactionDao.getById(id)

    suspend fun addTransaction(item: TransactionEntity): Long = transactionWriteRepository.addTransaction(item)

    suspend fun updateTransaction(old: TransactionEntity, new: TransactionEntity) =
        transactionWriteRepository.updateTransaction(old, new)

    suspend fun deleteTransaction(item: TransactionEntity) =
        transactionWriteRepository.deleteTransaction(item)

    suspend fun deleteTransactionById(id: Long) {
        val item = getById(id) ?: return
        deleteTransaction(item)
    }

    suspend fun getSummaryForPeriod(preset: PeriodPreset): Summary {
        val range = TimeRangeUtils.rangeFor(preset) ?: return Summary(0, 0)
        return getSummary(range.startAt, range.endAt)
    }

    suspend fun getSummary(dateRange: DateRangeFilter): Summary {
        val range = dateRange.resolveRange() ?: return Summary(0, 0)
        return getSummary(range.startAt, range.endAt)
    }

    suspend fun getCategoryExpenseRank(dateRange: DateRangeFilter): List<CategoryExpenseItem> {
        val range = dateRange.resolveRange() ?: return emptyList()
        return getCategoryExpenseRank(range.startAt, range.endAt)
    }

    suspend fun getTrend(dateRange: DateRangeFilter): List<TrendPoint> {
        val range = dateRange.resolveRange() ?: return emptyList()
        return getTrend(range.startAt, range.endAt)
    }

    suspend fun getSummary(startAt: Long, endAt: Long): Summary {
        val row = transactionDao.loadSummary(startAt = startAt, endAt = endAt)
        return Summary(
            incomeCent = row.incomeCent ?: 0L,
            expenseCent = row.expenseCent ?: 0L
        )
    }

    suspend fun getCategoryExpenseRank(startAt: Long, endAt: Long): List<CategoryExpenseItem> {
        return transactionDao.loadExpenseByCategoryWithName(startAt, endAt).map {
            CategoryExpenseItem(
                categoryId = it.categoryId,
                categoryName = it.categoryName,
                totalCent = it.totalCent
            )
        }
    }

    suspend fun getTrend(startAt: Long, endAt: Long): List<TrendPoint> {
        return transactionDao.loadDailyTrend(startAt, endAt).map {
            TrendPoint(
                bucket = it.bucket,
                incomeCent = it.incomeCent ?: 0L,
                expenseCent = it.expenseCent ?: 0L
            )
        }
    }

    suspend fun getMonthlyTrendForYear(year: Int): List<TrendPoint> {
        val range = TimeRangeUtils.yearRange(year)
        val rows = transactionDao.loadMonthlyTrend(range.startAt, range.endAt).map {
            TrendPoint(
                bucket = it.bucket,
                incomeCent = it.incomeCent ?: 0L,
                expenseCent = it.expenseCent ?: 0L
            )
        }
        val byBucket = rows.associateBy { it.bucket }
        return (1..12).map { month ->
            val key = "%04d-%02d".format(year, month)
            byBucket[key] ?: TrendPoint(key, 0L, 0L)
        }
    }

    suspend fun getYearlyTrendAll(): List<TrendPoint> {
        val rows = transactionDao.loadYearlyTrend().map {
            TrendPoint(
                bucket = it.bucket,
                incomeCent = it.incomeCent ?: 0L,
                expenseCent = it.expenseCent ?: 0L
            )
        }
        if (rows.isEmpty()) return emptyList()
        val minYear = rows.minOf { it.bucket.toInt() }
        val maxYear = java.time.Year.now().value
        val byBucket = rows.associateBy { it.bucket }
        return (minYear..maxYear).map { year ->
            val key = year.toString()
            byBucket[key] ?: TrendPoint(key, 0L, 0L)
        }
    }

    suspend fun getSummary(period: StatsPeriodFilter): Summary {
        val range = period.resolveRange()
        return getSummary(range.startAt, range.endAt)
    }

    suspend fun getCategoryExpenseRank(period: StatsPeriodFilter): List<CategoryExpenseItem> {
        val range = period.resolveRange()
        return getCategoryExpenseRank(range.startAt, range.endAt)
    }

    suspend fun getTrendForStats(period: StatsPeriodFilter): List<TrendPoint> {
        return when (period.scope) {
            StatsScope.YEAR -> getMonthlyTrendForYear(period.year)
            StatsScope.ALL -> getYearlyTrendAll()
            StatsScope.MONTH -> emptyList()
        }
    }

    suspend fun getMonthlyCompare(limit: Int = 6): List<MonthlyCompareItem> {
        return transactionDao.loadMonthlyCompare(limit).map {
            MonthlyCompareItem(
                monthKey = it.monthKey,
                incomeCent = it.incomeCent ?: 0L,
                expenseCent = it.expenseCent ?: 0L
            )
        }.reversed()
    }
}
