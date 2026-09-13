package app.prinkal.calculator.feature.lumpsum

data class LumpsumInput(
    val initialInvestment: Double,
    val annualReturnPercent: Double,
    val durationYears: Int
)

data class LumpsumYearlyGrowth(
    val year: Int,
    val value: Double,
    val estimatedReturns: Double
)

data class LumpsumResult(
    val investedAmount: Double,
    val estimatedReturns: Double,
    val finalValue: Double,
    val yearlyGrowth: List<LumpsumYearlyGrowth>
)

sealed interface LumpsumCalculationResult {
    data class Success(val value: LumpsumResult) : LumpsumCalculationResult

    data class Error(val message: String) : LumpsumCalculationResult
}

object LumpsumCalculator {
    fun calculate(input: LumpsumInput): LumpsumCalculationResult {
        if (!input.initialInvestment.isFinite() || input.initialInvestment <= 0.0) {
            return LumpsumCalculationResult.Error("Initial investment must be greater than zero")
        }
        if (!input.annualReturnPercent.isFinite() || input.annualReturnPercent <= -100.0) {
            return LumpsumCalculationResult.Error("Expected return must be greater than -100%")
        }
        if (input.durationYears !in 1..100) {
            return LumpsumCalculationResult.Error("Investment duration must be between 1 and 100 years")
        }

        val annualGrowth = 1.0 + input.annualReturnPercent / 100.0
        var value = input.initialInvestment
        val yearlyGrowth = mutableListOf<LumpsumYearlyGrowth>()
        for (year in 1..input.durationYears) {
            value *= annualGrowth
            if (!value.isFinite()) {
                return LumpsumCalculationResult.Error(
                    "Investment projection is outside the supported range"
                )
            }
            yearlyGrowth += LumpsumYearlyGrowth(
                year = year,
                value = value,
                estimatedReturns = value - input.initialInvestment
            )
        }

        return LumpsumCalculationResult.Success(
            LumpsumResult(
                investedAmount = input.initialInvestment,
                estimatedReturns = value - input.initialInvestment,
                finalValue = value,
                yearlyGrowth = yearlyGrowth
            )
        )
    }
}
