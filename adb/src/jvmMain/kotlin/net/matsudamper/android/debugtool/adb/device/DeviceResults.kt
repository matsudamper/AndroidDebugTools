package net.matsudamper.android.debugtool.adb.device

data class DeviceResult(
    val device: com.malinskiy.adam.request.device.Device,
    val name: String?,
)