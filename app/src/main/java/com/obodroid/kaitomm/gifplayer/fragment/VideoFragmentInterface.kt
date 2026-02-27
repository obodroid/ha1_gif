package com.obodroid.kaitomm.gifplayer.fragment

import androidx.fragment.app.Fragment

abstract class VideoFragmentInterface : Fragment() {
    abstract fun pauseVideo()

    abstract fun resumeVideo()
}

interface VideoFragmentListener {
    fun videoEnded()
}
