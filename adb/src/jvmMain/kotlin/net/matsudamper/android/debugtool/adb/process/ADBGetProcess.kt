package net.matsudamper.android.debugtool.adb.process

import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.shell.v2.ShellCommandRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.matsudamper.android.debugtool.adb.AdbException

internal class ADBGetProcess {
    suspend fun exec(device: Device?): List<ProcessResult> {
        val adb = runCatching {
            AndroidDebugBridgeClientFactory().build()
        }.onFailure {
            throw AdbException.AdbNotConnected(it)
        }.getOrThrow()

        val result = runCatching {
            adb.execute(
                request = ShellCommandRequest("ps"),
                serial = device?.serial,
            )
        }.getOrNull() ?: return emptyList()

        return parse(result.stdout)
    }

    private fun parse(lines: String): List<ProcessResult> {
        return lines.split("\n").mapNotNull { line ->
            val matchResult = regex.matchEntire(line) ?: return@mapNotNull null
            runCatching {
                ProcessResult(
                    user = matchResult.groups[1]!!.value,
                    pid = matchResult.groups[2]!!.value.toInt(),
                    ppid = matchResult.groups[3]!!.value.toInt(),
                    vsz = matchResult.groups[4]!!.value.toInt(),
                    rss = matchResult.groups[5]!!.value.toInt(),
                    wchan = matchResult.groups[6]!!.value,
                    addr = matchResult.groups[7]!!.value.toInt(),
                    s = matchResult.groups[8]!!.value,
                    name = matchResult.groups[9]!!.value,
                )
            }.getOrNull()
        }
    }

    companion object {
        private val regex = """^(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)$""".toRegex()
    }
}