package com.tensorflow.humanrec.utility

import android.os.Trace
import android.util.Log

const val TAG = "tensor"
var logEnabled = true

fun executeWithTimeLog(func: () -> Unit) {
    val t0 = System.currentTimeMillis()
    func.invoke()
    val t1 = System.currentTimeMillis()

    if (logEnabled) {
        ("time: " + (t1 - t0).toString() + "").i()
    }
}

fun endTrace() {
    Trace.endSection()
}

fun String.trace() {
    if (logEnabled) {
        Trace.beginSection(this)
    }
}

fun String.i() {
    Log.i(TAG, this)
}

fun String.e() {
    Log.e(TAG, this)
}
