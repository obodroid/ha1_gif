package com.obodroid.kaitomm.gifplayer.data.response

import com.google.gson.annotations.SerializedName

class PackDto(
        val version: Int,
        @SerializedName("file")
        val fileUrl: String
)