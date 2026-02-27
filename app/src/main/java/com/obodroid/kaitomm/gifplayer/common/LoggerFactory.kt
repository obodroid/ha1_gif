package com.obodroid.kaitomm.gifplayer.common


object LoggerFactory {

    fun getLogger(tag: String): Logger {
        return Logger(tag)
    }
}