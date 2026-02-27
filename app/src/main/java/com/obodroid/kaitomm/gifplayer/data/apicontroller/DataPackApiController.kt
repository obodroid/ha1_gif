package com.obodroid.kaitomm.gifplayer.data.apicontroller

import com.obodroid.kaitomm.gifplayer.data.response.PackDto
import io.reactivex.Single
import retrofit2.http.GET

interface DataPackApiController {

    @GET("emotionPack")
    fun getEmotionPack(): Single<PackDto>
}