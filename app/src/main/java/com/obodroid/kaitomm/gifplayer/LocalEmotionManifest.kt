package com.obodroid.kaitomm.gifplayer

import android.content.Context
import android.net.Uri
import com.obodroid.kaitomm.gifplayer.common.util.JsonUtil

class LocalEmotionManifest : EmotionManifest() {

    override fun getEmotionFilePath(emotion: String?): String? {
        if (emotion.isNullOrBlank()) {
            return null
        }

        val emotionFile = getEmotionFile(emotion) ?: return null
        return Uri.parse("$folderPath/${emotionFile.filename}").toString()
    }

    companion object {

        fun create(context: Context, resourceId: Int): LocalEmotionManifest {
            return JsonUtil.loadJsonFromRaw(
                context.resources,
                resourceId,
                LocalEmotionManifest::class.java
            ).apply {
                folderPath = "android.resource://${context.packageName}/raw"
            }
        }
    }
}