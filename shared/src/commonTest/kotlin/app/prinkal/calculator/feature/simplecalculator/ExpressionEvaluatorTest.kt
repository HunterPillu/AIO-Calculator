package app.prinkal.calculator.feature.simplecalculator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpressionEvaluatorTest {
    @Test
    fun `respects operator precedence`() {
        assertValue("2 + 3 × 4", 14.0)
        assertValue("(2 + 3) × 4", 20.0)
    }

    @Test
    fun `evaluates decimals and negative numbers`() {
        assertValue("-2.5 + 1.25", -1.25)
        assertValue("10.5 ÷ 2", 5.25)
    }

    @Test
    fun `evaluates percentages as postfix values`() {
        assertValue("200 × 10%", 20.0)
        assertValue("50%", 0.5)
    }

    @Test
    fun `supports scientific notation`() {
        assertValue("1.5e2 + 2", 152.0)
    }

    @Test
    fun `evaluates scientific functions in degrees`() {
        assertValue("sin(30)", 0.5)
        assertValue("sqrt(81) + log(100)", 11.0)
        assertValue("2^3^2", 512.0)
    }

    @Test
    fun `evaluates trigonometry in radians`() {
        assertValue("sin(1.5707963267948966)", 1.0, AngleMode.RADIANS)
    }

    @Test
    fun `evaluates constants factorial and helper functions`() {
        assertValue("π", kotlin.math.PI)
        assertValue("5!", 120.0)
        assertValue("reciprocal(4) + square(3)", 9.25)
    }

    @Test
    fun `rejects invalid expressions`() {
        assertError("2 +")
        assertError("(2 + 3")
        assertError("2 / 0")
        assertError("2 & 3")
        assertError("sqrt(-1)")
        assertError("4.5!")
        assertError("unknown(2)")
    }

    @Test
    fun `handles empty input`() {
        assertError("")
        assertError("   ")
    }

    private fun assertValue(
        expression: String,
        expected: Double,
        angleMode: AngleMode = AngleMode.DEGREES
    ) {
        val result = ExpressionEvaluator.evaluate(expression, angleMode)
        assertTrue(result is EvaluationResult.Success, "Expected success for $expression")
        assertEquals(expected, (result as EvaluationResult.Success).value, 0.000000001)
    }

    private fun assertError(expression: String) {
        assertTrue(
            ExpressionEvaluator.evaluate(expression) is EvaluationResult.Error,
            "Expected error for $expression"
        )
    }
}
