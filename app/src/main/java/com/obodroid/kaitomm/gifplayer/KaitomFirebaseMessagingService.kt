package com.obodroid.kaitomm.gifplayer

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.obodroid.kaitomm.gifplayer.common.LoggerFactory
import com.obodroid.kaitomm.gifplayer.common.RetrofitFactory
import com.obodroid.kaitomm.gifplayer.common.configurations.PackageName
import com.obodroid.kaitomm.gifplayer.common.extensions.broadcast
import com.obodroid.kaitomm.gifplayer.common.extensions.subscribeEx
import com.obodroid.kaitomm.gifplayer.data.apicontroller.PushNotificationMappingApiController
import io.reactivex.schedulers.Schedulers

class KaitomFirebaseMessagingService : FirebaseMessagingService() {

    val logger = LoggerFactory.getLogger(javaClass.name)

    private val pushNotificationApiController by lazy {
        RetrofitFactory.getApiController(PushNotificationMappingApiController::class.java)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        logger.debug("${message.data}")

        broadcast("com.obodroid.kaitomm.action.PUSH_FIREBASE") {
            setPackage(PackageName.APP_MAIN)
            val data = message.data
            for (item in data) {
                putExtra(item.key, item.value)
            }
        }

    }

    override fun onNewToken(token: String) {
        pushNotificationApiController.registerPushnotification(token)
            .subscribeOn(Schedulers.io())
            .subscribeEx(
                onSuccess = {
                    logger.debug("Renew push notification with firebase token: $token success!")
                }
            )
    }
}