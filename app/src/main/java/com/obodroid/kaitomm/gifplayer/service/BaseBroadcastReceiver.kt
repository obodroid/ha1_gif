package com.obodroid.kaitomm.gifplayer.service

import android.content.BroadcastReceiver
import com.obodroid.kaitomm.gifplayer.common.LoggerFactory


abstract class BaseBroadcastReceiver: BroadcastReceiver() {

    protected val logger = LoggerFactory.getLogger(this.javaClass.name)
}