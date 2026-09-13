package app.prinkal.calculator.feature.catalog

enum class CalculatorCategory(val label: String) {
    MATHEMATICS("Mathematics"),
    LOANS("Loans")
}

data class CalculatorDefinition(
    val id: String,
    val name: String,
    val description: String,
    val category: CalculatorCategory,
    val isPopular: Boolean
)

object CalculatorCatalog {
    val calculators: List<CalculatorDefinition> = listOf(
        CalculatorDefinition(
            id = "simple",
            name = "Simple Calculator",
            description = "Everyday arithmetic with parentheses and percentages.",
            category = CalculatorCategory.MATHEMATICS,
            isPopular = true
        ),
        CalculatorDefinition(
            id = "scientific",
            name = "Scientific Calculator",
            description = "Trigonometry, logarithms, powers, constants, and factorials.",
            category = CalculatorCategory.MATHEMATICS,
            isPopular = true
        ),
        CalculatorDefinition(
            id = "emi",
            name = "EMI Calculator",
            description = "Monthly loan payment, interest, total payment, and amortization.",
            category = CalculatorCategory.LOANS,
            isPopular = true
        ),
        CalculatorDefinition(
            id = "loan-prepayment",
            name = "Loan Prepayment",
            description = "Compare reducing loan tenure or reducing the EMI after prepayment.",
            category = CalculatorCategory.LOANS,
            isPopular = false
        )
    )

    fun search(query: String, category: CalculatorCategory? = null): List<CalculatorDefinition> {
        val normalizedQuery = query.trim().lowercase()
        return calculators.filter { calculator ->
            (category == null || calculator.category == category) &&
                (normalizedQuery.isEmpty() ||
                    calculator.name.lowercase().contains(normalizedQuery) ||
                    calculator.description.lowercase().contains(normalizedQuery))
        }
    }
}
