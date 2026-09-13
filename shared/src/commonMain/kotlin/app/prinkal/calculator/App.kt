package app.prinkal.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.prinkal.calculator.feature.catalog.CalculatorCatalog
import app.prinkal.calculator.feature.catalog.CalculatorDefinition
import app.prinkal.calculator.feature.emi.EmiCalculationResult
import app.prinkal.calculator.feature.emi.EmiCalculator
import app.prinkal.calculator.feature.emi.EmiInput
import app.prinkal.calculator.feature.loanprepayment.LoanPrepaymentCalculationResult
import app.prinkal.calculator.feature.loanprepayment.LoanPrepaymentCalculator
import app.prinkal.calculator.feature.loanprepayment.LoanPrepaymentInput
import app.prinkal.calculator.feature.loanprepayment.PeriodicPrepaymentCalculationResult
import app.prinkal.calculator.feature.loanprepayment.PrepaymentFrequency
import app.prinkal.calculator.feature.simplecalculator.AngleMode
import app.prinkal.calculator.feature.simplecalculator.EvaluationResult
import app.prinkal.calculator.feature.simplecalculator.ExpressionEvaluator

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CalculatorHome()
        }
    }
}

@Composable
private fun CalculatorHome() {
    var query by remember { mutableStateOf("") }
    var selectedCalculator by remember { mutableStateOf<String?>(null) }

    selectedCalculator?.let { id ->
        when (id) {
            "simple" -> SimpleCalculatorScreen(onBack = { selectedCalculator = null })
            "scientific" -> ScientificCalculatorScreen(onBack = { selectedCalculator = null })
            "emi" -> EmiCalculatorScreen(onBack = { selectedCalculator = null })
            "loan-prepayment" -> LoanPrepaymentScreen(onBack = { selectedCalculator = null })
        }
        return
    }

    val calculators = CalculatorCatalog.search(query)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("All-in-One Calculator", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Choose a calculator",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search calculators") },
            singleLine = true
        )
        Text("Popular", style = MaterialTheme.typography.titleMedium)
        calculators.filter { it.isPopular }.forEach { definition ->
            CalculatorCard(definition) { selectedCalculator = definition.id }
        }
        Text("All calculators", style = MaterialTheme.typography.titleMedium)
        calculators.forEach { definition ->
            CalculatorCard(definition) { selectedCalculator = definition.id }
        }
        if (calculators.isEmpty()) {
            Text(
                "No calculators match your search.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalculatorCard(definition: CalculatorDefinition, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(definition.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                definition.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SimpleCalculatorScreen(onBack: () -> Unit) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun append(value: String) {
        expression += value
        result = null
        error = null
    }

    fun evaluate() {
        when (val evaluation = ExpressionEvaluator.evaluate(expression)) {
            is EvaluationResult.Success -> {
                result = evaluation.value
                error = null
            }
            is EvaluationResult.Error -> {
                result = null
                error = evaluation.message
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onBack) { Text("Back") }
        Text("Simple calculator", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Arithmetic with precedence, parentheses, and percentages.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.End,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = result?.let { "= ${formatNumber(it)}" } ?: (error ?: ""),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (error == null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    textAlign = TextAlign.End
                )
            }
        }

        Keypad(
            onClear = {
                expression = ""
                result = null
                error = null
            },
            onBackspace = {
                expression = if (expression.isEmpty()) "" else expression.substring(0, expression.length - 1)
                result = null
                error = null
            },
            onToggleSign = {
                expression = toggleSign(expression)
                result = null
                error = null
            },
            onAppend = ::append,
            onEvaluate = ::evaluate
        )
    }
}

@Composable
private fun ScientificCalculatorScreen(onBack: () -> Unit) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var angleMode by remember { mutableStateOf(AngleMode.DEGREES) }

    fun clearResult() {
        result = null
        error = null
    }

    fun append(value: String) {
        expression += value
        clearResult()
    }

    fun evaluate() {
        when (val evaluation = ExpressionEvaluator.evaluate(expression, angleMode)) {
            is EvaluationResult.Success -> {
                result = evaluation.value
                error = null
            }
            is EvaluationResult.Error -> {
                result = null
                error = evaluation.message
            }
        }
    }

    val rows = listOf(
        listOf("sin(", "cos(", "tan(", ")"),
        listOf("asin(", "acos(", "atan(", "("),
        listOf("log(", "ln(", "sqrt(", "square("),
        listOf("reciprocal(", "^", "!", "%"),
        listOf("π", "e"),
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf("(", "0", ".", "+"),
        listOf("AC", "⌫", "+/-", "=")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack) { Text("Back") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Scientific calculator", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = {
                angleMode = if (angleMode == AngleMode.DEGREES) {
                    AngleMode.RADIANS
                } else {
                    AngleMode.DEGREES
                }
                clearResult()
            }) {
                Text(if (angleMode == AngleMode.DEGREES) "DEG" else "RAD")
            }
        }
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.End,
                    maxLines = 3
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    result?.let { "= ${formatNumber(it)}" } ?: (error ?: ""),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (error == null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    textAlign = TextAlign.End
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        Button(
                            onClick = {
                                when (label) {
                                    "AC" -> {
                                        expression = ""
                                        clearResult()
                                    }
                                    "⌫" -> {
                                        expression = if (expression.isEmpty()) "" else expression.substring(
                                            0,
                                            expression.length - 1
                                        )
                                        clearResult()
                                    }
                                    "+/-" -> {
                                        expression = toggleSign(expression)
                                        clearResult()
                                    }
                                    "=" -> evaluate()
                                    else -> append(label)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(label, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmiCalculatorScreen(onBack: () -> Unit) {
    var loanAmount by remember { mutableStateOf("1000000") }
    var interestRate by remember { mutableStateOf("10") }
    var tenure by remember { mutableStateOf("60") }
    var tenureInYears by remember { mutableStateOf(false) }
    var calculation by remember { mutableStateOf<EmiCalculationResult?>(null) }

    fun calculate() {
        val months = tenure.toIntOrNull()?.let { value ->
            if (tenureInYears) value * 12 else value
        } ?: 0
        calculation = EmiCalculator.calculate(
            EmiInput(
                principal = loanAmount.toDoubleOrNull() ?: Double.NaN,
                annualInterestRatePercent = interestRate.toDoubleOrNull() ?: Double.NaN,
                tenureMonths = months
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack) { Text("Back") }
        Text("EMI calculator", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Estimate monthly payments and review the amortization schedule.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = loanAmount,
            onValueChange = { loanAmount = it; calculation = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Loan amount") },
            singleLine = true
        )
        OutlinedTextField(
            value = interestRate,
            onValueChange = { interestRate = it; calculation = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Annual interest rate (%)") },
            singleLine = true
        )
        OutlinedTextField(
            value = tenure,
            onValueChange = { tenure = it; calculation = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (tenureInYears) "Tenure (years)" else "Tenure (months)") },
            singleLine = true
        )
        Button(onClick = { tenureInYears = !tenureInYears; calculation = null }) {
            Text(if (tenureInYears) "Use months" else "Use years")
        }
        Button(
            onClick = ::calculate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate EMI")
        }
        when (val result = calculation) {
            is EmiCalculationResult.Error -> Text(
                result.message,
                color = MaterialTheme.colorScheme.error
            )
            is EmiCalculationResult.Success -> {
                val value = result.value
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Monthly EMI", style = MaterialTheme.typography.titleMedium)
                        Text("₹${formatNumber(value.monthlyEmi)}", style = MaterialTheme.typography.headlineSmall)
                        Text("Total principal: ₹${formatNumber(value.totalPrincipal)}")
                        Text("Total interest: ₹${formatNumber(value.totalInterest)}")
                        Text("Total payment: ₹${formatNumber(value.totalPayment)}")
                    }
                }
                Text("Amortization schedule", style = MaterialTheme.typography.titleMedium)
                value.schedule.take(12).forEach { row ->
                    Text(
                        "Month ${row.month}: EMI ₹${formatNumber(row.payment)} · " +
                            "Principal ₹${formatNumber(row.principal)} · " +
                            "Interest ₹${formatNumber(row.interest)}"
                    )
                }
                if (value.schedule.size > 12) {
                    Text(
                        "Showing the first 12 of ${value.schedule.size} months.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            null -> Unit
        }
    }
}

@Composable
private fun LoanPrepaymentScreen(onBack: () -> Unit) {
    var principal by remember { mutableStateOf("1000000") }
    var rate by remember { mutableStateOf("10") }
    var emi by remember { mutableStateOf("21247.04") }
    var tenure by remember { mutableStateOf("60") }
    var prepayment by remember { mutableStateOf("100000") }
    var periodicPrepayment by remember { mutableStateOf("10000") }
    var frequencyIndex by remember { mutableStateOf(0) }
    var calculation by remember { mutableStateOf<LoanPrepaymentCalculationResult?>(null) }
    var periodicCalculation by remember {
        mutableStateOf<PeriodicPrepaymentCalculationResult?>(null)
    }

    fun calculate() {
        calculation = LoanPrepaymentCalculator.calculate(
            LoanPrepaymentInput(
                outstandingPrincipal = principal.toDoubleOrNull() ?: Double.NaN,
                annualInterestRatePercent = rate.toDoubleOrNull() ?: Double.NaN,
                currentEmi = emi.toDoubleOrNull() ?: Double.NaN,
                remainingTenureMonths = tenure.toIntOrNull() ?: 0,
                prepaymentAmount = prepayment.toDoubleOrNull() ?: Double.NaN
            )
        )
        periodicCalculation = LoanPrepaymentCalculator.calculatePeriodic(
            input = LoanPrepaymentInput(
                outstandingPrincipal = principal.toDoubleOrNull() ?: Double.NaN,
                annualInterestRatePercent = rate.toDoubleOrNull() ?: Double.NaN,
                currentEmi = emi.toDoubleOrNull() ?: Double.NaN,
                remainingTenureMonths = tenure.toIntOrNull() ?: 0,
                prepaymentAmount = prepayment.toDoubleOrNull() ?: Double.NaN
            ),
            periodicAmount = periodicPrepayment.toDoubleOrNull() ?: Double.NaN,
            frequency = PrepaymentFrequency.values()[frequencyIndex]
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack) { Text("Back") }
        Text("Loan prepayment", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Compare keeping the EMI and shortening the tenure with keeping the tenure and lowering the EMI.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LoanInputField("Outstanding principal", principal) {
            principal = it
            calculation = null
            periodicCalculation = null
        }
        LoanInputField("Annual interest rate (%)", rate) {
            rate = it
            calculation = null
            periodicCalculation = null
        }
        LoanInputField("Current EMI", emi) {
            emi = it
            calculation = null
            periodicCalculation = null
        }
        LoanInputField("Remaining tenure (months)", tenure) {
            tenure = it
            calculation = null
            periodicCalculation = null
        }
        LoanInputField("One-time prepayment amount", prepayment) {
            prepayment = it
            calculation = null
            periodicCalculation = null
        }
        LoanInputField("Periodic prepayment amount", periodicPrepayment) {
            periodicPrepayment = it
            calculation = null
            periodicCalculation = null
        }
        Button(onClick = {
            frequencyIndex = (frequencyIndex + 1) % PrepaymentFrequency.values().size
            calculation = null
            periodicCalculation = null
        }) {
            Text("Frequency: ${PrepaymentFrequency.values()[frequencyIndex].label}")
        }
        Button(onClick = ::calculate, modifier = Modifier.fillMaxWidth()) {
            Text("Compare options")
        }
        when (val result = calculation) {
            is LoanPrepaymentCalculationResult.Error -> Text(
                result.message,
                color = MaterialTheme.colorScheme.error
            )
            is LoanPrepaymentCalculationResult.Success -> {
                val value = result.value
                LoanComparisonCard(
                    title = "Reduce tenure",
                    detail = "${value.reduceTenure.months} months · " +
                        "Interest saved ₹${formatNumber(value.reduceTenureInterestSaved)}"
                )
                LoanComparisonCard(
                    title = "Reduce EMI",
                    detail = "New EMI ₹${formatNumber(value.reduceEmi.emi)} · " +
                        "Interest saved ₹${formatNumber(value.reduceEmiInterestSaved)}"
                )
            }
            null -> Unit
        }
        when (val result = periodicCalculation) {
            is PeriodicPrepaymentCalculationResult.Error -> Text(
                result.message,
                color = MaterialTheme.colorScheme.error
            )
            is PeriodicPrepaymentCalculationResult.Success -> {
                val value = result.value
                LoanComparisonCard(
                    title = "Periodic prepayment",
                    detail = "${value.afterPrepayments.months} months · " +
                        "Interest saved ₹${formatNumber(value.interestSaved)}"
                )
            }
            null -> Unit
        }
    }
}

@Composable
private fun LoanInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true
    )
}

@Composable
private fun LoanComparisonCard(title: String, detail: String) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(detail)
        }
    }
}

@Composable
private fun Keypad(
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onToggleSign: () -> Unit,
    onAppend: (String) -> Unit,
    onEvaluate: () -> Unit
) {
    val rows = listOf(
        listOf("AC", "⌫", "(", ")"),
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf("+/-", "0", ".", "+"),
        listOf("%", "=")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { label ->
                    Button(
                        onClick = {
                            when (label) {
                                "AC" -> onClear()
                                "⌫" -> onBackspace()
                                "+/-" -> onToggleSign()
                                "=" -> onEvaluate()
                                else -> onAppend(label)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(if (row.size == 2) 2.1f else 1.35f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(label, fontSize = 19.sp)
                    }
                }
                if (row.size == 2) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

private fun toggleSign(expression: String): String {
    if (expression.isEmpty()) return "-"

    var start = expression.length
    while (start > 0 && (expression[start - 1].isDigit() || expression[start - 1] == '.')) {
        start--
    }
    if (start == expression.length) return "$expression-"

    if (start > 0 &&
        expression[start - 1] == '-' &&
        (start == 1 || expression[start - 2] in "+-×÷*/(")
    ) {
        return expression.removeRange(start - 1, start)
    }

    return expression.substring(0, start) + "-" + expression.substring(start)
}

private fun formatNumber(value: Double): String {
    if (value == 0.0) return "0"
    if (kotlin.math.abs(value) < 1_000_000_000_000_000.0 &&
        value == value.toLong().toDouble()
    ) {
        return value.toLong().toString()
    }
    return value.toString()
}
