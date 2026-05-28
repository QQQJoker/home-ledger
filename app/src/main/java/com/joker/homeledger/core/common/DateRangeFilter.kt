package com.joker.homeledger.core.common

data class DateRangeFilter(
    val preset: PeriodPreset = PeriodPreset.THIS_MONTH,
    val useCustomRange: Boolean = false,
    val customStartAt: Long? = null,
    val customEndAt: Long? = null
) {
    fun resolveRange(nowMillis: Long = System.currentTimeMillis()): TimeRange? {
        if (useCustomRange) {
            val start = customStartAt ?: return null
            val end = customEndAt ?: return null
            if (start > end) return null
            return TimeRange(startAt = start, endAt = end)
        }
        return TimeRangeUtils.rangeFor(preset, nowMillis)
    }

    fun displayLabel(): String {
        if (useCustomRange && customStartAt != null && customEndAt != null) {
            return DateFormatUtils.formatDateRange(customStartAt, customEndAt)
        }
        return preset.label
    }
}
