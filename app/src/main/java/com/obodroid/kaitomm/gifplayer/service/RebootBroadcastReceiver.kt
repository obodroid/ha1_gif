package com.obodroid.kaitomm.gifplayer.service

import android.content.Context
import android.content.Intent


class RebootBroadcastReceiver : BaseBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        logger.debug("ACTION: -------------------------> ${intent?.action}")
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
//                context.startActivity<MainActivity> {
//                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                }
            }
        }
    }
}