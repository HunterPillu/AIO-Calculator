package app.prinkal.calculator.core.logging

import co.touchlab.kermit.Logger

expect val isDebugBuild: Boolean

object AppLogger {
    private val logger = Logger.withTag("AIO-Calculator")

    fun calculatorOpened(id: String) {
        debug("calculator_opened:$id")
    }

    fun settingsOpened() {
        debug("settings_opened")
    }

    fun calculationSucceeded(id: String) {
        debug("calculation_succeeded:$id")
    }

    fun calculationFailed(id: String) {
        debug("calculation_failed:$id")
    }

    fun releaseWarning(event: String) {
        logger.w { "release_warning:$event" }
    }

    private fun debug(event: String) {
        if (isDebugBuild) {
            logger.d { event }
        }
    }
}
