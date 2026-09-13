package app.prinkal.calculator.feature.sip

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SipCalculatorTest {
    @Test
    fun `zero return keeps final value equal to invested amount`() {
        val result = successful(
            SipInput(
                monthlyInvestment = 10_000.0,
                annualReturnPercent = 0.0,
                durationYears = 1
            )
        )

        assertEquals(120_000.0, result.totalInvested, 0.000001)
        assertEquals(120_000.0, result.finalValue, 0.000001)
        assertEquals(0.0, result.estimatedReturns, 0.000001)
        assertEquals(1, result.yearlyGrowth.size)
    }

    @Test
    fun `step up increases later yearly contributions`() {
        val result = successful(
            SipInput(
                monthlyInvestment = 5_000.0,
                annualReturnPercent = 12.0,
                durationYears = 3,
                annualStepUpPercent = 10.0
            )
        )

        assertTrue(result.yearlyGrowth[1].invested > result.yearlyGrowth[0].invested)
        assertTrue(result.finalValue > result.totalInvested)
    }

    @Test
    fun `rejects invalid sip input`() {
        assertTrue(
            SipCalculator.calculate(SipInput(0.0, 12.0, 10)) is SipCalculationResult.Error
        )
        assertTrue(
            SipCalculator.calculate(SipInput(1_000.0, 12.0, 0)) is SipCalculationResult.Error
        )
        assertTrue(
            SipCalculator.calculate(SipInput(1_000.0, 12.0, 10, 101.0)) is SipCalculationResult.Error
        )
    }

    private fun successful(input: SipInput): SipResult {
        val result = SipCalculator.calculate(input)
        assertTrue(result is SipCalculationResult.Success)
        return (result as SipCalculationResult.Success).value
    }
}
