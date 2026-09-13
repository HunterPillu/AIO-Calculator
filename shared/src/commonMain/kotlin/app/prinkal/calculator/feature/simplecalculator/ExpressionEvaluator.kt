package app.prinkal.calculator.feature.simplecalculator

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class AngleMode {
    DEGREES,
    RADIANS
}

sealed interface EvaluationResult {
    data class Success(val value: Double) : EvaluationResult

    data class Error(val message: String) : EvaluationResult
}

/**
 * Evaluates calculator expressions without executing arbitrary code.
 *
 * The parser supports arithmetic, parentheses, postfix percentage/factorial,
 * exponentiation, scientific notation, constants, and common scientific
 * functions. Display formatting remains outside the calculation engine.
 */
object ExpressionEvaluator {
    fun evaluate(expression: String): EvaluationResult = evaluate(expression, AngleMode.DEGREES)

    fun evaluate(expression: String, angleMode: AngleMode): EvaluationResult {
        if (expression.isBlank()) {
            return EvaluationResult.Error("Enter an expression")
        }

        return try {
            Parser(tokenize(expression), angleMode).parse()
        } catch (error: ParseException) {
            EvaluationResult.Error(error.message ?: "Invalid expression")
        }
    }

    private fun tokenize(expression: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0

        while (index < expression.length) {
            when (val character = expression[index]) {
                ' ', '\t', '\n', '\r' -> index++
                '+' -> {
                    tokens += Token.Plus
                    index++
                }
                '-' -> {
                    tokens += Token.Minus
                    index++
                }
                '*', '×' -> {
                    tokens += Token.Multiply
                    index++
                }
                '/', '÷' -> {
                    tokens += Token.Divide
                    index++
                }
                '^' -> {
                    tokens += Token.Power
                    index++
                }
                '%' -> {
                    tokens += Token.Percent
                    index++
                }
                '!' -> {
                    tokens += Token.Factorial
                    index++
                }
                '(' -> {
                    tokens += Token.LeftParenthesis
                    index++
                }
                ')' -> {
                    tokens += Token.RightParenthesis
                    index++
                }
                '.', in '0'..'9' -> {
                    val start = index
                    var hasDigits = false
                    var hasDecimalPoint = false

                    while (index < expression.length) {
                        when (expression[index]) {
                            in '0'..'9' -> {
                                hasDigits = true
                                index++
                            }
                            '.' -> {
                                if (hasDecimalPoint) {
                                    throw ParseException("Invalid number")
                                }
                                hasDecimalPoint = true
                                index++
                            }
                            else -> break
                        }
                    }

                    if (!hasDigits) {
                        throw ParseException("Invalid number")
                    }

                    if (index < expression.length &&
                        (expression[index] == 'e' || expression[index] == 'E')
                    ) {
                        index++
                        if (index < expression.length &&
                            (expression[index] == '+' || expression[index] == '-')
                        ) {
                            index++
                        }

                        val exponentStart = index
                        while (index < expression.length && expression[index].isDigit()) {
                            index++
                        }
                        if (exponentStart == index) {
                            throw ParseException("Invalid scientific notation")
                        }
                    }

                    val value = expression.substring(start, index).toDoubleOrNull()
                        ?: throw ParseException("Invalid number")
                    if (!value.isFinite()) {
                        throw ParseException("Number is too large")
                    }
                    tokens += Token.Number(value)
                }
                else -> {
                    if (!character.isLetter() && character != 'π') {
                        throw ParseException("Unsupported character: $character")
                    }
                    val start = index
                    index++
                    while (index < expression.length &&
                        (expression[index].isLetter() || expression[index] == 'π')
                    ) {
                        index++
                    }
                    tokens += Token.Identifier(expression.substring(start, index))
                }
            }
        }

        return tokens
    }

    private class Parser(
        private val tokens: List<Token>,
        private val angleMode: AngleMode
    ) {
        private var position = 0

        fun parse(): EvaluationResult {
            if (tokens.isEmpty()) {
                throw ParseException("Enter an expression")
            }

            val value = parseAddition()
            if (position < tokens.size) {
                throw ParseException("Unexpected token")
            }
            ensureFinite(value)
            return EvaluationResult.Success(value)
        }

        private fun parseAddition(): Double {
            var value = parseMultiplication()
            while (position < tokens.size) {
                value = when (tokens[position]) {
                    Token.Plus -> {
                        position++
                        value + parseMultiplication()
                    }
                    Token.Minus -> {
                        position++
                        value - parseMultiplication()
                    }
                    else -> return value
                }
                ensureFinite(value)
            }
            return value
        }

        private fun parseMultiplication(): Double {
            var value = parseUnary()
            while (position < tokens.size) {
                value = when (tokens[position]) {
                    Token.Multiply -> {
                        position++
                        value * parseUnary()
                    }
                    Token.Divide -> {
                        position++
                        val divisor = parseUnary()
                        if (divisor == 0.0) {
                            throw ParseException("Cannot divide by zero")
                        }
                        value / divisor
                    }
                    else -> return value
                }
                ensureFinite(value)
            }
            return value
        }

        private fun parseUnary(): Double {
            return when {
                position < tokens.size && tokens[position] == Token.Plus -> {
                    position++
                    parseUnary()
                }
                position < tokens.size && tokens[position] == Token.Minus -> {
                    position++
                    -parseUnary()
                }
                else -> parsePower()
            }
        }

        private fun parsePower(): Double {
            val base = parsePostfix()
            if (position < tokens.size && tokens[position] == Token.Power) {
                position++
                val exponent = parseUnary()
                val value = base.pow(exponent)
                ensureFinite(value)
                return value
            }
            return base
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (position < tokens.size) {
                value = when (tokens[position]) {
                    Token.Percent -> {
                        position++
                        value / 100.0
                    }
                    Token.Factorial -> {
                        position++
                        factorial(value)
                    }
                    else -> return value
                }
                ensureFinite(value)
            }
            return value
        }

        private fun parsePrimary(): Double {
            if (position >= tokens.size) {
                throw ParseException("Expected a value")
            }

            return when (val token = tokens[position++]) {
                is Token.Number -> token.value
                is Token.Identifier -> parseIdentifier(token.name)
                Token.LeftParenthesis -> {
                    val value = parseAddition()
                    if (position >= tokens.size || tokens[position] != Token.RightParenthesis) {
                        throw ParseException("Missing closing parenthesis")
                    }
                    position++
                    value
                }
                else -> throw ParseException("Expected a value")
            }
        }

        private fun parseIdentifier(identifier: String): Double {
            val name = identifier.lowercase()
            if (name == "pi" || identifier == "π") {
                return PI
            }
            if (name == "e") {
                return kotlin.math.E
            }

            if (position >= tokens.size || tokens[position] != Token.LeftParenthesis) {
                throw ParseException("Function $identifier requires parentheses")
            }
            position++
            val argument = parseAddition()
            if (position >= tokens.size || tokens[position] != Token.RightParenthesis) {
                throw ParseException("Missing closing parenthesis")
            }
            position++
            return applyFunction(name, argument)
        }

        private fun applyFunction(name: String, argument: Double): Double {
            val value = when (name) {
                "sin" -> sin(toRadians(argument))
                "cos" -> cos(toRadians(argument))
                "tan" -> tan(toRadians(argument))
                "asin" -> {
                    requireDomain(argument in -1.0..1.0, "asin is defined from -1 to 1")
                    fromRadians(asin(argument))
                }
                "acos" -> {
                    requireDomain(argument in -1.0..1.0, "acos is defined from -1 to 1")
                    fromRadians(acos(argument))
                }
                "atan" -> fromRadians(atan(argument))
                "log" -> {
                    requireDomain(argument > 0.0, "log requires a positive value")
                    log10(argument)
                }
                "ln" -> {
                    requireDomain(argument > 0.0, "ln requires a positive value")
                    ln(argument)
                }
                "sqrt" -> {
                    requireDomain(argument >= 0.0, "sqrt requires a non-negative value")
                    sqrt(argument)
                }
                "square" -> argument * argument
                "reciprocal" -> {
                    requireDomain(argument != 0.0, "Cannot take the reciprocal of zero")
                    1.0 / argument
                }
                else -> throw ParseException("Unknown function: $name")
            }
            ensureFinite(value)
            return value
        }

        private fun factorial(value: Double): Double {
            requireDomain(value >= 0.0 && value == value.toLong().toDouble(), "Factorial requires a non-negative integer")
            requireDomain(value <= 170.0, "Factorial result is too large")
            var result = 1.0
            var factor = 2L
            while (factor <= value.toLong()) {
                result *= factor.toDouble()
                factor++
            }
            return result
        }

        private fun toRadians(value: Double): Double {
            return if (angleMode == AngleMode.DEGREES) value * PI / 180.0 else value
        }

        private fun fromRadians(value: Double): Double {
            return if (angleMode == AngleMode.DEGREES) value * 180.0 / PI else value
        }

        private fun requireDomain(condition: Boolean, message: String) {
            if (!condition) {
                throw ParseException(message)
            }
        }

        private fun ensureFinite(value: Double) {
            if (!value.isFinite()) {
                throw ParseException("Result is too large")
            }
        }
    }

    private sealed interface Token {
        data class Number(val value: Double) : Token
        data class Identifier(val name: String) : Token

        data object Plus : Token
        data object Minus : Token
        data object Multiply : Token
        data object Divide : Token
        data object Power : Token
        data object Percent : Token
        data object Factorial : Token
        data object LeftParenthesis : Token
        data object RightParenthesis : Token
    }

    private class ParseException(message: String) : IllegalArgumentException(message)
}
