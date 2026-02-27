package com.obodroid.kaitomm.gifplayer.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.obodroid.kaitomm.gifplayer.MainViewModel
import com.obodroid.kaitomm.gifplayer.R
import com.obodroid.kaitomm.gifplayer.common.configurations.StatusCode
import com.obodroid.kaitomm.gifplayer.common.extensions.registerReceiverFilter
import com.obodroid.kaitomm.gifplayer.common.extensions.runService
import com.obodroid.kaitomm.gifplayer.common.intents.ActionIntent
import com.obodroid.kaitomm.gifplayer.service.GifPlayerService
import kotlinx.coroutines.*
import org.json.JSONObject
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtension
import timber.log.Timber

class JooxWebViewFragment : Fragment(R.layout.fragment_webview) {

    private lateinit var geckoView: GeckoView

    private var token: String? = null
    private val scope by lazy { CoroutineScope(Job() + Dispatchers.Main + handler) }
    private val viewModel: MainViewModel by activityViewModels()
    private var mPort: WebExtension.Port? = null
    private lateinit var runtime: GeckoRuntime
    private lateinit var session: GeckoSession
    private var isReady = false
    private var jobStartPlayer: Job? = null
    private var isPlaying = false
    private var songId: String? = null
    private var songName: String? = null
    private var replayJob: Job? = null
    private var isCallPause = false
    private var errorCount = 0
    private val permissionDelegate = object : GeckoSession.PermissionDelegate {
        override fun onContentPermissionRequest(
            session: GeckoSession,
            perm: GeckoSession.PermissionDelegate.ContentPermission
        ): GeckoResult<Int>? {
            Timber.d("allow permission")
            return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
        }
    }
    private val portDelegate = object : WebExtension.PortDelegate {
        override fun onPortMessage(message: Any, port: WebExtension.Port) {
            super.onPortMessage(message, port)
            Timber.d("onPortMessage $message")
            if (message is JSONObject) {
                when (message.optString("action")) {
                    "Kaitomm" -> {
                        handlerJooxCallback(message)
                    }
                    "evalJavascript" -> {
                        val data = message.optString("data")
                        when (message.optString("id")) {
                            "artistSong" -> {
                                songName = data
                                viewModel.bottomText.postValue(songName)
                            }
                        }
                    }
                }
            }
        }

        override fun onDisconnect(port: WebExtension.Port) {
            super.onDisconnect(port)
            mPort = null
        }
    }
    private val messageDelegate = object : WebExtension.MessageDelegate {
        override fun onConnect(port: WebExtension.Port) {
            super.onConnect(port)
            mPort = port
            port.setDelegate(portDelegate)
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.d("action: ${intent?.action} songId: $songId")
            when (intent?.action) {
                ActionIntent.ACTION_MEDIA_PLAY -> {
                    val newSongId = intent.getStringExtra(EXTRA_MEDIA_ID)
                    errorCount = 0
                    isPlaying = true
                    if (songId == newSongId) {
                        evaluateJavascript("stop();")
                        evaluateJavascript("resume()")
                        onPlay()
                        Timber.tag("media").i("resume play songId: $songId")
                    } else {
                        songId = newSongId
                        evaluateJavascript("play('$songId');")
                        delayReplay()
                        Timber.tag("media").i("play songId: $songId")
                    }
                }
                ActionIntent.ACTION_MEDIA_PAUSE -> {
                    isCallPause = true
                    replayJob?.cancel()
                    replayJob = null
                    evaluateJavascript("pause();")
                }
                ActionIntent.ACTION_MEDIA_RESUME -> {
                    if (isPlaying) {
                        isCallPause = false
                        evaluateJavascript("resume();")
                        viewModel.bottomText.postValue(songName)
                    }
                }
                ActionIntent.ACTION_MEDIA_STOP -> {
                    replayJob?.cancel()
                    replayJob = null
                    isPlaying = false
                    evaluateJavascript("stop();")
                }
            }
        }
    }

    private val handler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geckoView = view.findViewById(R.id.geckoView)

        token = requireArguments().getString(ARG_TOKEN)
        Timber.d("onViewCreated $token")

        requireContext().registerReceiverFilter(
            broadcastReceiver,
            listOf(
                ActionIntent.ACTION_MEDIA_PLAY,
                ActionIntent.ACTION_MEDIA_PAUSE,
                ActionIntent.ACTION_MEDIA_RESUME,
                ActionIntent.ACTION_MEDIA_STOP,
            )
        )

        setupGeckoView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.tag("media").e("onLowMemory")
        startUrl(session)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        jobStartPlayer?.cancel()
        replayJob?.cancel()
        mPort?.disconnect()
        runtime.shutdown()

        mPort = null
    }

    private fun setupGeckoView() {
        Timber.d("setupGeckoView")
        session = GeckoSession()
        session.permissionDelegate = permissionDelegate
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStop(session: GeckoSession, success: Boolean) {
                super.onPageStop(session, success)
                Timber.d("success: $success")
                if (!success) {
                    startUrl(session)
                    Timber.tag("media").e("Start page result: FAILURE retry")
                }
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                super.onProgressChange(session, progress)
                if (progress == 100) {
                    Timber.d("load done isReady: $isReady jobStartPlayer: ${jobStartPlayer != null}")
                    Timber.tag("media").i("Setup Joox player result: SUCCESS")
                    if (!isReady && jobStartPlayer == null) {
                        jobStartPlayer = scope.launch(Dispatchers.Main) {
                            context?.runService<GifPlayerService>(action = ActionIntent.ACTION_MEDIA_ON_PLAYER_READY)
                        }
                    } else {
                        evaluateJavascript("play('$songId');")
                        delayReplay()
                        Timber.tag("media").i("play old songId: $songId")
                    }
                }
            }
        }
        runtime = GeckoRuntime.create(requireContext())
        runtime.webExtensionController
            .ensureBuiltIn("resource://android/assets/", "messaging@example.com")
            .accept(object : GeckoResult.Consumer<WebExtension> {
                override fun accept(t: WebExtension?) {
                    Timber.d("accept")
                    if (t == null) {
                        Timber.e("WebExtension == null")
                        return
                    }
                    scope.launch {
                        t.setMessageDelegate(messageDelegate, "browser")
                    }
                }

            }) { t -> Timber.e(t, "accept Throwable") }

        session.open(runtime)
        geckoView.setSession(session)
        startUrl(session)
    }

    private fun startUrl(session: GeckoSession) {
        session.stop()
        session.load(
            GeckoSession.Loader()
                .uri(URL_PLAYER)
                .headerFilter(GeckoSession.HEADER_FILTER_UNRESTRICTED_UNSAFE)
                .additionalHeaders(getHeaders())
        )
    }

    private fun getHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $token"
        return headers
    }

    private fun evaluateJavascript(javascriptString: String?, id: String? = null) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("action", "evalJavascript")
            jsonObject.put("data", javascriptString)
            jsonObject.put("id", id ?: System.currentTimeMillis().toString())
            mPort?.postMessage(jsonObject)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun delayReplay(retryCount: Int = 0) {
        replayJob?.cancel()
        replayJob = scope.launch(Dispatchers.Main) {
            delay(DELAY)
            ensureActive()
            if (retryCount > MAX_ERROR_RETRY) {
                evaluateJavascript("resume();")
                Timber.tag("media").i("resend resume play songId: $songId")
            } else {
                evaluateJavascript("play('$songId');")
                delayReplay(retryCount + 1)
                Timber.tag("media").i("resend play songId: $songId")
            }
        }
    }

    private fun returnError(errorCode: Int? = null) {
        replayJob?.cancel()
        replayJob = null
        context?.runService<GifPlayerService>(action = ActionIntent.ACTION_MEDIA_ON_ERROR) {
            putExtra(EXTRA_MEDIA_ERROR, errorCode.toString())
        }
    }

    private fun handlerJooxCallback(message: JSONObject) {
        val topic = message.optString("topic", "")
        Timber.tag("media").i("callback topic: $topic")
        when (topic) {
            "onEnd" -> {
                onEnd()
            }
            "onPlay" -> {
                isReady = true
                onPlay()
            }
            "onPause" -> {
                if (isCallPause) {
                    isCallPause = false
                    context?.runService<GifPlayerService>(action = ActionIntent.ACTION_MEDIA_ON_PAUSE)
                } else {
                    onEnd()
                }
            }
            "onStop" -> {
                context?.runService<GifPlayerService>(action = ActionIntent.ACTION_MEDIA_ON_STOP)
            }
            "onError" -> {
                val dataString = message.optString("data", "{}")
                val data = JSONObject(dataString)
                val status = data.optInt("status")
                var code: Int? = null
                var removeFragment = false
                val statusCode: Int? = when (status) {
                    500 -> {
                        if (errorCount > MAX_ERROR_RETRY) {
                            StatusCode.ERROR_UNKNOWN
                        } else {
                            code = data.optJSONObject("data")
                                ?.optJSONObject("detail")
                                ?.optInt("error_code")
                            when (code) {
                                9009002 -> {
                                    StatusCode.ERROR_MEDIA_CAN_NOT_PLAY
                                }
                                -400 -> {
                                    removeFragment = true
                                    StatusCode.ERROR_MEDIA_PLAYER
                                }
                                else -> {
                                    errorCount++
                                    null
                                }
                            }
                        }
                    }
                    else -> {
                        StatusCode.ERROR_UNKNOWN
                    }
                }
                Timber.e("onError status: $status code: $code statusCode: $statusCode removeFragment: $removeFragment")
                if (statusCode != null) {
                    returnError(statusCode)
                }
                if (removeFragment) {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .remove(this@JooxWebViewFragment)
                        .commit()
                }
            }
            else -> {
            }
        }
    }

    private fun onPlay() {
        replayJob?.cancel()
        replayJob = null
        context?.runService<GifPlayerService>(action = ActionIntent.ACTION_MEDIA_ON_PLAY)
        evaluateJavascript(
            "`\${jooxPlayer.params.songData.artist_list[0].name} - \${jooxPlayer.params.songData.name}`;",
            "artistSong"
        )
    }

    private fun onEnd() {
        isPlaying = false
        songName = null
        viewModel.bottomText.postValue("")
        context?.runService<GifPlayerService>(action = ActionIntent.ACTION_MEDIA_ON_END)
    }

    companion object {
        private const val URL_PLAYER = "https://www.joox.com/kaitomm/player/"
        const val ARG_TOKEN = "token"

        const val EXTRA_MEDIA_ID = "id"
        const val EXTRA_MEDIA_ERROR = "mediaError"

        const val MAX_ERROR_RETRY = 5
        const val DELAY = 15_000L
    }
}