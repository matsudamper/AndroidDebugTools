package net.matsudamper.android.debugtool.adb.process
data class ProcessResult(
    val user: String,
    val pid: Int,
    val ppid: Int,
    val vsz: Int,
    val rss: Int,
    val wchan: String,
    val addr: Int,
    val s: String,
    val name: String
)