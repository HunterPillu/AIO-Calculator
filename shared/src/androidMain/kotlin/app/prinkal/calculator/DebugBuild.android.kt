package app.prinkal.calculator.core.logging

import android.os.Debug

actual val isDebugBuild: Boolean
    get() = Debug.isDebuggerConnected()
