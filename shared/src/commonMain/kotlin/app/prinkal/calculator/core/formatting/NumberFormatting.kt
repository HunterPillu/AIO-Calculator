package app.prinkal.calculator.core.formatting

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

fun formatNumber(value: Double, maxFractionDigits: Int = 2): String {
    if (!value.isFinite()) return "—"
    if (value == 0.0) return "0"

    val digits = maxFractionDigits.coerceIn(0, 6)
    val factor = powerOfTen(digits)
    val scaled = value * factor
    val rounded = if (scaled >= 0.0) {
        floor(scaled + 0.5) / factor
    } else {
        ceil(scaled - 0.5) / factor
    }
    val negative = rounded < 0.0
    val absolute = abs(rounded)
    val whole = absolute.toLong()
    val fraction = ((absolute - whole) * factor + 0.5).toLong()
    val sign = if (negative) "-" else ""

    if (fraction == 0L) return "$sign$whole"

    val fractionText = fraction.toString().padStart(digits, '0').trimEnd('0')
    return "$sign$whole.$fractionText"
}

fun formatCurrency(value: Double): String = "₹${formatNumber(value)}"

private fun powerOfTen(exponent: Int): Double {
    var result = 1.0
    repeat(exponent) { result *= 10.0 }
    return result
}
