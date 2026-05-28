package com.joker.homeledger.core.common

import java.math.BigDecimal
import java.math.RoundingMode

object MoneyFormatter {
    fun centToYuan(cent: Long): BigDecimal {
        return BigDecimal(cent).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
    }

    fun yuanToCent(yuan: String): Long {
        if (yuan.isBlank()) return 0L
        val normalized = yuan.trim()
        return BigDecimal(normalized)
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }
}
