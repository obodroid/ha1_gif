package com.obodroid.kaitomm.gifplayer

import com.google.gson.annotations.SerializedName

abstract class EmotionManifest {

    @SerializedName("emotionMap")
    private var emotions: List<Emotion> = emptyList()

    protected var folderPath = ""

    abstract fun getEmotionFilePath(emotion: String?): String?

    protected fun getEmotionFile(emotion: String): Emotion? =
        emotions.firstOrNull {
            it.emotion.equals(emotion, ignoreCase = true)
        }

}
