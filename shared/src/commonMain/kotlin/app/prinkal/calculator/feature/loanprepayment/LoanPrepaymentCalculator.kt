package app.prinkal.calculator.feature.loanprepayment

data class LoanPrepaymentInput(
    val outstandingPrincipal: Double,
    val annualInterestRatePercent: Double,
    val currentEmi: Double,
    val remainingTenureMonths: Int,
    val prepaymentAmount: Double
)

data class LoanPlan(
    val months: Int,
    val emi: Double,
    val totalInterest: Double
)

data class LoanPrepaymentResult(
    val original: LoanPlan,
    val reduceTenure: LoanPlan,
    val reduceTenureInterestSaved: Double,
    val reduceEmi: LoanPlan,
    val reduceEmiInterestSaved: Double
)

enum class PrepaymentFrequency(val months: Int, val label: String) {
    MONTHLY(1, "Monthly"),
    QUARTERLY(3, "Quarterly"),
    HALF_YEARLY(6, "Half-yearly"),
    YEARLY(12, "Yearly")
}

data class PeriodicPrepaymentResult(
    val original: LoanPlan,
    val afterPrepayments: LoanPlan,
    val interestSaved: Double
)

sealed interface LoanPrepaymentCalculationResult {
    data class Success(val value: LoanPrepaymentResult) : LoanPrepaymentCalculationResult

    data class Error(val message: String) : LoanPrepaymentCalculationResult
}

sealed interface PeriodicPrepaymentCalculationResult {
    data class Success(val value: PeriodicPrepaymentResult) : PeriodicPrepaymentCalculationResult

    data class Error(val message: String) : PeriodicPrepaymentCalculationResult
}

object LoanPrepaymentCalculator {
    fun calculate(input: LoanPrepaymentInput): LoanPrepaymentCalculationResult {
        val validation = validate(input)
        if (validation != null) {
            return LoanPrepaymentCalculationResult.Error(validation)
        }

        val monthlyRate = input.annualInterestRatePercent / 1_200.0
        val original = buildPlan(
            balance = input.outstandingPrincipal,
            monthlyRate = monthlyRate,
            emi = input.currentEmi,
            maximumMonths = input.remainingTenureMonths
        ) ?: return LoanPrepaymentCalculationResult.Error(
            "Current EMI does not repay the loan within the remaining tenure"
        )

        val reducedBalance = input.outstandingPrincipal - input.prepaymentAmount
        val reduceTenure = buildPlan(
            balance = reducedBalance,
            monthlyRate = monthlyRate,
            emi = input.currentEmi,
            maximumMonths = input.remainingTenureMonths
        ) ?: return LoanPrepaymentCalculationResult.Error("Unable to calculate the reduced tenure")

        val reducedEmi = paymentFor(
            balance = reducedBalance,
            monthlyRate = monthlyRate,
            months = input.remainingTenureMonths
        )
        val reduceEmi = LoanPlan(
            months = input.remainingTenureMonths,
            emi = reducedEmi,
            totalInterest = (reducedEmi * input.remainingTenureMonths - reducedBalance).coerceAtLeast(0.0)
        )
        return LoanPrepaymentCalculationResult.Success(
            LoanPrepaymentResult(
                original = original,
                reduceTenure = reduceTenure,
                reduceTenureInterestSaved = (original.totalInterest - reduceTenure.totalInterest)
                    .coerceAtLeast(0.0),
                reduceEmi = reduceEmi,
                reduceEmiInterestSaved = (original.totalInterest - reduceEmi.totalInterest)
                    .coerceAtLeast(0.0)
            )
        )
    }

    fun calculatePeriodic(
        input: LoanPrepaymentInput,
        periodicAmount: Double,
        frequency: PrepaymentFrequency
    ): PeriodicPrepaymentCalculationResult {
        val validation = validateBase(input)
        if (validation != null) {
            return PeriodicPrepaymentCalculationResult.Error(validation)
        }
        if (!periodicAmount.isFinite() || periodicAmount <= 0.0) {
            return PeriodicPrepaymentCalculationResult.Error(
                "Periodic prepayment must be greater than zero"
            )
        }

        val monthlyRate = input.annualInterestRatePercent / 1_200.0
        val original = buildPlan(
            balance = input.outstandingPrincipal,
            monthlyRate = monthlyRate,
            emi = input.currentEmi,
            maximumMonths = input.remainingTenureMonths
        ) ?: return PeriodicPrepaymentCalculationResult.Error(
            "Current EMI does not repay the loan within the remaining tenure"
        )
        val afterPrepayments = buildPeriodicPlan(
            input = input,
            monthlyRate = monthlyRate,
            periodicAmount = periodicAmount,
            frequency = frequency
        ) ?: return PeriodicPrepaymentCalculationResult.Error(
            "Unable to calculate the periodic prepayment"
        )
        return PeriodicPrepaymentCalculationResult.Success(
            PeriodicPrepaymentResult(
                original = original,
                afterPrepayments = afterPrepayments,
                interestSaved = (original.totalInterest - afterPrepayments.totalInterest)
                    .coerceAtLeast(0.0)
            )
        )
    }

    private fun validate(input: LoanPrepaymentInput): String? {
        val baseValidation = validateBase(input)
        if (baseValidation != null) {
            return baseValidation
        }
        if (input.prepaymentAmount <= 0.0 || !input.prepaymentAmount.isFinite()) {
            return "Prepayment must be greater than zero"
        }
        if (input.prepaymentAmount >= input.outstandingPrincipal) {
            return "Prepayment must be less than the outstanding principal"
        }
        return null
    }

    private fun validateBase(input: LoanPrepaymentInput): String? {
        if (!input.outstandingPrincipal.isFinite() || input.outstandingPrincipal <= 0.0) {
            return "Outstanding principal must be greater than zero"
        }
        if (!input.annualInterestRatePercent.isFinite() ||
            input.annualInterestRatePercent < 0.0
        ) {
            return "Interest rate cannot be negative"
        }
        if (!input.currentEmi.isFinite() || input.currentEmi <= 0.0) {
            return "Current EMI must be greater than zero"
        }
        if (input.remainingTenureMonths <= 0) {
            return "Remaining tenure must be at least one month"
        }
        return null
    }

    private fun buildPlan(
        balance: Double,
        monthlyRate: Double,
        emi: Double,
        maximumMonths: Int
    ): LoanPlan? {
        var remaining = balance
        var totalInterest = 0.0
        var months = 0
        while (remaining > 0.000001 && months < maximumMonths) {
            months++
            val interest = remaining * monthlyRate
            if (emi <= interest && remaining > emi) {
                return null
            }
            val payment = if (months == maximumMonths) {
                val finalPayment = remaining + interest
                if (finalPayment > emi + 1.0) {
                    return null
                }
                finalPayment
            } else {
                minOf(emi, remaining + interest)
            }
            val principal = payment - interest
            if (principal <= 0.0) {
                return null
            }
            remaining = (remaining - principal).coerceAtLeast(0.0)
            totalInterest += interest
        }
        if (remaining > 0.000001) {
            return null
        }
        return LoanPlan(months, emi, totalInterest)
    }

    private fun buildPeriodicPlan(
        input: LoanPrepaymentInput,
        monthlyRate: Double,
        periodicAmount: Double,
        frequency: PrepaymentFrequency
    ): LoanPlan? {
        var remaining = input.outstandingPrincipal
        var totalInterest = 0.0
        var months = 0
        while (remaining > 0.000001 && months < input.remainingTenureMonths) {
            months++
            val interest = remaining * monthlyRate
            if (input.currentEmi <= interest && remaining > input.currentEmi) {
                return null
            }
            val payment = if (months == input.remainingTenureMonths) {
                val finalPayment = remaining + interest
                if (finalPayment > input.currentEmi + 1.0) {
                    return null
                }
                finalPayment
            } else {
                minOf(input.currentEmi, remaining + interest)
            }
            val principal = payment - interest
            if (principal <= 0.0) {
                return null
            }
            remaining = (remaining - principal).coerceAtLeast(0.0)
            if (months % frequency.months == 0) {
                remaining = (remaining - periodicAmount).coerceAtLeast(0.0)
            }
            totalInterest += interest
        }
        if (remaining > 0.000001) {
            return null
        }
        return LoanPlan(months, input.currentEmi, totalInterest)
    }

    private fun paymentFor(balance: Double, monthlyRate: Double, months: Int): Double {
        if (monthlyRate == 0.0) {
            return balance / months
        }
        val growth = power(1.0 + monthlyRate, months)
        return balance * monthlyRate * growth / (growth - 1.0)
    }

    private fun power(base: Double, exponent: Int): Double {
        var result = 1.0
        repeat(exponent) {
            result *= base
        }
        return result
    }
}
