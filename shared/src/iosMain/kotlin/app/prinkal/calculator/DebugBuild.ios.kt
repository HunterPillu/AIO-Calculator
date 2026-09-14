package app.prinkal.calculator.core.logging

import kotlin.native.Platform
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual val isDebugBuild: Boolean
    get() = Platform.isDebugBinary
