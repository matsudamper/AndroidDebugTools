package net.matsudamper.android.debugtool.adb.log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Serializable
import java.time.LocalDateTime


class AdbLogChannelResult(
    private val channel: ReceiveChannel<String>,
    private val onError: (e: Throwable) -> Unit,
    private val maxLines: Int = 100000,
) {
    private val _logs: MutableStateFlow<List<AdbLogResult>> = MutableStateFlow(listOf())
    val logs: StateFlow<List<AdbLogResult>> = _logs.asStateFlow()
    val isClosedForReceive: Boolean get() = channel.isClosedForReceive
    fun close() {
        channel.cancel()
    }

    fun CoroutineScope.startCollect() {
        val logLineChannel: Channel<String> = Channel(Channel.UNLIMITED)
        launch {
            channel.receiveAsFlow()
                .catch { onError(it) }
                .map { newResult ->
                    newResult.split("\n").forEach {
                        logLineChannel.send(it)
                    }
                }
                .collect()
        }

        launch {
            logLineChannel.receiveAsFlow().collect channelCollect@{ line ->
                val matchResult = logStartRegex.matchEntire(line)
                if (matchResult == null) {
                    _logs.update { target ->
                        target.toMutableList().also {
                            val lastItem = it.removeLastOrNull() ?: return@channelCollect
                            it.add(
                                lastItem.copy(
                                    body = run {
                                        val body = lastItem.body
                                        if (body.isEmpty()) {
                                            line
                                        } else {
                                            body.plus("\n$line")
                                        }
                                    }
                                )
                            )
                        }
                    }
                    return@channelCollect
                }

                _logs.update { target ->
                    target.toMutableList().also {
                        it.add(
                            createResult(
                                index = target.lastOrNull()?.index?.plus(1) ?: 0,
                                logStartLineMatchResult = matchResult,
                            ) ?: return@channelCollect
                        )
                    }.takeLast(maxLines)
                }
            }
        }
    }

    private fun createResult(
        index: Long,
        logStartLineMatchResult: MatchResult,
    ): AdbLogResult? {
        val month = logStartLineMatchResult.groups[1]?.value ?: return null
        val day = logStartLineMatchResult.groups[2]?.value ?: return null

        val hour = logStartLineMatchResult.groups[3]?.value ?: return null
        val min = logStartLineMatchResult.groups[4]?.value ?: return null
        val second = logStartLineMatchResult.groups[5]?.value ?: return null
        val millSecond = logStartLineMatchResult.groups[6]?.value ?: return null

        val pid = logStartLineMatchResult.groups[7]?.value ?: return null // tmp name
        val ppid = logStartLineMatchResult.groups[8]?.value ?: return null // tmp name

        val level = logStartLineMatchResult.groups[9]?.value ?: return null
        val tag = logStartLineMatchResult.groups[10]?.value ?: return null

        return AdbLogResult(
            index = index,
            dateTime = LocalDateTime.now()
                .withMonth(month.toInt())
                .withDayOfMonth(day.toInt())
                .withHour(hour.toInt())
                .withMinute(min.toInt())
                .withSecond(second.toInt())
                .withNano(millSecond.toInt() * 1000000),
            level = when (level) {
                "E" -> AdbLogResult.Level.Error
                "W" -> AdbLogResult.Level.Warning
                "D" -> AdbLogResult.Level.Debug
                "I" -> AdbLogResult.Level.Info
                "A" -> AdbLogResult.Level.Assert
                "V" -> AdbLogResult.Level.Verbose
                else -> AdbLogResult.Level.Unknown
            },
            tag = tag,
            body = "",
            pid = pid.toInt(),
            ppid = ppid.toInt(),
        )
    }

    companion object {
        // [ 12-30 03:28:54.013  8009: 8698 D/LOG      ]
        private val logStartRegex =
            """^\[\s*(\d+)-(\d+)\s+(\d+):(\d+):(\d+)\.(\d+)\s+(\d+):\s*(\d+)\s+(.)/(.+?)\s*\]$""".toRegex()
    }
}

data class AdbLogResult(
    val index: Long,
    val dateTime: LocalDateTime,
    val level: Level,
    val tag: String,
    val body: String,
    val pid: Int,
    val ppid: Int,
) : Serializable {
    enum class Level {
        Error,
        Warning,
        Debug,
        Info,
        Assert,
        Verbose,
        Unknown,
        ;
    }
}
