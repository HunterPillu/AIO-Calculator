package app.prinkal.calculator.feature.loanprepayment

import kotlin.test.Test
import kotlin.test.assertTrue

class LoanPrepaymentCalculatorTest {
    @Test
    fun `prepayment reduces both tenure and interest`() {
        val result = LoanPrepaymentCalculator.calculate(
            LoanPrepaymentInput(
                outstandingPrincipal = 1_000_000.0,
                annualInterestRatePercent = 10.0,
                currentEmi = 21_247.04,
                remainingTenureMonths = 60,
                prepaymentAmount = 100_000.0
            )
        )

        assertTrue(result is LoanPrepaymentCalculationResult.Success)
        val value = (result as LoanPrepaymentCalculationResult.Success).value
        assertTrue(value.reduceTenure.months < value.original.months)
        assertTrue(value.reduceTenureInterestSaved > 0.0)
        assertTrue(value.reduceEmi.emi < value.original.emi)
        assertTrue(value.reduceEmiInterestSaved > 0.0)
    }

    @Test
    fun `rejects an emi that cannot repay the balance`() {
        val result = LoanPrepaymentCalculator.calculate(
            LoanPrepaymentInput(1_000_000.0, 12.0, 5_000.0, 60, 100_000.0)
        )

        assertTrue(result is LoanPrepaymentCalculationResult.Error)
    }

    @Test
    fun `rejects a zero interest emi that cannot cover the tenure`() {
        val result = LoanPrepaymentCalculator.calculate(
            LoanPrepaymentInput(1_000_000.0, 0.0, 100.0, 60, 100_000.0)
        )

        assertTrue(result is LoanPrepaymentCalculationResult.Error)
    }

    @Test
    fun `periodic prepayments shorten the loan`() {
        val result = LoanPrepaymentCalculator.calculatePeriodic(
            input = LoanPrepaymentInput(1_000_000.0, 10.0, 21_247.04, 60, 100_000.0),
            periodicAmount = 10_000.0,
            frequency = PrepaymentFrequency.MONTHLY
        )

        assertTrue(result is PeriodicPrepaymentCalculationResult.Success)
        val value = (result as PeriodicPrepaymentCalculationResult.Success).value
        assertTrue(value.afterPrepayments.months < value.original.months)
        assertTrue(value.interestSaved > 0.0)
    }
}
