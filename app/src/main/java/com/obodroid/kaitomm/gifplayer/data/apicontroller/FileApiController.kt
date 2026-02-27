package com.obodroid.kaitomm.gifplayer.data.apicontroller

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FileApiController {

    @Streaming
    @GET
    fun downloadFileStream(@Url url: String): Single<Response<ResponseBody>>
}