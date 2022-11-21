package net.matsudamper.android.debugtool.adb.log

import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.logcat.ChanneledLogcatRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import net.matsudamper.android.debugtool.adb.AdbException

internal class AdbGetLogStream {
    @Throws(AdbException::class)
    fun exec(
        commandCoroutineScope: CoroutineScope,
        device: Device?,
        onError: () -> Unit,
    ): AdbLogChannelResult {
        val adb = runCatching {
            AndroidDebugBridgeClientFactory().build()
        }.onFailure {
            throw AdbException.AdbNotConnected(it)
        }.getOrThrow()

        val result = runCatching {
            adb.execute(
                request = ChanneledLogcatRequest(),
                scope = CoroutineScope(SupervisorJob(commandCoroutineScope.coroutineContext.job)),
                serial = device?.serial,
            )
        }.onFailure {
            throw AdbException.UnknownFailure(it)
        }.getOrThrow()

        return AdbLogChannelResult(
            channel = result,
            onError = { onError }
        )
    }
}