package com.joker.homeledger.core.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateFormatUtils {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun formatDateTime(millis: Long): String {
        val zone = ZoneId.systemDefault()
        return Instant.ofEpochMilli(millis).atZone(zone).format(dateTimeFormatter)
    }

    fun formatDate(millis: Long): String {
        val zone = ZoneId.systemDefault()
        return Instant.ofEpochMilli(millis).atZone(zone).format(dateFormatter)
    }

    fun formatDateRange(startAt: Long, endAt: Long): String {
        return "${formatDate(startAt)} ~ ${formatDate(endAt)}"
    }
}
