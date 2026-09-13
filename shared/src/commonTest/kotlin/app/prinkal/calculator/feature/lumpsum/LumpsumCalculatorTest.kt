package app.prinkal.calculator.feature.lumpsum

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LumpsumCalculatorTest {
    @Test
    fun `zero return keeps the initial investment unchanged`() {
        val result = successful(LumpsumInput(100_000.0, 0.0, 5))

        assertEquals(100_000.0, result.finalValue, 0.000001)
        assertEquals(0.0, result.estimatedReturns, 0.000001)
        assertEquals(5, result.yearlyGrowth.size)
    }

    @Test
    fun `compounds annual returns`() {
        val result = successful(LumpsumInput(100_000.0, 10.0, 2))

        assertEquals(121_000.0, result.finalValue, 0.000001)
        assertEquals(21_000.0, result.estimatedReturns, 0.000001)
    }

    @Test
    fun `rejects invalid input`() {
        assertTrue(
            LumpsumCalculator.calculate(LumpsumInput(0.0, 10.0, 5)) is LumpsumCalculationResult.Error
        )
        assertTrue(
            LumpsumCalculator.calculate(LumpsumInput(100_000.0, -100.0, 5)) is LumpsumCalculationResult.Error
        )
        assertTrue(
            LumpsumCalculator.calculate(LumpsumInput(100_000.0, 10.0, 0)) is LumpsumCalculationResult.Error
        )
    }

    private fun successful(input: LumpsumInput): LumpsumResult {
        val result = LumpsumCalculator.calculate(input)
        assertTrue(result is LumpsumCalculationResult.Success)
        return (result as LumpsumCalculationResult.Success).value
    }
}
