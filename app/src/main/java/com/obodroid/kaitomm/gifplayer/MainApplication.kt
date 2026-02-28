package com.obodroid.kaitomm.gifplayer

import android.app.Application
import java.util.*

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+7"))
    }
}
