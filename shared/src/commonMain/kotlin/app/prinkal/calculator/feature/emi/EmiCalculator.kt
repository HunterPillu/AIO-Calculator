package app.prinkal.calculator.feature.emi

data class EmiInput(
    val principal: Double,
    val annualInterestRatePercent: Double,
    val tenureMonths: Int
)

data class AmortizationRow(
    val month: Int,
    val payment: Double,
    val principal: Double,
    val interest: Double,
    val remainingBalance: Double
)

data class EmiResult(
    val monthlyEmi: Double,
    val totalPrincipal: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val schedule: List<AmortizationRow>
)

sealed interface EmiCalculationResult {
    data class Success(val value: EmiResult) : EmiCalculationResult

    data class Error(val message: String) : EmiCalculationResult
}

object EmiCalculator {
    fun calculate(input: EmiInput): EmiCalculationResult {
        if (!input.principal.isFinite() || input.principal <= 0.0) {
            return EmiCalculationResult.Error("Loan amount must be greater than zero")
        }
        if (!input.annualInterestRatePercent.isFinite() ||
            input.annualInterestRatePercent < 0.0
        ) {
            return EmiCalculationResult.Error("Interest rate cannot be negative")
        }
        if (input.tenureMonths <= 0) {
            return EmiCalculationResult.Error("Tenure must be at least one month")
        }
        if (input.tenureMonths > 1_200) {
            return EmiCalculationResult.Error("Tenure cannot exceed 100 years")
        }

        val monthlyRate = input.annualInterestRatePercent / 1_200.0
        val monthlyEmi = if (monthlyRate == 0.0) {
            input.principal / input.tenureMonths
        } else {
            val growth = (1.0 + monthlyRate).pow(input.tenureMonths)
            input.principal * monthlyRate * growth / (growth - 1.0)
        }
        if (!monthlyEmi.isFinite()) {
            return EmiCalculationResult.Error("Loan calculation is outside the supported range")
        }

        val schedule = buildSchedule(input, monthlyRate, monthlyEmi)
        val totalPayment = schedule.sumOf { it.payment }
        val totalInterest = totalPayment - input.principal
        return EmiCalculationResult.Success(
            EmiResult(
                monthlyEmi = monthlyEmi,
                totalPrincipal = input.principal,
                totalInterest = totalInterest.coerceAtLeast(0.0),
                totalPayment = totalPayment,
                schedule = schedule
            )
        )
    }

    private fun buildSchedule(
        input: EmiInput,
        monthlyRate: Double,
        monthlyEmi: Double
    ): List<AmortizationRow> {
        var balance = input.principal
        return (1..input.tenureMonths).map { month ->
            val interest = balance * monthlyRate
            val regularPrincipal = (monthlyEmi - interest).coerceAtLeast(0.0)
            val principal = if (month == input.tenureMonths) {
                balance
            } else {
                regularPrincipal.coerceAtMost(balance)
            }
            val payment = principal + interest
            balance = (balance - principal).coerceAtLeast(0.0)
            AmortizationRow(
                month = month,
                payment = payment,
                principal = principal,
                interest = interest,
                remainingBalance = balance
            )
        }
    }

    private fun Double.pow(exponent: Int): Double {
        var result = 1.0
        repeat(exponent) {
            result *= this
        }
        return result
    }
}
