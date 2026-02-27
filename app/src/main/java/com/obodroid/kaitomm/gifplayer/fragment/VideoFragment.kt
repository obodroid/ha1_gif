package com.obodroid.kaitomm.gifplayer.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.JsonParser
import com.obodroid.kaitomm.gifplayer.R
import com.obodroid.kaitomm.gifplayer.VideoActivity


private const val ARG_URLS = "urls"
private const val ARG_OPTION = "option"

class VideoFragment : VideoFragmentInterface() {
    private var player: SimpleExoPlayer? = null
    private var urls: String? = null
    private var option: VideoActivity.Option? = null
    private var listener: VideoFragmentListener? = null

    private lateinit var videoView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            urls = it.getString(ARG_URLS)
            option = it.getSerializable(ARG_OPTION) as VideoActivity.Option
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is VideoFragmentListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoView = view.findViewById(R.id.videoView)

        val urlList = JsonParser().parse(urls).asJsonArray

        videoView.hideController()
        videoView.controllerAutoShow = false
        videoView.useController = false

        player = SimpleExoPlayer.Builder(requireContext()).build()
        videoView.player = player

        if (option?.isScaleCropInside == true) {
            videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        }

        player?.playWhenReady = true
        player?.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_ENDED -> {
                        listener?.videoEnded()
                    }
                }
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                listener?.videoEnded()
            }
        })

        val mediaSource = ConcatenatingMediaSource()

        for (url in urlList) {
            val uri = Uri.parse(url.asString)
            val userAgent =
                Util.getUserAgent(requireContext(), resources.getString(R.string.app_name))
            val videoSource =
                if (uri.lastPathSegment?.contains("mp3") == true || uri.lastPathSegment?.contains("mp4") == true) {
                    val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(requireContext(), resources.getString(R.string.app_name))
                    )
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(url.asString))
                } else if (uri.lastPathSegment?.contains("m3u8") == true) {
                    val dataSourceFactory: DataSource.Factory =
                        DefaultHttpDataSourceFactory(userAgent)
                    HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri)
                } else {
                    val dataSourceFactory: DataSource.Factory =
                        DefaultHttpDataSourceFactory(userAgent)
                    val dashChunkSourceFactory = DefaultDashChunkSource.Factory(dataSourceFactory)
                    val manifestDataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
                    DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory)
                        .createMediaSource(uri)
                }
            mediaSource.addMediaSource(videoSource)
        }

        // Prepare the player with the source.
        player?.prepare(mediaSource)
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    // VideoFragmentInterface
    override fun pauseVideo() {
        player?.playWhenReady = false
    }

    override fun resumeVideo() {
        player?.playWhenReady = true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param urls Json Array String.
         * @return A new instance of fragment VideoFragment.
         */
        @JvmStatic
        fun newInstance(urls: String, option: VideoActivity.Option) =
            VideoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URLS, urls)
                    putSerializable(ARG_OPTION, option)
                }
            }
    }
}