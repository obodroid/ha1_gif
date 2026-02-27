package com.obodroid.kaitomm.gifplayer.data.apicontroller

import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.http.Query

interface PushNotificationMappingApiController {

    @POST("register/robots")
    fun registerPushnotification(@Query("registerId") registerId: String): Single<Any>
}