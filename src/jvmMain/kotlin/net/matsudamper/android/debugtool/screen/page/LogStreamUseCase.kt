package net.matsudamper.android.debugtool.screen.page

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.matsudamper.android.debugtool.adb.AdbCommandProcessor
import net.matsudamper.android.debugtool.adb.AdbException
import net.matsudamper.android.debugtool.adb.log.AdbLogResult
import net.matsudamper.android.debugtool.adb.device.DeviceResult

class LogStreamUseCase(
    private val adbCommandProcessor: AdbCommandProcessor,
) {
    private val _logStateFlow: MutableStateFlow<List<AdbLogResult>> = MutableStateFlow(listOf())
    val logStateFlow: StateFlow<List<AdbLogResult>> = _logStateFlow.asStateFlow()

    private val _connected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private var logStreamJob = Job()

    suspend fun connect() {
        coroutineScope {
            data class Tmp(
                val serial: DeviceResult?,
                val logConnected: Boolean,
            )
            adbCommandProcessor.currentDeviceFlow.combine(connected) { serial, logConnected ->
                Tmp(
                    serial = serial,
                    logConnected = logConnected,
                ).also { println("state: $it") }
            }
                .filter { it.logConnected.not() }
                .filter { it.serial != null }
                .collect {
                    logConnect(this)
                }
        }
    }

    private fun logConnect(coroutineScope: CoroutineScope) {
        logStreamJob.cancel()
        logStreamJob = Job()
        coroutineScope.launch(logStreamJob) {
            val result = try {
                adbCommandProcessor.getLogStream(this)
            } catch (e: AdbException) {
                when (e) {
                    is AdbException.AdbNotConnected -> TODO()
                    is AdbException.AdbNotFound -> TODO()
                    is AdbException.UnknownFailure -> TODO()
                }
            }

            _connected.value = true
            launch {
                while (result.isClosedForReceive.not()) {
                    delay(100)
                }
                println("disconnected logcat")
                _connected.value = false
            }

            with(result) {
                startCollect()
            }

            result.logs.collect { newLog ->
                _logStateFlow.update {
                    newLog
                }
            }
        }
    }
}