package com.obodroid.kaitomm.gifplayer

import android.net.Uri
import com.obodroid.kaitomm.gifplayer.common.util.JsonUtil
import java.io.File

class RemoteEmotionManifest : EmotionManifest() {

    override fun getEmotionFilePath(emotion: String?): String? {
        if (emotion.isNullOrBlank()) {
            return null
        }

        val emotionFile = getEmotionFile(emotion) ?: return null
        return Uri.fromFile(File("$folderPath/${emotionFile.filename}")).toString()
    }

    companion object {

        fun create(folderPath: String, filename: String): RemoteEmotionManifest {
            return JsonUtil.loadJsonFromFile("$folderPath/$filename", RemoteEmotionManifest::class.java)
                ?.apply {
                    this.folderPath = folderPath
                } ?: RemoteEmotionManifest()
        }
    }
}
