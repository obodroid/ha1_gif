package com.obodroid.kaitomm.gifplayer.data.interceptor

import android.annotation.SuppressLint
import com.obodroid.kaitomm.gifplayer.MainApplication
import com.obodroid.kaitomm.gifplayer.common.util.SharePreferenceWrapper
import com.obodroid.kaitomm.gifplayer.data.repository.TokenRepository
import com.obodroid.kaitomm.gifplayer.common.LoggerFactory
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class TokenRequestHeaderInterceptor : Interceptor {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val tokenManager = TokenRepository()

    @SuppressLint("CheckResult")
    override fun intercept(chain: Interceptor.Chain): Response {
        val origin = chain.request()

        if (tokenManager.needRefresh()) {
            try {
                logger.debug("Refreshing token..")
                tokenManager.refreshToken().blockingGet(15, TimeUnit.SECONDS)
            } catch (e: RuntimeException) {
                tokenManager.clearToken()
                e.printStackTrace()
            }
        }

        val token = tokenManager.getToken()
        if (token.isEmpty()) {
            return chain.proceed(origin)
        }

        val request = origin.newBuilder()
            .header("Authorization", token)
            .header("x-mac-address", SharePreferenceWrapper.getString(MainApplication.DEVICE_ID_KEY))
            .build()

        return chain.proceed(request)
    }
}