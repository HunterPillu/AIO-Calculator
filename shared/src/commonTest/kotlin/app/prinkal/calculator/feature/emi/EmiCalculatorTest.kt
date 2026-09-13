package app.prinkal.calculator.feature.emi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmiCalculatorTest {
    @Test
    fun `calculates standard home loan emi`() {
        val result = successful(
            EmiInput(
                principal = 1_000_000.0,
                annualInterestRatePercent = 10.0,
                tenureMonths = 60
            )
        )

        assertEquals(21_247.04, result.monthlyEmi, 0.01)
        assertEquals(60, result.schedule.size)
        assertEquals(0.0, result.schedule.last().remainingBalance, 0.000001)
        assertTrue(result.totalInterest > 270_000.0)
    }

    @Test
    fun `calculates zero interest loan`() {
        val result = successful(EmiInput(120_000.0, 0.0, 12))

        assertEquals(10_000.0, result.monthlyEmi, 0.000001)
        assertEquals(0.0, result.totalInterest, 0.000001)
        assertEquals(120_000.0, result.totalPayment, 0.000001)
    }

    @Test
    fun `rejects invalid input`() {
        assertError(EmiInput(0.0, 10.0, 12))
        assertError(EmiInput(100_000.0, -1.0, 12))
        assertError(EmiInput(100_000.0, 10.0, 0))
    }

    private fun successful(input: EmiInput): EmiResult {
        val result = EmiCalculator.calculate(input)
        assertTrue(result is EmiCalculationResult.Success)
        return (result as EmiCalculationResult.Success).value
    }

    private fun assertError(input: EmiInput) {
        assertTrue(EmiCalculator.calculate(input) is EmiCalculationResult.Error)
    }
}
