package com.lsang.smokingcheck.util

/** milliseconds → "MM:SS" */
fun Long.prettyMmSs(): String {
    val totalSec = (this / 1000L).toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    return String.format("%02d:%02d", m, s)
}
