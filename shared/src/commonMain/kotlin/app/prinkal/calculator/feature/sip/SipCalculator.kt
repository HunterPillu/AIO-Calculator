package app.prinkal.calculator.feature.sip

data class SipInput(
    val monthlyInvestment: Double,
    val annualReturnPercent: Double,
    val durationYears: Int,
    val annualStepUpPercent: Double = 0.0
)

data class SipYearlyGrowth(
    val year: Int,
    val invested: Double,
    val estimatedReturns: Double,
    val value: Double
)

data class SipResult(
    val totalInvested: Double,
    val estimatedReturns: Double,
    val finalValue: Double,
    val yearlyGrowth: List<SipYearlyGrowth>
)

sealed interface SipCalculationResult {
    data class Success(val value: SipResult) : SipCalculationResult

    data class Error(val message: String) : SipCalculationResult
}

object SipCalculator {
    fun calculate(input: SipInput): SipCalculationResult {
        if (!input.monthlyInvestment.isFinite() || input.monthlyInvestment <= 0.0) {
            return SipCalculationResult.Error("Monthly SIP must be greater than zero")
        }
        if (!input.annualReturnPercent.isFinite() || input.annualReturnPercent <= -100.0) {
            return SipCalculationResult.Error("Expected return must be greater than -100%")
        }
        if (input.durationYears !in 1..100) {
            return SipCalculationResult.Error("Investment duration must be between 1 and 100 years")
        }
        if (!input.annualStepUpPercent.isFinite() ||
            input.annualStepUpPercent < 0.0 ||
            input.annualStepUpPercent > 100.0
        ) {
            return SipCalculationResult.Error("Annual step-up must be between 0% and 100%")
        }

        val monthlyRate = input.annualReturnPercent / 1_200.0
        val monthlyGrowth = 1.0 + monthlyRate
        var monthlyInvestment = input.monthlyInvestment
        var balance = 0.0
        var totalInvested = 0.0
        val yearlyGrowth = mutableListOf<SipYearlyGrowth>()

        for (year in 1..input.durationYears) {
            val investedAtStart = totalInvested
            repeat(12) {
                balance = (balance + monthlyInvestment) * monthlyGrowth
                totalInvested += monthlyInvestment
            }
            val estimatedReturns = balance - totalInvested
            yearlyGrowth += SipYearlyGrowth(
                year = year,
                invested = totalInvested - investedAtStart,
                estimatedReturns = estimatedReturns,
                value = balance
            )
            monthlyInvestment *= 1.0 + input.annualStepUpPercent / 100.0
        }

        if (!balance.isFinite() || !totalInvested.isFinite()) {
            return SipCalculationResult.Error("Investment projection is outside the supported range")
        }
        return SipCalculationResult.Success(
            SipResult(
                totalInvested = totalInvested,
                estimatedReturns = balance - totalInvested,
                finalValue = balance,
                yearlyGrowth = yearlyGrowth
            )
        )
    }
}
