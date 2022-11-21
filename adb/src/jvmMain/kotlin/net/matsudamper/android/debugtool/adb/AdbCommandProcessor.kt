package net.matsudamper.android.debugtool.adb

import com.malinskiy.adam.interactor.StartAdbInteractor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.matsudamper.android.debugtool.adb.device.AdbGetDevices
import net.matsudamper.android.debugtool.adb.device.DeviceResult
import net.matsudamper.android.debugtool.adb.log.AdbGetLogStream
import net.matsudamper.android.debugtool.adb.log.AdbLogChannelResult
import net.matsudamper.android.debugtool.adb.process.ADBGetProcess
import net.matsudamper.android.debugtool.adb.process.ProcessResult
import java.util.*

class AdbCommandProcessor(
    private val processorScope: CoroutineScope,
) {
    private val _devices: MutableStateFlow<List<DeviceResult>> = MutableStateFlow(listOf())
    val devices: StateFlow<List<DeviceResult>> = _devices.asStateFlow()

    private val _currentDeviceFlow: MutableStateFlow<DeviceResult?> = MutableStateFlow(null)
    val currentDeviceFlow: StateFlow<DeviceResult?> = _currentDeviceFlow.asStateFlow()

    fun setTargetDevice(serial: DeviceResult?) {
        println("setTargetDevice: $serial")
        this._currentDeviceFlow.value = serial
    }

    @Throws(AdbException::class)
    fun updateDeviceList() {
        processorScope.launch {
            val newDeviceList = AdbGetDevices().exec()

            _devices.update {
                newDeviceList
            }
        }
    }

    @Throws(AdbException::class)
    fun getLogStream(coroutineScope: CoroutineScope): AdbLogChannelResult = AdbGetLogStream().exec(
        commandCoroutineScope = coroutineScope,
        device = currentDeviceFlow.value?.device,
        onError = {
            _currentDeviceFlow.value = null
            processorScope.launch {
                updateDeviceList()
            }
        }
    )

    suspend fun getProcess(): List<ProcessResult> = ADBGetProcess().exec(device = currentDeviceFlow.value?.device)

    companion object {
        val started: MutableStateFlow<Boolean> = MutableStateFlow(false)
        suspend fun startServer(): AdbException.AdbNotFound? {
            println("startServer")
            return runCatching {
                StartAdbInteractor().execute()
            }.onFailure {
                it.printStackTrace()
            }.onSuccess { it ->
                println("start adb: $it")
                started.value = it
            }.fold(
                onSuccess = { null },
                onFailure = { AdbException.AdbNotFound(it) },
            )
        }
    }
}

