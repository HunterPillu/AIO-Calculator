package app.prinkal.calculator.feature.catalog

import app.prinkal.calculator.resources.*
import org.jetbrains.compose.resources.StringResource

enum class CalculatorCategory(val labelRes: StringResource) {
    MATHEMATICS(Res.string.category_mathematics),
    LOANS(Res.string.category_loans),
    INVESTMENTS(Res.string.category_investments)
}

data class CalculatorDefinition(
    val id: String,
    val nameRes: StringResource,
    val descriptionRes: StringResource,
    val category: CalculatorCategory,
    val isPopular: Boolean
)

object CalculatorCatalog {
    val calculators: List<CalculatorDefinition> = listOf(
        CalculatorDefinition(
            id = "simple",
            nameRes = Res.string.simple_calculator_name,
            descriptionRes = Res.string.simple_calculator_description,
            category = CalculatorCategory.MATHEMATICS,
            isPopular = true
        ),
        CalculatorDefinition(
            id = "scientific",
            nameRes = Res.string.scientific_calculator_name,
            descriptionRes = Res.string.scientific_calculator_description,
            category = CalculatorCategory.MATHEMATICS,
            isPopular = true
        ),
        CalculatorDefinition(
            id = "emi",
            nameRes = Res.string.emi_calculator_name,
            descriptionRes = Res.string.emi_calculator_description,
            category = CalculatorCategory.LOANS,
            isPopular = true
        ),
        CalculatorDefinition(
            id = "loan-prepayment",
            nameRes = Res.string.loan_prepayment_name,
            descriptionRes = Res.string.loan_prepayment_description,
            category = CalculatorCategory.LOANS,
            isPopular = false
        ),
        CalculatorDefinition(
            id = "sip",
            nameRes = Res.string.sip_calculator_name,
            descriptionRes = Res.string.sip_calculator_description,
            category = CalculatorCategory.INVESTMENTS,
            isPopular = true
        ),
        CalculatorDefinition(
            id = "lumpsum",
            nameRes = Res.string.lumpsum_calculator_name,
            descriptionRes = Res.string.lumpsum_calculator_description,
            category = CalculatorCategory.INVESTMENTS,
            isPopular = false
        )
    )

    fun search(
        query: String,
        category: CalculatorCategory? = null,
        searchableText: Map<String, String>
    ): List<CalculatorDefinition> {
        val normalizedQuery = query.trim().lowercase()
        return calculators.filter { calculator ->
            (category == null || calculator.category == category) &&
                (normalizedQuery.isEmpty() ||
                    searchableText[calculator.id].orEmpty().lowercase().contains(normalizedQuery))
        }
    }
}
