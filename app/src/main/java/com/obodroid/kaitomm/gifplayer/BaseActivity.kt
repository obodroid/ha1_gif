package com.obodroid.kaitomm.gifplayer

import androidx.appcompat.app.AppCompatActivity
import com.obodroid.kaitomm.gifplayer.common.LoggerFactory

abstract class BaseActivity : AppCompatActivity() {

    protected val logger = LoggerFactory.getLogger(this.javaClass.name)
}