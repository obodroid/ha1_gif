package com.obodroid.kaitomm.gifplayer.service

import android.content.Context
import android.content.Intent
import com.obodroid.kaitomm.gifplayer.common.intents.ActionIntent
import com.obodroid.kaitomm.gifplayer.common.extensions.localBroadcast

class PackageInstallBroadcastReceiver : BaseBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            "installation_in_progress" -> {
//                context.stopService<GifPlayerService>()
//                context.localBroadcast(ActionIntent.ACTION_FINISH)
            }
            "installation_finish" -> {
                context.localBroadcast(ActionIntent.ACTION_FINISH)
            }
        }
    }
}