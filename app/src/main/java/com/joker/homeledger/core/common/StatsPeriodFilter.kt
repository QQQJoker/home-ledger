package com.joker.homeledger.core.common

import java.time.ZoneId
import java.time.ZonedDateTime

enum class StatsScope(val label: String) {
    MONTH("月度"),
    YEAR("年度"),
    ALL("全部")
}

data class StatsPeriodFilter(
    val scope: StatsScope = StatsScope.MONTH,
    val year: Int = currentYear(),
    val month: Int = currentMonth()
) {
    fun resolveRange(): TimeRange = when (scope) {
        StatsScope.MONTH -> TimeRangeUtils.monthRange(year, month)
        StatsScope.YEAR -> TimeRangeUtils.yearRange(year)
        StatsScope.ALL -> TimeRangeUtils.allRange()
    }

    fun periodTitle(): String = when (scope) {
        StatsScope.MONTH -> "${year}年${month}月"
        StatsScope.YEAR -> "${year}年"
        StatsScope.ALL -> "全部"
    }

    fun previous(): StatsPeriodFilter = when (scope) {
        StatsScope.MONTH -> {
            if (month > 1) copy(month = month - 1)
            else copy(year = year - 1, month = 12)
        }
        StatsScope.YEAR -> copy(year = year - 1)
        StatsScope.ALL -> this
    }

    fun next(): StatsPeriodFilter = when (scope) {
        StatsScope.MONTH -> {
            if (month < 12) copy(month = month + 1)
            else copy(year = year + 1, month = 1)
        }
        StatsScope.YEAR -> copy(year = year + 1)
        StatsScope.ALL -> this
    }

    companion object {
        private fun currentYear(): Int =
            ZonedDateTime.now(ZoneId.systemDefault()).year

        private fun currentMonth(): Int =
            ZonedDateTime.now(ZoneId.systemDefault()).monthValue
    }
}
