package net.matsudamper.android.debugtool.adb

import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.interactor.StartAdbInteractor
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.prop.GetPropRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import java.nio.charset.Charset
import java.util.*

class AdbCommandProcessor {
    private val json = Json
    private var serial: String? = null

    fun setSerial(serial: String?) {
        this.serial = serial
    }

    suspend fun getDevices(): AdbResults<DeviceResults> {
        return runCatching {
            println(System.getProperty("os.name").lowercase(Locale.ENGLISH).contains("win"))

            val adb = runCatching {
                AndroidDebugBridgeClientFactory().build()
            }.onFailure {
                return AdbResults.AdbException(AdbException.AdbNotConnected(it))
            }.getOrThrow()


            val devices = adb.execute(request = ListDevicesRequest())
            AdbResults.Success(
                result = DeviceResults(
                    devices = devices.map { device ->
                        val prop = adb.execute(request = GetPropRequest(), serial = device.serial)
                        DeviceResults.Device(
                            device = device,
                            name = prop["ro.product.model"]
                        )
                    }
                )
            )
        }.fold(
            onSuccess = { it },
            onFailure = {
                it.printStackTrace()
                AdbResults.UnknownFailure(it)
            }
        )
    }

    companion object {
        suspend fun startServer(): AdbException.AdbNotFound? {
            return runCatching {
                StartAdbInteractor().execute()
            }.onFailure {
                it.printStackTrace()
            }.fold(
                onSuccess = { null },
                onFailure = { AdbException.AdbNotFound(it) },
            )
        }
    }
}

sealed interface AdbResults<T> {
    data class Success<T>(
        val result: T
    ) : AdbResults<T>

    class UnknownFailure<T>(
        val e: Throwable
    ) : AdbResults<T>

    class AdbException<T>(
        val e: net.matsudamper.android.debugtool.adb.AdbException,
    ) : AdbResults<T>
}

data class DeviceResults(
    val devices: List<Device>
) {
    data class Device(
        val device: com.malinskiy.adam.request.device.Device,
        val name: String?,
    )
}

sealed class AdbException : Exception() {
    class AdbNotFound(
        cause: Throwable?,
    ) : AdbException() {
        override val cause: Throwable? = cause
        override val message: String? = cause?.message
    }

    class AdbNotConnected(
        cause: Throwable?,
    ) : AdbException() {
        override val cause: Throwable? = cause
        override val message: String? = cause?.message
    }
}