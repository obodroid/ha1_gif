package com.obodroid.kaitomm.gifplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Insets
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowInsets
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.obodroid.kaitomm.gifplayer.common.configurations.PackageName
import com.obodroid.kaitomm.gifplayer.common.extensions.broadcast
import com.obodroid.kaitomm.gifplayer.common.extensions.registerReceiverFilter
import com.obodroid.kaitomm.gifplayer.common.intents.ActionIntent
import com.obodroid.kaitomm.gifplayer.fragment.ImageViewFragment
import com.obodroid.kaitomm.gifplayer.fragment.VideoFbFragment
import com.obodroid.kaitomm.gifplayer.fragment.VideoFragment
import com.obodroid.kaitomm.gifplayer.fragment.VideoFragmentInterface
import com.obodroid.kaitomm.gifplayer.fragment.VideoFragmentListener
import java.io.Serializable


class VideoActivity : BaseActivity(), VideoFragmentListener {
    private var videoInterface: VideoFragmentInterface? = null

    private lateinit var rootView: ConstraintLayout

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            logger.info("onReceive ${intent?.action}")
            when (intent?.action) {
                ActionIntent.ACTION_PAUSE_VIDEO -> pauseVideo()
                ActionIntent.ACTION_RESUME_VIDEO -> resumeVideo()
                ActionIntent.ACTION_PLAY_VIDEO -> playVideo(intent.extras ?: Bundle())
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        rootView = findViewById(R.id.rootView)

        val extras = intent.extras
        if (extras == null) {
            finish()
            logger.error("no extra")
            return
        }

        if (!playVideo(extras)) {
            finish()
            return
        }

        registerIntentFilter()
    }

    override fun onDestroy() {
        unregisterIntentFilter()
        broadcast(ActionIntent.ACTION_ENDED_VIDEO) {
            setPackage(PackageName.APP_MAIN)
        }
        super.onDestroy()
    }

    // VideoFragmentListener
    override fun videoEnded() {
        finish()
    }

    private fun playVideo(extras: Bundle): Boolean {
        logger.debug("playVideo")
        val width = extras.getString(EXTRA_SCALE_WIDTH)?.toIntOrNull()
        val height = extras.getString(EXTRA_SCALE_HEIGHT)?.toIntOrNull()
        if (width != null && height != null) {
            val windowHeight =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    val windowInsets = windowManager.currentWindowMetrics.windowInsets
                    val insets: Insets = windowInsets.getInsetsIgnoringVisibility(
                        WindowInsets.Type.navigationBars()
                                or WindowInsets.Type.displayCutout()
                    )
                    insets.top + insets.bottom
                } else {
                    val displayMetrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(displayMetrics)
                    displayMetrics.heightPixels
                }

            rootView.layoutParams.width =
                (width.toFloat() / height * windowHeight).toInt()
        }

        // Set main video fragment
        val fragment = replaceFragment(R.id.fragmentContainer, extras, null)
        if (fragment is VideoFragmentInterface) {
            videoInterface = fragment
        }

        // Set other fragment
        viewLoop@ for (index in 0..rootView.childCount) {
            val view = rootView.getChildAt(index)
            val tag = view?.tag as String?
            if (tag.isNullOrBlank()) {
                continue
            }
            replaceFragment(view.id, extras, tag)
        }
        return true
    }

    private fun replaceFragment(viewId: Int, extras: Bundle, tag: String? = null): Fragment? {
        val prefix: String = tag?.let { "$it-" } ?: ""
        val urls = extras.getString("$prefix$EXTRA_URLS") ?: return null
        val option = Option(extras.getString("$prefix$EXTRA_IS_REPEAT")?.toBoolean() ?: false, true)
        return when (extras.getString("$prefix$EXTRA_TYPE")) {
            "facebook-video" -> {
                VideoFbFragment.newInstance(urls, option)
            }
            "video" -> {
                VideoFragment.newInstance(urls, option)
            }
            else -> {
                ImageViewFragment.newInstance(R.drawable.black)
            }
        }.also { fragment ->
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.let {
                supportFragmentManager
                    .beginTransaction()
                    .remove(it)
                    .commit()
            }
            supportFragmentManager
                .beginTransaction()
                .replace(viewId, fragment)
                .commit()
        }
    }

    private fun registerIntentFilter() {
        registerReceiverFilter(
            broadcastReceiver,
            listOf(
                ActionIntent.ACTION_PAUSE_VIDEO,
                ActionIntent.ACTION_RESUME_VIDEO,
                ActionIntent.ACTION_PLAY_VIDEO
            )
        )
    }

    private fun unregisterIntentFilter() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    private fun pauseVideo() {
        videoInterface?.pauseVideo()
    }

    private fun resumeVideo() {
        videoInterface?.resumeVideo()
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_URLS = "urls"
        const val EXTRA_TAG = "tag"
        const val EXTRA_IS_REPEAT = "repeatValue"

        const val EXTRA_SCALE_WIDTH = "displayWidth"
        const val EXTRA_SCALE_HEIGHT = "displayHeight"
    }

    class Option(
        val isRepeat: Boolean = false,
        val isScaleCropInside: Boolean = true
    ) : Serializable
}