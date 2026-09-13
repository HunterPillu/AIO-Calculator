package app.prinkal.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.prinkal.calculator.core.formatting.formatCurrency
import app.prinkal.calculator.core.formatting.formatNumber
import app.prinkal.calculator.feature.catalog.CalculatorCatalog
import app.prinkal.calculator.feature.catalog.CalculatorCategory
import app.prinkal.calculator.feature.catalog.CalculatorDefinition
import app.prinkal.calculator.feature.emi.EmiCalculationResult
import app.prinkal.calculator.feature.emi.EmiCalculator
import app.prinkal.calculator.feature.emi.EmiInput
import app.prinkal.calculator.feature.emi.EmiResult
import app.prinkal.calculator.feature.loanprepayment.LoanPrepaymentCalculationResult
import app.prinkal.calculator.feature.loanprepayment.LoanPrepaymentCalculator
import app.prinkal.calculator.feature.loanprepayment.LoanPrepaymentInput
import app.prinkal.calculator.feature.loanprepayment.LoanPrepaymentResult
import app.prinkal.calculator.feature.loanprepayment.PeriodicPrepaymentCalculationResult
import app.prinkal.calculator.feature.loanprepayment.PeriodicPrepaymentResult
import app.prinkal.calculator.feature.loanprepayment.PrepaymentFrequency
import app.prinkal.calculator.feature.lumpsum.LumpsumCalculationResult
import app.prinkal.calculator.feature.lumpsum.LumpsumCalculator
import app.prinkal.calculator.feature.lumpsum.LumpsumInput
import app.prinkal.calculator.feature.lumpsum.LumpsumResult
import app.prinkal.calculator.feature.simplecalculator.AngleMode
import app.prinkal.calculator.feature.simplecalculator.EvaluationResult
import app.prinkal.calculator.feature.simplecalculator.ExpressionEvaluator
import app.prinkal.calculator.feature.sip.SipCalculationResult
import app.prinkal.calculator.feature.sip.SipCalculator
import app.prinkal.calculator.feature.sip.SipInput
import app.prinkal.calculator.feature.sip.SipResult
import app.prinkal.calculator.ui.CalculatorTheme

private sealed interface AppDestination {
    data object Home : AppDestination
    data class Calculator(val id: String) : AppDestination
    data class EmiResultRoute(val value: EmiResult) : AppDestination
    data class LoanResultRoute(val value: LoanResultBundle) : AppDestination
    data class SipResultRoute(val value: SipResult) : AppDestination
    data class LumpsumResultRoute(val value: LumpsumResult) : AppDestination
}

private data class LoanResultBundle(
    val comparison: LoanPrepaymentResult?,
    val periodic: PeriodicPrepaymentResult?
)

@Composable
fun App() {
    CalculatorTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CalculatorApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorApp() {
    var destination by remember { mutableStateOf<AppDestination>(AppDestination.Home) }

    AnimatedContent(
        targetState = destination,
        transitionSpec = {
            (fadeIn() + slideInHorizontally { it / 5 }) togetherWith
                (fadeOut() + slideOutHorizontally { -it / 5 })
        },
        label = "calculator route transition"
    ) { target ->
        when (target) {
            AppDestination.Home -> CalculatorHome(
                onOpen = { destination = AppDestination.Calculator(it) }
            )
            is AppDestination.Calculator -> when (target.id) {
                "simple" -> SimpleCalculatorScreen(
                    onBack = { destination = AppDestination.Home }
                )
                "scientific" -> ScientificCalculatorScreen(
                    onBack = { destination = AppDestination.Home }
                )
                "emi" -> EmiCalculatorScreen(
                    onBack = { destination = AppDestination.Home },
                    onResult = { destination = AppDestination.EmiResultRoute(it) }
                )
                "loan-prepayment" -> LoanPrepaymentScreen(
                    onBack = { destination = AppDestination.Home },
                    onResult = {
                        destination = AppDestination.LoanResultRoute(it)
                    }
                )
                "sip" -> SipCalculatorScreen(
                    onBack = { destination = AppDestination.Home },
                    onResult = { destination = AppDestination.SipResultRoute(it) }
                )
                "lumpsum" -> LumpsumCalculatorScreen(
                    onBack = { destination = AppDestination.Home },
                    onResult = { destination = AppDestination.LumpsumResultRoute(it) }
                )
            }
            is AppDestination.EmiResultRoute -> EmiResultScreen(
                result = target.value,
                onBack = { destination = AppDestination.Calculator("emi") }
            )
            is AppDestination.LoanResultRoute -> LoanResultScreen(
                result = target.value,
                onBack = { destination = AppDestination.Calculator("loan-prepayment") }
            )
            is AppDestination.SipResultRoute -> SipResultScreen(
                result = target.value,
                onBack = { destination = AppDestination.Calculator("sip") }
            )
            is AppDestination.LumpsumResultRoute -> LumpsumResultScreen(
                result = target.value,
                onBack = { destination = AppDestination.Calculator("lumpsum") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorHome(onOpen: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CalculatorCategory?>(null) }
    val calculators = CalculatorCatalog.search(query, selectedCategory)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calculator", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = padding.calculateTopPadding() + 12.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HomeHero()
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search calculators") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") },
                        leadingIcon = if (selectedCategory == null) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            null
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    CalculatorCategory.values().forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            label = { Text(category.label) }
                        )
                    }
                }
            }
            item {
                Text(
                    if (query.isBlank()) "Popular calculators" else "Search results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (calculators.isEmpty()) {
                item {
                    Text(
                        "No calculators match your search.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(calculators, key = { it.id }) { definition ->
                    CalculatorCard(definition, onClick = { onOpen(definition.id) })
                }
            }
        }
    }
}

@Composable
private fun HomeHero() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Make every number simple",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Fast, clear tools for everyday math and money decisions.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CalculatorCard(
    definition: CalculatorDefinition,
    onClick: () -> Unit
) {
    val icon = when (definition.id) {
        "simple" -> Icons.Default.Calculate
        "scientific" -> Icons.Default.Functions
        "emi" -> Icons.Default.Payments
        "sip" -> Icons.AutoMirrored.Filled.TrendingUp
        "lumpsum" -> Icons.Default.AccountBalance
        else -> Icons.Default.AccountBalance
    }
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = definition.name,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    definition.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    definition.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorScaffold(
    title: String,
    description: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = padding.calculateTopPadding() + 4.dp,
                        bottom = 24.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                content(PaddingValues(0.dp))
            }
        }
    )
}

@Composable
private fun SimpleCalculatorScreen(onBack: () -> Unit) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun clearResult() {
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

    CalculatorScaffold(
        title = "Simple calculator",
        description = "Arithmetic with precedence, parentheses, and percentages.",
        onBack = onBack
    ) {
        ExpressionDisplay(expression, result, error)
        Keypad(
            onClear = { expression = ""; clearResult() },
            onBackspace = {
                expression = if (expression.isEmpty()) "" else expression.dropLast(1)
                clearResult()
            },
            onToggleSign = {
                expression = toggleSign(expression)
                clearResult()
            },
            onAppend = {
                expression += it
                clearResult()
            },
            onEvaluate = ::evaluate
        )
    }
}

@Composable
private fun ExpressionDisplay(
    expression: String,
    result: Double?,
    error: String?
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
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

    CalculatorScaffold(
        title = "Scientific calculator",
        description = "Trigonometry, logarithms, powers, constants, and factorials.",
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilterChip(
                selected = angleMode == AngleMode.RADIANS,
                onClick = {
                    angleMode = if (angleMode == AngleMode.DEGREES) {
                        AngleMode.RADIANS
                    } else {
                        AngleMode.DEGREES
                    }
                    clearResult()
                },
                label = { Text(if (angleMode == AngleMode.DEGREES) "DEG" else "RAD") }
            )
        }
        ExpressionDisplay(expression, result, error)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        CalculatorKey(
                            label = label,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (label) {
                                    "AC" -> {
                                        expression = ""
                                        clearResult()
                                    }
                                    "⌫" -> {
                                        expression = if (expression.isEmpty()) "" else expression.dropLast(1)
                                        clearResult()
                                    }
                                    "+/-" -> {
                                        expression = toggleSign(expression)
                                        clearResult()
                                    }
                                    "=" -> evaluate()
                                    else -> {
                                        expression += label
                                        clearResult()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmiCalculatorScreen(
    onBack: () -> Unit,
    onResult: (EmiResult) -> Unit
) {
    var loanAmount by remember { mutableStateOf("1000000") }
    var interestRate by remember { mutableStateOf("10") }
    var tenure by remember { mutableStateOf("60") }
    var tenureInYears by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val months = tenure.toIntOrNull()?.let { value ->
            if (tenureInYears) value * 12 else value
        } ?: 0
        when (
            val calculation = EmiCalculator.calculate(
                EmiInput(
                    principal = loanAmount.toDoubleOrNull() ?: Double.NaN,
                    annualInterestRatePercent = interestRate.toDoubleOrNull() ?: Double.NaN,
                    tenureMonths = months
                )
            )
        ) {
            is EmiCalculationResult.Success -> {
                error = null
                onResult(calculation.value)
            }
            is EmiCalculationResult.Error -> error = calculation.message
        }
    }

    CalculatorScaffold(
        title = "EMI calculator",
        description = "Estimate monthly payments without losing sight of total interest.",
        onBack = onBack
    ) {
        CalculatorInputField("Loan amount", loanAmount) {
            loanAmount = it
            error = null
        }
        CalculatorInputField("Annual interest rate (%)", interestRate) {
            interestRate = it
            error = null
        }
        CalculatorInputField(
            if (tenureInYears) "Tenure (years)" else "Tenure (months)",
            tenure
        ) {
            tenure = it
            error = null
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    tenureInYears = !tenureInYears
                    error = null
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (tenureInYears) "Switch to months" else "Switch to years")
            }
            Button(
                onClick = ::calculate,
                modifier = Modifier.weight(1f)
            ) {
                Text("Calculate")
            }
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmiResultScreen(
    result: EmiResult,
    onBack: () -> Unit
) {
    ResultScaffold(
        title = "EMI result",
        onBack = onBack
    ) {
        AnimatedVisibility(visible = true, enter = fadeIn()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text("Monthly EMI", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        formatCurrency(result.monthlyEmi),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        SummaryRow("Total principal", formatCurrency(result.totalPrincipal))
        SummaryRow("Total interest", formatCurrency(result.totalInterest))
        SummaryRow("Total payment", formatCurrency(result.totalPayment))
        Text("Amortization schedule", style = MaterialTheme.typography.titleLarge)
        result.schedule.forEach { row ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Month ${row.month}", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Payment ${formatCurrency(row.payment)}  ·  " +
                            "Principal ${formatCurrency(row.principal)}  ·  " +
                            "Interest ${formatCurrency(row.interest)}"
                    )
                    Text(
                        "Remaining ${formatCurrency(row.remainingBalance)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SipCalculatorScreen(
    onBack: () -> Unit,
    onResult: (SipResult) -> Unit
) {
    var monthlyInvestment by remember { mutableStateOf("10000") }
    var annualReturn by remember { mutableStateOf("12") }
    var durationYears by remember { mutableStateOf("10") }
    var annualStepUp by remember { mutableStateOf("0") }
    var error by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        when (
            val calculation = SipCalculator.calculate(
                SipInput(
                    monthlyInvestment = monthlyInvestment.toDoubleOrNull() ?: Double.NaN,
                    annualReturnPercent = annualReturn.toDoubleOrNull() ?: Double.NaN,
                    durationYears = durationYears.toIntOrNull() ?: 0,
                    annualStepUpPercent = annualStepUp.toDoubleOrNull() ?: Double.NaN
                )
            )
        ) {
            is SipCalculationResult.Success -> {
                error = null
                onResult(calculation.value)
            }
            is SipCalculationResult.Error -> error = calculation.message
        }
    }

    CalculatorScaffold(
        title = "SIP calculator",
        description = "Project monthly investments and see how an annual step-up changes growth.",
        onBack = onBack
    ) {
        CalculatorInputField("Monthly SIP", monthlyInvestment) {
            monthlyInvestment = it
            error = null
        }
        CalculatorInputField("Expected annual return (%)", annualReturn) {
            annualReturn = it
            error = null
        }
        CalculatorInputField("Investment duration (years)", durationYears) {
            durationYears = it
            error = null
        }
        CalculatorInputField("Annual SIP step-up (%)", annualStepUp) {
            annualStepUp = it
            error = null
        }
        Button(onClick = ::calculate, modifier = Modifier.fillMaxWidth()) {
            Text("Calculate SIP")
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SipResultScreen(
    result: SipResult,
    onBack: () -> Unit
) {
    ResultScaffold(title = "SIP result", onBack = onBack) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Final value", color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    formatCurrency(result.finalValue),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        SummaryRow("Total invested", formatCurrency(result.totalInvested))
        SummaryRow("Estimated returns", formatCurrency(result.estimatedReturns))
        Text("Yearly growth", style = MaterialTheme.typography.titleLarge)
        result.yearlyGrowth.forEach { growth ->
            ResultCard(
                title = "Year ${growth.year}",
                value = formatCurrency(growth.value),
                detail = "Invested ${formatCurrency(growth.invested)} · " +
                    "Returns ${formatCurrency(growth.estimatedReturns)}"
            )
        }
    }
}

@Composable
private fun LumpsumCalculatorScreen(
    onBack: () -> Unit,
    onResult: (LumpsumResult) -> Unit
) {
    var initialInvestment by remember { mutableStateOf("100000") }
    var annualReturn by remember { mutableStateOf("12") }
    var durationYears by remember { mutableStateOf("10") }
    var error by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        when (
            val calculation = LumpsumCalculator.calculate(
                LumpsumInput(
                    initialInvestment = initialInvestment.toDoubleOrNull() ?: Double.NaN,
                    annualReturnPercent = annualReturn.toDoubleOrNull() ?: Double.NaN,
                    durationYears = durationYears.toIntOrNull() ?: 0
                )
            )
        ) {
            is LumpsumCalculationResult.Success -> {
                error = null
                onResult(calculation.value)
            }
            is LumpsumCalculationResult.Error -> error = calculation.message
        }
    }

    CalculatorScaffold(
        title = "Lumpsum calculator",
        description = "Project a one-time investment using annual compounding.",
        onBack = onBack
    ) {
        CalculatorInputField("Initial investment", initialInvestment) {
            initialInvestment = it
            error = null
        }
        CalculatorInputField("Expected annual return (%)", annualReturn) {
            annualReturn = it
            error = null
        }
        CalculatorInputField("Investment duration (years)", durationYears) {
            durationYears = it
            error = null
        }
        Button(onClick = ::calculate, modifier = Modifier.fillMaxWidth()) {
            Text("Calculate lumpsum")
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun LumpsumResultScreen(
    result: LumpsumResult,
    onBack: () -> Unit
) {
    ResultScaffold(title = "Lumpsum result", onBack = onBack) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Final value", color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    formatCurrency(result.finalValue),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        SummaryRow("Invested amount", formatCurrency(result.investedAmount))
        SummaryRow("Estimated returns", formatCurrency(result.estimatedReturns))
        Text("Yearly growth", style = MaterialTheme.typography.titleLarge)
        result.yearlyGrowth.forEach { growth ->
            ResultCard(
                title = "Year ${growth.year}",
                value = formatCurrency(growth.value),
                detail = "Estimated returns ${formatCurrency(growth.estimatedReturns)}"
            )
        }
    }
}

@Composable
private fun LoanPrepaymentScreen(
    onBack: () -> Unit,
    onResult: (LoanResultBundle) -> Unit
) {
    var principal by remember { mutableStateOf("1000000") }
    var rate by remember { mutableStateOf("10") }
    var emi by remember { mutableStateOf("21247.04") }
    var tenure by remember { mutableStateOf("60") }
    var prepayment by remember { mutableStateOf("100000") }
    var periodicPrepayment by remember { mutableStateOf("10000") }
    var frequencyIndex by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    fun input() = LoanPrepaymentInput(
        outstandingPrincipal = principal.toDoubleOrNull() ?: Double.NaN,
        annualInterestRatePercent = rate.toDoubleOrNull() ?: Double.NaN,
        currentEmi = emi.toDoubleOrNull() ?: Double.NaN,
        remainingTenureMonths = tenure.toIntOrNull() ?: 0,
        prepaymentAmount = prepayment.toDoubleOrNull() ?: Double.NaN
    )

    fun calculate() {
        val comparison = LoanPrepaymentCalculator.calculate(input())
        val periodic = LoanPrepaymentCalculator.calculatePeriodic(
            input = input(),
            periodicAmount = periodicPrepayment.toDoubleOrNull() ?: Double.NaN,
            frequency = PrepaymentFrequency.entries[frequencyIndex]
        )
        val comparisonValue = (comparison as? LoanPrepaymentCalculationResult.Success)?.value
        val periodicValue = (periodic as? PeriodicPrepaymentCalculationResult.Success)?.value
        if (comparisonValue != null || periodicValue != null) {
            error = null
            onResult(LoanResultBundle(comparisonValue, periodicValue))
        } else {
            error = (comparison as LoanPrepaymentCalculationResult.Error).message
        }
    }

    CalculatorScaffold(
        title = "Loan prepayment",
        description = "Compare reducing tenure, reducing EMI, or paying extra periodically.",
        onBack = onBack
    ) {
        CalculatorInputField("Outstanding principal", principal) {
            principal = it
            error = null
        }
        CalculatorInputField("Annual interest rate (%)", rate) {
            rate = it
            error = null
        }
        CalculatorInputField("Current EMI", emi) {
            emi = it
            error = null
        }
        CalculatorInputField("Remaining tenure (months)", tenure) {
            tenure = it
            error = null
        }
        CalculatorInputField("One-time prepayment amount", prepayment) {
            prepayment = it
            error = null
        }
        CalculatorInputField("Periodic prepayment amount", periodicPrepayment) {
            periodicPrepayment = it
            error = null
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    frequencyIndex =
                        (frequencyIndex + 1) % PrepaymentFrequency.entries.size
                    error = null
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(PrepaymentFrequency.entries[frequencyIndex].label)
            }
            Button(onClick = ::calculate, modifier = Modifier.weight(1f)) {
                Text("Compare")
            }
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun LoanResultScreen(
    result: LoanResultBundle,
    onBack: () -> Unit
) {
    ResultScaffold(title = "Loan prepayment result", onBack = onBack) {
        result.comparison?.let { comparison ->
            Text("One-time prepayment", style = MaterialTheme.typography.titleLarge)
            SummaryRow("Original tenure", "${comparison.original.months} months")
            ResultCard(
                title = "Reduce tenure",
                value = "${comparison.reduceTenure.months} months",
                detail = "Interest saved ${formatCurrency(comparison.reduceTenureInterestSaved)}"
            )
            ResultCard(
                title = "Reduce EMI",
                value = formatCurrency(comparison.reduceEmi.emi),
                detail = "Interest saved ${formatCurrency(comparison.reduceEmiInterestSaved)}"
            )
        }
        result.periodic?.let { periodic ->
            Text("Periodic prepayment", style = MaterialTheme.typography.titleLarge)
            ResultCard(
                title = "New payoff plan",
                value = "${periodic.afterPrepayments.months} months",
                detail = "Interest saved ${formatCurrency(periodic.interestSaved)}"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = padding.calculateTopPadding() + 4.dp,
                    bottom = 28.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ResultCard(title: String, value: String, detail: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CalculatorInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { label ->
                    CalculatorKey(
                        label = label,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(if (row.size == 2) 2.1f else 1.35f),
                        onClick = {
                            when (label) {
                                "AC" -> onClear()
                                "⌫" -> onBackspace()
                                "+/-" -> onToggleSign()
                                "=" -> onEvaluate()
                                else -> onAppend(label)
                            }
                        }
                    )
                }
                if (row.size == 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CalculatorKey(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val isEquals = label == "="
    val isAction = label == "AC" || label == "⌫"
    val colors = if (isEquals) {
        ButtonDefaults.buttonColors()
    } else if (isAction) {
        ButtonDefaults.filledTonalButtonColors()
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = colors,
        contentPadding = PaddingValues(4.dp)
    ) {
        when (label) {
            "⌫" -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
            "AC" -> Icon(Icons.Default.Delete, contentDescription = "Clear")
            else -> Text(label, fontSize = 18.sp)
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
