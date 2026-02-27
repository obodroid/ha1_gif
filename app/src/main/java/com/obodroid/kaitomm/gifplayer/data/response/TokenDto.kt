package com.obodroid.kaitomm.gifplayer.data.response

import com.google.gson.annotations.SerializedName

class TokenDto(
        @SerializedName("access_token")
        val token: String,

        @SerializedName("token_type")
        val tokenType: String,

        @SerializedName("expires_in")
        val validTimeInSecond: Long
)