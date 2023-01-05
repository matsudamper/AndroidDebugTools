package net.matsudamper.android.debugtool.adb.result

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.matsudamper.android.debugtool.adb.log.AdbLogChannelResult
import net.matsudamper.android.debugtool.adb.log.AdbLogResult
import java.time.LocalDateTime

class AdbLogChannelResultTest : StringSpec({
    "ログがパースできる" {
        val logLines = listOf(
            "one",
            "two",
            "three",
        )
        val result = AdbLogChannelResult(
            Channel<String>(Channel.UNLIMITED).apply {
                send("[ 12-30 03:29:11.071  8009: 8698 D/L O G      ]")
                logLines.forEach { send(it) }
            },
            onError = {}
        )

        with(result) {
            launch(Job()) {
                startCollect()
            }
        }
        delay(1)
        result.logs.value.shouldHaveSize(1)
        val expected = AdbLogResult(
            index = 0,
            dateTime = LocalDateTime.now()
                .withMonth(12)
                .withDayOfMonth(30)
                .withHour(3)
                .withMinute(29)
                .withSecond(11)
                .withNano(71 * 1000 * 1000),
            level = AdbLogResult.Level.Debug,
            tag = "L O G",
            body = logLines.joinToString("\n"),
            pid = 8009,
            ppid = 8698,
        )
        result.logs.value.first().shouldBe(expected)
    }

    "ログがパースできる2" {
        val logLines = listOf(
            "[VRI[WatchWhileActivity]#33](f:0,a:2) Faking releaseBufferCallback from transactionCompleteCallback"
        )
        val result = AdbLogChannelResult(
            Channel<String>(Channel.UNLIMITED).apply {
                send("[ 12-31 05:02:20.246 14640:16121 E/BLASTBufferQueue ]")
                logLines.forEach { send(it) }
            },
            onError = {}
        )

        with(result) {
            launch(Job()) {
                startCollect()
            }
        }
        delay(1)
        result.logs.value.shouldHaveSize(1)
        val expected = AdbLogResult(
            index = 0,
            dateTime = LocalDateTime.now()
                .withMonth(12)
                .withDayOfMonth(31)
                .withHour(5)
                .withMinute(2)
                .withSecond(20)
                .withNano(246 * 1000 * 1000),
            level = AdbLogResult.Level.Error,
            tag = "BLASTBufferQueue",
            body = logLines.joinToString("\n"),
            pid = 14640,
            ppid = 16121,
        )
        result.logs.value.first().shouldBe(expected)
    }

    "追加のログが反映される" {
        val firstLogLines = listOf(
            "one",
            "two",
            "three",
        )
        val secondLogLines = listOf(
            "four",
            "three",
            "five",
        )
        val channel = Channel<String>(Channel.UNLIMITED).apply {
            send("[ 12-30 03:29:11.071  8009: 8698 D/L O G      ]")
        }
        val logResult = AdbLogChannelResult(
            channel = channel,
            onError = {}
        )

        with(logResult) {
            launch(Job()) {
                startCollect()
            }
        }

        val expected = AdbLogResult(
            index = 0,
            dateTime = LocalDateTime.now()
                .withMonth(12)
                .withDayOfMonth(30)
                .withHour(3)
                .withMinute(29)
                .withSecond(11)
                .withNano(71 * 1000 * 1000),
            level = AdbLogResult.Level.Debug,
            tag = "L O G",
            body = firstLogLines.joinToString("\n"),
            pid = 8009,
            ppid = 8698,
        )

        firstLogLines.forEach { channel.send(it) }
        delay(1)
        run {
            val result = logResult.logs.value
            result.shouldHaveSize(1)

            result.first().shouldBe(expected)
        }

        secondLogLines.forEach { channel.send(it) }
        delay(1)
        run {
            val result = logResult.logs.value
            result.shouldHaveSize(1)

            result.first().shouldBe(
                expected.copy(
                    body = firstLogLines.plus(secondLogLines)
                        .joinToString("\n")
                )
            )
        }
    }
})