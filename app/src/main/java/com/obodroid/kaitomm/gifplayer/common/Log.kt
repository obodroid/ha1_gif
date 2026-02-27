package com.obodroid.kaitomm.gifplayer.common

import java.io.Serializable

data class Log(
    var priority: Int? = null,
    var message: String? = null,
    var method: String? = null,
    var `class`: String? = null,
    var lineNumber: Int? = null,
    var timestamp: Long? = null,
    var packageName: String? = null,
    var tag: String? = null,
    var versionCode: Int? = null,
) : Serializable