package net.matsudamper.android.debugtool.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import net.matsudamper.android.debugtool.adb.AdbCommandProcessor

object GlobalScope {
    val adbCommandProcessor by lazy {
        AdbCommandProcessor(
            processorScope = CoroutineScope(GlobalScope.coroutineContext),
        )
    }
}