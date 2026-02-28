package com.obodroid.kaitomm.gifplayer

import com.google.gson.annotations.SerializedName

data class Emotion(
    @SerializedName("fileName") val filename: String,
    val emotion: String
)

data class EmotionMap(
    @SerializedName("emotionMap") val emotions: List<Emotion>
)