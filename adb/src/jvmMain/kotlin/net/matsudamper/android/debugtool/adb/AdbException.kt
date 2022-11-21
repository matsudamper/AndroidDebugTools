package net.matsudamper.android.debugtool.adb

sealed class AdbException(cause: Throwable?) : Exception(cause) {
    class AdbNotFound(
        cause: Throwable?,
    ) : AdbException(cause)

    class AdbNotConnected(
        cause: Throwable?,
    ) : AdbException(cause)

    class UnknownFailure(
        cause: Throwable?,
    ) : AdbException(cause)
}