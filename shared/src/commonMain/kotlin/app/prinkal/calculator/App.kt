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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.prinkal.calculator.core.formatting.formatCurrency
import app.prinkal.calculator.core.formatting.formatNumber
import app.prinkal.calculator.core.app.AppInfo
import app.prinkal.calculator.core.logging.AppLogger
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
import app.prinkal.calculator.resources.*
import app.prinkal.calculator.ui.CalculatorTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private sealed interface AppDestination {
    data object Home : AppDestination
    data object Settings : AppDestination
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
                onOpen = {
                    AppLogger.calculatorOpened(it)
                    destination = AppDestination.Calculator(it)
                },
                onSettings = {
                    AppLogger.settingsOpened()
                    destination = AppDestination.Settings
                }
            )
            AppDestination.Settings -> SettingsScreen(
                onBack = { destination = AppDestination.Home }
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
private fun CalculatorHome(
    onOpen: (String) -> Unit,
    onSettings: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CalculatorCategory?>(null) }
    val searchableText = CalculatorCatalog.calculators.associate { definition ->
        definition.id to "${stringResource(definition.nameRes)} " +
            stringResource(definition.descriptionRes)
    }
    val calculators = CalculatorCatalog.search(query, selectedCategory, searchableText)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(Res.string.app_title), fontWeight = FontWeight.SemiBold)
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(Res.string.accessibility_settings)
                        )
                    }
                },
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
                    label = { Text(stringResource(Res.string.search_calculators)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(
                                        Res.string.accessibility_clear_search
                                    )
                                )
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
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text(stringResource(Res.string.all)) },
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
                    }
                    items(items = CalculatorCategory.entries.toList(), key = { it.name }) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            label = { Text(stringResource(category.labelRes)) }
                        )
                    }
                }
            }
            item {
                Text(
                    stringResource(
                        if (query.isBlank()) {
                            Res.string.popular_calculators
                        } else {
                            Res.string.search_results
                        }
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (calculators.isEmpty()) {
                item {
                    Text(
                        stringResource(Res.string.no_calculators_match),
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
                    stringResource(Res.string.home_hero_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.home_hero_description),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    ResultScaffold(title = stringResource(Res.string.settings), onBack = onBack) {
        Text(stringResource(Res.string.settings_app), style = MaterialTheme.typography.titleLarge)
        SettingsInfoCard(
            icon = Icons.Default.Info,
            title = stringResource(Res.string.app_name),
            body = stringResource(
                Res.string.version_info,
                AppInfo.versionName,
                AppInfo.versionCode
            )
        )
        Text(
            stringResource(Res.string.about_developer),
            style = MaterialTheme.typography.titleLarge
        )
        SettingsInfoCard(
            icon = Icons.Default.AccountBalance,
            title = stringResource(Res.string.developer_name),
            body = stringResource(Res.string.developer_description)
        )
        Text(stringResource(Res.string.privacy), style = MaterialTheme.typography.titleLarge)
        SettingsInfoCard(
            icon = Icons.Default.Settings,
            title = stringResource(Res.string.offline_first_title),
            body = stringResource(Res.string.offline_first_description)
        )
        Text(
            stringResource(Res.string.important_information),
            style = MaterialTheme.typography.titleLarge
        )
        SettingsInfoCard(
            icon = Icons.Default.Info,
            title = stringResource(Res.string.financial_disclaimer_title),
            body = stringResource(Res.string.financial_disclaimer_description)
        )
        Text(
            stringResource(Res.string.open_source_components),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            stringResource(Res.string.open_source_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsInfoCard(
    icon: ImageVector,
    title: String,
    body: String
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    contentDescription = stringResource(definition.nameRes),
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(definition.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(definition.descriptionRes),
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.accessibility_back)
                        )
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
                AppLogger.calculationSucceeded("simple")
            }
            is EvaluationResult.Error -> {
                result = null
                error = evaluation.message
                AppLogger.calculationFailed("simple")
            }
        }
    }

    CalculatorScaffold(
        title = stringResource(Res.string.simple_calculator_title),
        description = stringResource(Res.string.simple_calculator_screen_description),
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
                AppLogger.calculationSucceeded("scientific")
            }
            is EvaluationResult.Error -> {
                result = null
                error = evaluation.message
                AppLogger.calculationFailed("scientific")
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
        title = stringResource(Res.string.scientific_calculator_title),
        description = stringResource(Res.string.scientific_calculator_screen_description),
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
                label = {
                    Text(
                        stringResource(
                            if (angleMode == AngleMode.DEGREES) {
                                Res.string.degrees
                            } else {
                                Res.string.radians
                            }
                        )
                    )
                }
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
                AppLogger.calculationSucceeded("emi")
                onResult(calculation.value)
            }
            is EmiCalculationResult.Error -> {
                error = calculation.message
                AppLogger.calculationFailed("emi")
            }
        }
    }

    CalculatorScaffold(
        title = stringResource(Res.string.emi_calculator_title),
        description = stringResource(Res.string.emi_calculator_screen_description),
        onBack = onBack
    ) {
        CalculatorInputField(stringResource(Res.string.loan_amount), loanAmount) {
            loanAmount = it
            error = null
        }
        CalculatorInputField(stringResource(Res.string.annual_interest_rate), interestRate) {
            interestRate = it
            error = null
        }
        CalculatorInputField(
            stringResource(
                if (tenureInYears) Res.string.tenure_years else Res.string.tenure_months
            ),
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
                Text(
                    stringResource(
                        if (tenureInYears) Res.string.switch_to_months else Res.string.switch_to_years
                    )
                )
            }
            Button(
                onClick = ::calculate,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(Res.string.calculate))
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
        title = stringResource(Res.string.emi_result_title),
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
                    Text(
                        stringResource(Res.string.monthly_emi),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        formatCurrency(result.monthlyEmi),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        SummaryRow(
            stringResource(Res.string.total_principal),
            formatCurrency(result.totalPrincipal)
        )
        SummaryRow(
            stringResource(Res.string.total_interest),
            formatCurrency(result.totalInterest)
        )
        SummaryRow(
            stringResource(Res.string.total_payment),
            formatCurrency(result.totalPayment)
        )
        Text(
            stringResource(Res.string.amortization_schedule),
            style = MaterialTheme.typography.titleLarge
        )
        result.schedule.forEach { row ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        stringResource(Res.string.month, row.month),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        stringResource(
                            Res.string.payment_details,
                            formatCurrency(row.payment),
                            formatCurrency(row.principal),
                            formatCurrency(row.interest)
                        )
                    )
                    Text(
                        stringResource(
                            Res.string.remaining_balance,
                            formatCurrency(row.remainingBalance)
                        ),
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
                AppLogger.calculationSucceeded("sip")
                onResult(calculation.value)
            }
            is SipCalculationResult.Error -> {
                error = calculation.message
                AppLogger.calculationFailed("sip")
            }
        }
    }

    CalculatorScaffold(
        title = stringResource(Res.string.sip_calculator_title),
        description = stringResource(Res.string.sip_calculator_screen_description),
        onBack = onBack
    ) {
        CalculatorInputField(stringResource(Res.string.monthly_sip), monthlyInvestment) {
            monthlyInvestment = it
            error = null
        }
        CalculatorInputField(stringResource(Res.string.expected_annual_return), annualReturn) {
            annualReturn = it
            error = null
        }
        CalculatorInputField(
            stringResource(Res.string.investment_duration_years),
            durationYears
        ) {
            durationYears = it
            error = null
        }
        CalculatorInputField(stringResource(Res.string.annual_sip_step_up), annualStepUp) {
            annualStepUp = it
            error = null
        }
        Button(onClick = ::calculate, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.calculate_sip))
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
    ResultScaffold(title = stringResource(Res.string.sip_result_title), onBack = onBack) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    stringResource(Res.string.final_value),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    formatCurrency(result.finalValue),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        SummaryRow(
            stringResource(Res.string.total_invested),
            formatCurrency(result.totalInvested)
        )
        SummaryRow(
            stringResource(Res.string.estimated_returns),
            formatCurrency(result.estimatedReturns)
        )
        Text(
            stringResource(Res.string.yearly_growth),
            style = MaterialTheme.typography.titleLarge
        )
        result.yearlyGrowth.forEach { growth ->
            ResultCard(
                title = stringResource(Res.string.year_title, growth.year),
                value = formatCurrency(growth.value),
                detail = stringResource(
                    Res.string.invested_returns,
                    formatCurrency(growth.invested),
                    formatCurrency(growth.estimatedReturns)
                )
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
                AppLogger.calculationSucceeded("lumpsum")
                onResult(calculation.value)
            }
            is LumpsumCalculationResult.Error -> {
                error = calculation.message
                AppLogger.calculationFailed("lumpsum")
            }
        }
    }

    CalculatorScaffold(
        title = stringResource(Res.string.lumpsum_calculator_title),
        description = stringResource(Res.string.lumpsum_calculator_screen_description),
        onBack = onBack
    ) {
        CalculatorInputField(stringResource(Res.string.initial_investment), initialInvestment) {
            initialInvestment = it
            error = null
        }
        CalculatorInputField(stringResource(Res.string.expected_annual_return), annualReturn) {
            annualReturn = it
            error = null
        }
        CalculatorInputField(
            stringResource(Res.string.investment_duration_years),
            durationYears
        ) {
            durationYears = it
            error = null
        }
        Button(onClick = ::calculate, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.calculate_lumpsum))
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
    ResultScaffold(title = stringResource(Res.string.lumpsum_result_title), onBack = onBack) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    stringResource(Res.string.final_value),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    formatCurrency(result.finalValue),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        SummaryRow(
            stringResource(Res.string.invested_amount),
            formatCurrency(result.investedAmount)
        )
        SummaryRow(
            stringResource(Res.string.estimated_returns),
            formatCurrency(result.estimatedReturns)
        )
        Text(
            stringResource(Res.string.yearly_growth),
            style = MaterialTheme.typography.titleLarge
        )
        result.yearlyGrowth.forEach { growth ->
            ResultCard(
                title = stringResource(Res.string.year_title, growth.year),
                value = formatCurrency(growth.value),
                detail = stringResource(
                    Res.string.estimated_returns_only,
                    formatCurrency(growth.estimatedReturns)
                )
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
            AppLogger.calculationSucceeded("loan-prepayment")
            onResult(LoanResultBundle(comparisonValue, periodicValue))
        } else {
            error = (comparison as LoanPrepaymentCalculationResult.Error).message
            AppLogger.calculationFailed("loan-prepayment")
        }
    }

    CalculatorScaffold(
        title = stringResource(Res.string.loan_prepayment_title),
        description = stringResource(Res.string.loan_prepayment_screen_description),
        onBack = onBack
    ) {
        CalculatorInputField(stringResource(Res.string.outstanding_principal), principal) {
            principal = it
            error = null
        }
        CalculatorInputField(stringResource(Res.string.annual_interest_rate), rate) {
            rate = it
            error = null
        }
        CalculatorInputField(stringResource(Res.string.current_emi), emi) {
            emi = it
            error = null
        }
        CalculatorInputField(stringResource(Res.string.remaining_tenure_months), tenure) {
            tenure = it
            error = null
        }
        CalculatorInputField(
            stringResource(Res.string.one_time_prepayment_amount),
            prepayment
        ) {
            prepayment = it
            error = null
        }
        CalculatorInputField(
            stringResource(Res.string.periodic_prepayment_amount),
            periodicPrepayment
        ) {
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
                Text(
                    stringResource(
                        prepaymentFrequencyResource(PrepaymentFrequency.entries[frequencyIndex])
                    )
                )
            }
            Button(onClick = ::calculate, modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.compare))
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
    ResultScaffold(
        title = stringResource(Res.string.loan_prepayment_result_title),
        onBack = onBack
    ) {
        result.comparison?.let { comparison ->
            Text(
                stringResource(Res.string.one_time_prepayment),
                style = MaterialTheme.typography.titleLarge
            )
            SummaryRow(
                stringResource(Res.string.original_tenure_label),
                stringResource(Res.string.original_tenure, comparison.original.months)
            )
            ResultCard(
                title = stringResource(Res.string.reduce_tenure),
                value = stringResource(Res.string.months, comparison.reduceTenure.months),
                detail = stringResource(
                    Res.string.interest_saved,
                    formatCurrency(comparison.reduceTenureInterestSaved)
                )
            )
            ResultCard(
                title = stringResource(Res.string.reduce_emi),
                value = formatCurrency(comparison.reduceEmi.emi),
                detail = stringResource(
                    Res.string.interest_saved,
                    formatCurrency(comparison.reduceEmiInterestSaved)
                )
            )
        }
        result.periodic?.let { periodic ->
            Text(
                stringResource(Res.string.periodic_prepayment),
                style = MaterialTheme.typography.titleLarge
            )
            ResultCard(
                title = stringResource(Res.string.new_payoff_plan),
                value = stringResource(Res.string.months, periodic.afterPrepayments.months),
                detail = stringResource(
                    Res.string.interest_saved,
                    formatCurrency(periodic.interestSaved)
                )
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.accessibility_back)
                        )
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
            "⌫" -> Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(Res.string.accessibility_backspace)
            )
            "AC" -> Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(Res.string.accessibility_clear)
            )
            else -> Text(label, fontSize = 18.sp)
        }
    }
}

private fun prepaymentFrequencyResource(frequency: PrepaymentFrequency): StringResource =
    when (frequency) {
        PrepaymentFrequency.MONTHLY -> Res.string.monthly
        PrepaymentFrequency.QUARTERLY -> Res.string.quarterly
        PrepaymentFrequency.HALF_YEARLY -> Res.string.half_yearly
        PrepaymentFrequency.YEARLY -> Res.string.yearly
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
