package com.obodroid.kaitomm.gifplayer

import com.google.gson.annotations.SerializedName

class Emotion(
        @SerializedName("fileName")
        val filename: String,
        val emotion: String
)