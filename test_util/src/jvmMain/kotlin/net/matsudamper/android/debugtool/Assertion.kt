package net.matsudamper.android.debugtool

import kotlinx.coroutines.*

suspend fun waitSuccessAssertion(timeoutMills: Long, assertionBlock: () -> Unit) {
    var lastError: Throwable? = null
    coroutineScope {
        try {
            withTimeout(timeMillis = timeoutMills) {
                while (
                    isActive &&
                    runCatching { assertionBlock() }
                        .fold(
                            onSuccess = { false },
                            onFailure = {
                                lastError = it
                                when (it) {
                                    is AssertionError -> true
                                    else -> throw IllegalStateException(it)
                                }
                            },
                        )
                ) {
                    delay(10)
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw TimeoutAssertionException(lastError ?: e, timeoutMills)
        }
    }
}

class TimeoutAssertionException(e: Throwable, timeoutMills: Long) : Exception(
    "Timed out waiting for $timeoutMills ms", e
)