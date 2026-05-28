package com.joker.homeledger.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateLabelUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("M月d日")

    fun dayGroupLabel(occurredAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(occurredAtMillis).atZone(zone).toLocalDate()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        return when (date) {
            today -> "今天"
            today.minusDays(1) -> "昨天"
            else -> date.format(dateFormatter)
        }
    }

    fun timeLabel(occurredAtMillis: Long): String {
        val zone = ZoneId.systemDefault()
        val time = Instant.ofEpochMilli(occurredAtMillis).atZone(zone).toLocalTime()
        return String.format("%02d:%02d", time.hour, time.minute)
    }
}
