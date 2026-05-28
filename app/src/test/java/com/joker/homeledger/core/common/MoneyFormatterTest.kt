package com.joker.homeledger.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun `cent to yuan keeps two decimals`() {
        assertEquals("12.34", MoneyFormatter.centToYuan(1234).toPlainString())
    }

    @Test
    fun `yuan to cent rounds half up`() {
        assertEquals(1005L, MoneyFormatter.yuanToCent("10.045"))
    }
}
