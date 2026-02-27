package com.obodroid.kaitomm.gifplayer.common

import android.util.Log
import com.obodroid.kaitomm.gifplayer.BuildConfig

open class Logger(
    val tag: String
) {

    fun verbose(message: String) {
        Log.v(tag, message)
    }

    fun info(message: String) {
        Log.i(tag, message)
    }

    fun debug(message: String) {
        Log.d(tag, message)
    }

    fun warning(message: String) {
        Log.w(tag, message)
    }

    fun error(message: String) {
        Log.e(tag, message)
    }
}