package com.joker.homeledger.core.common

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

object TimeRangeUtils {
    fun rangeFor(preset: PeriodPreset, nowMillis: Long = System.currentTimeMillis()): TimeRange? {
        return when (preset) {
            PeriodPreset.ALL -> null
            PeriodPreset.TODAY -> today(nowMillis)
            PeriodPreset.THIS_WEEK -> thisWeek(nowMillis)
            PeriodPreset.THIS_MONTH -> thisMonth(nowMillis)
            PeriodPreset.THIS_YEAR -> thisYear(nowMillis)
        }
    }

    fun today(nowMillis: Long = System.currentTimeMillis()): TimeRange {
        val zone = ZoneId.systemDefault()
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val start = now.toLocalDate().atStartOfDay(zone)
        val end = start.plusDays(1).minusNanos(1)
        return TimeRange(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun thisWeek(nowMillis: Long = System.currentTimeMillis()): TimeRange {
        val zone = ZoneId.systemDefault()
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val startDate = now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val start = startDate.atStartOfDay(zone)
        val end = start.plusDays(7).minusNanos(1)
        return TimeRange(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun thisMonth(nowMillis: Long = System.currentTimeMillis()): TimeRange {
        val zone = ZoneId.systemDefault()
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val start = ZonedDateTime.of(now.year, now.monthValue, 1, 0, 0, 0, 0, zone)
        val end = start.plusMonths(1).minusNanos(1)
        return TimeRange(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun thisYear(nowMillis: Long = System.currentTimeMillis()): TimeRange {
        val zone = ZoneId.systemDefault()
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val start = ZonedDateTime.of(now.year, 1, 1, 0, 0, 0, 0, zone)
        val end = start.plusYears(1).minusNanos(1)
        return TimeRange(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun monthRange(year: Int, month: Int): TimeRange {
        val zone = ZoneId.systemDefault()
        val start = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, zone)
        val end = start.plusMonths(1).minusNanos(1)
        return TimeRange(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun yearRange(year: Int): TimeRange {
        val zone = ZoneId.systemDefault()
        val start = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, zone)
        val end = start.plusYears(1).minusNanos(1)
        return TimeRange(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun allRange(): TimeRange = TimeRange(0L, Long.MAX_VALUE)
}
