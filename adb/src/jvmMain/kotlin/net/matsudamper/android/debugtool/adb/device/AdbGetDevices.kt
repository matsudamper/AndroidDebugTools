package net.matsudamper.android.debugtool.adb.device

import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.prop.GetPropRequest
import kotlinx.coroutines.coroutineScope
import net.matsudamper.android.debugtool.adb.AdbException

internal class AdbGetDevices {
    @Throws(AdbException::class)
    suspend fun exec(): List<DeviceResult> {
        return coroutineScope {
            runCatching {
                val adb = runCatching {
                    AndroidDebugBridgeClientFactory().build()
                }.onFailure {
                    throw AdbException.AdbNotConnected(it)
                }.getOrThrow()

                val devices = adb.execute(request = ListDevicesRequest())
                println("devices: $devices")
                devices.map { device ->
                    val prop = adb.execute(request = GetPropRequest(), serial = device.serial)
                    DeviceResult(
                        device = device,
                        name = prop["ro.product.model"]
                    )
                }
            }.onFailure {
                throw if (it is AdbException) {
                    it
                } else {
                    AdbException.UnknownFailure(it)
                }
            }.getOrThrow()
        }
    }
}