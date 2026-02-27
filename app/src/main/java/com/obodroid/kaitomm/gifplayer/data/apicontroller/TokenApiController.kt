package com.obodroid.kaitomm.gifplayer.data.apicontroller

import com.obodroid.kaitomm.gifplayer.data.response.TokenDto
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TokenApiController {

    @FormUrlEncoded
    @POST("token")
    fun grantToken(
        @Field("grant_type") type: String,
        @Field("client_id") id: String,
        @Field("client_secret") secret: String
    ): Single<TokenDto>
}