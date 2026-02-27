package com.obodroid.kaitomm.gifplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.obodroid.kaitomm.gifplayer.common.intents.ActionIntent
import com.obodroid.kaitomm.gifplayer.DataPackConfigurations.EMOTION_FOLDER_NAME
import com.obodroid.kaitomm.gifplayer.DataPackConfigurations.KEY_EMOTION_PACK_VERSION
import com.obodroid.kaitomm.gifplayer.common.LoggerFactory
import com.obodroid.kaitomm.gifplayer.common.RetrofitFactory
import com.obodroid.kaitomm.gifplayer.common.extensions.applySchedulers
import com.obodroid.kaitomm.gifplayer.common.extensions.runService
import com.obodroid.kaitomm.gifplayer.common.extensions.subscribeEx
import com.obodroid.kaitomm.gifplayer.common.util.SharePreferenceWrapper
import com.obodroid.kaitomm.gifplayer.data.apicontroller.DataPackApiController
import com.obodroid.kaitomm.gifplayer.data.repository.FileDownloadRepository

class DataPackDownloadBroadcastReceiver : BroadcastReceiver() {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    private val dataPackApiController by lazy {
        RetrofitFactory.getApiController(DataPackApiController::class.java)
    }

    private val fileDownloadManager by lazy {
        FileDownloadRepository()
    }

    override fun onReceive(context: Context, intent: Intent) {
        getEmotionPack(context, destinationPath = "${context.filesDir.path}/$EMOTION_FOLDER_NAME")
    }

    private fun getEmotionPack(context: Context, destinationPath: String) {
        logger.debug("getEmotionPack destinationPath $destinationPath")
        dataPackApiController.getEmotionPack()
            .applySchedulers()
            .subscribeEx(
                onSuccess = {
                    if (needUpdate(KEY_EMOTION_PACK_VERSION, it.version)) {
                        downloadAndExtract(
                            context,
                            it.fileUrl,
                            destinationPath,
                            Pair(KEY_EMOTION_PACK_VERSION, it.version)
                        )
                    }
                }
            )
    }

    private fun downloadAndExtract(
        context: Context,
        url: String,
        destinationPath: String,
        version: Pair<String, Int>
    ) {
        fileDownloadManager
            .downloadFileStream(url, destinationPath)
            .applySchedulers()
            .subscribeEx(
                onSuccess = { success ->
                    if (success) {
                        SharePreferenceWrapper.putInt(version.first, version.second)
                        context.runService<GifPlayerService>(action = ActionIntent.ACTION_EMOTION_PACK_UPDATED)
                    }
                },
                onError = {
                    it.printStackTrace()
                }
            )
    }

    private fun needUpdate(versionKey: String, remoteVersion: Int): Boolean {
        val localVersion = SharePreferenceWrapper.getInt(versionKey, 0)

        logger.debug("versionKey = $versionKey \n localVersion = $localVersion \n remoteVersion = $remoteVersion")
//        return false
        return remoteVersion > localVersion
    }
}