package app.prinkal.calculator.core.formatting

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormattingTest {
    @Test
    fun `formats results with at most two fraction digits`() {
        assertEquals("12.35", formatNumber(12.345))
        assertEquals("12", formatNumber(12.0))
        assertEquals("-0.5", formatNumber(-0.5))
    }

    @Test
    fun `formats currency with rupee symbol`() {
        assertEquals("₹1000.5", formatCurrency(1000.5))
    }
}
