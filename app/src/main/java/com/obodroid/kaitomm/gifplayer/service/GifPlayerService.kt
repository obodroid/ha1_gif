package com.obodroid.kaitomm.gifplayer.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.DeadObjectException
import android.os.IBinder
import com.obodroid.kaitomm.gifplayer.*
import com.obodroid.kaitomm.gifplayer.common.intents.ActionIntent
import com.obodroid.kaitomm.gifplayer.common.LoggerFactory
import com.obodroid.kaitomm.gifplayer.common.extensions.localBroadcast
import com.obodroid.kaitomm.gifplayer.common.util.JsonUtil
import com.obodroid.kaitomm.gifplayer.common.util.SharePreferenceWrapper
import com.obodroid.kaitomm.gifplayer.data.model.EmotionData
import com.obodroid.kaitomm.gifplayer.fragment.JooxWebViewFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class GifPlayerService : Service() {

    private val logger = LoggerFactory.getLogger(javaClass.name)
    private var remoteEmotions: EmotionManifest? = null
    private var localEmotions: EmotionManifest? = null
    private var mediaListener: IGifPlayerMediaListener? = null
    private val commandList by lazy { mutableListOf<Intent>() }

    private val binder = object : IGifPlayerService.Stub() {
        override fun setfaceInfo(bundle: Bundle?) {
            localBroadcast(ActionIntent.ACTION_ON_DISPLAY_EMOTION) {
                var emotion = bundle?.getString("emotion", "neutral")
                var message = ""

                if (JsonUtil.isValidJson(emotion!!)) {
                    val emotionObj = JSONObject(emotion)
                    emotion = "iaq_info"
                    message = emotionObj["iaqInfo"].toString()

                    logger.debug("emotion $emotion")
                    logger.debug("message $message")
                }
                val emotionFilePath = getEmotionFilePath(emotion)
                logger.debug("emotionFilePath $emotionFilePath")

                logger.debug("Receive emotion: $emotion - $emotionFilePath")

                val emotionData = EmotionData(emotion, emotionFilePath)
                putExtra(BaseMainActivity.EXTRA_EMOTION, emotionData)
                putExtra(BaseMainActivity.EXTRA_MESSAGE, message)

            }
        }

        override fun showText(text: String) {
            localBroadcast(ActionIntent.ACTION_ON_DISPLAY_TEXT) {
                logger.debug("Receive text: $text")
                logger.debug(text.split("//").toString())
                putExtra(BaseMainActivity.EXTRA_TEXT, text.split("//")[0])
                if (text.split("//").size > 1) {
                    putExtra(BaseMainActivity.EXTRA_DURATION_TIME, text.split("//")[1].toInt())
                }
            }
        }

        override fun takePic() {
        }

        override fun showBottomText(text: String?) {
            localBroadcast(ActionIntent.ACTION_ON_DISPLAY_BOTTOM_TEXT) {
                putExtra(BaseMainActivity.EXTRA_TEXT, text)
            }
        }

        override fun playVideo(data: Bundle?) {
            logger.debug("playVideo")
            localBroadcast(ActionIntent.ACTION_PLAY_VIDEO) {
                putExtras(data ?: Bundle())
            }
        }

        override fun pauseVideo() {
            logger.debug("pauseVideo")
            localBroadcast(ActionIntent.ACTION_PAUSE_VIDEO)
        }

        override fun resumeVideo() {
            logger.debug("resumeVideo")
            localBroadcast(ActionIntent.ACTION_RESUME_VIDEO)
        }

        override fun setting(key: String, value: String) {
            val oldValue = SharePreferenceWrapper.getString(key)
            SharePreferenceWrapper.putString(key, value)
            if (oldValue != value) {
                Timber.i("key: $key value: '$value'")
            }
            localBroadcast(ActionIntent.ACTION_SETTING) {
                putExtra(key, value)
            }
        }

        override fun setupPlayer(token: String?, listener: IGifPlayerMediaListener?) {
            Timber.d("setupPlayer token: $token")
            mediaListener = listener
            commandList.clear()
            localBroadcast(ActionIntent.ACTION_MEDIA_SETUP_PLAYER) {
                putExtra(BaseMainActivity.EXTRA_MEDIA_TOKEN, token)
            }
        }

        override fun playMedia(id: String?) {
            Timber.d("playMedia")
            localBroadcast(ActionIntent.ACTION_MEDIA_PLAY) {
                putExtra(JooxWebViewFragment.EXTRA_MEDIA_ID, id)
            }
        }

        override fun pauseMedia() {
            localBroadcast(ActionIntent.ACTION_MEDIA_PAUSE)
        }

        override fun resumeMedia() {
            localBroadcast(ActionIntent.ACTION_MEDIA_RESUME)
        }

        override fun stopMedia() {
            localBroadcast(ActionIntent.ACTION_MEDIA_STOP)
        }
    }

    private fun getEmotionFilePath(emotion: String?): String? {
        var emotionFilePath = remoteEmotions?.getEmotionFilePath(emotion)
        if (emotionFilePath == null) {
            emotionFilePath = localEmotions?.getEmotionFilePath(emotion)
        }
        return emotionFilePath
    }

    private fun initializeEmotions() {
        localEmotions = LocalEmotionManifest.create(this, R.raw.local_emotion)
        remoteEmotions = RemoteEmotionManifest.create(
            "${filesDir.path}/${DataPackConfigurations.EMOTION_FOLDER_NAME}",
            "manifest.json"
        )
    }

    override fun onCreate() {
        super.onCreate()
        initializeEmotions()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            Timber.d("${intent.action} ${mediaListener != null}")
            when (intent.action) {
                ActionIntent.ACTION_EMOTION_PACK_UPDATED -> {
                    remoteEmotions = RemoteEmotionManifest.create(
                        "${filesDir.path}/${DataPackConfigurations.EMOTION_FOLDER_NAME}",
                        "manifest.json"
                    )
                }
                ActionIntent.ACTION_MEDIA_ON_PLAYER_READY -> {
                    mediaListener?.onPlayerReady()
                }
                ActionIntent.ACTION_MEDIA_ON_PLAY -> {
                    mediaListener?.onPlay()
                }
                ActionIntent.ACTION_MEDIA_ON_PAUSE -> {
                    mediaListener?.onPause()
                }
                ActionIntent.ACTION_MEDIA_ON_STOP -> {
                    mediaListener?.onStop()
                }
                ActionIntent.ACTION_MEDIA_ON_END -> {
                    mediaListener?.onEnd()
                }
                ActionIntent.ACTION_MEDIA_ON_ERROR -> {
                    val error = intent.getStringExtra(JooxWebViewFragment.EXTRA_MEDIA_ERROR)
                    mediaListener?.onError(error)
                }
            }
        } catch (e: DeadObjectException) {
            commandList.add(intent)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaListener = null
        commandList.clear()
    }
}