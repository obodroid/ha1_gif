package com.obodroid.kaitomm.gifplayer.data.repository

import com.obodroid.kaitomm.gifplayer.common.RetrofitFactory
import com.obodroid.kaitomm.gifplayer.common.extensions.applySchedulers
import com.obodroid.kaitomm.gifplayer.common.util.SharePreferenceWrapper
import com.obodroid.kaitomm.gifplayer.data.apicontroller.TokenApiController
import io.reactivex.Completable
import java.util.*
import java.util.concurrent.TimeUnit

class TokenRepository {

    private val tokenApiController: TokenApiController by lazy {
        RetrofitFactory.getAuthApiController(TokenApiController::class.java)
    }

    fun getToken(): String = SharePreferenceWrapper.getString(TOKEN_KEY, "")

    private fun setToken(token: String, validTimeInSecond: Long) {
        SharePreferenceWrapper.putString(TOKEN_KEY, token)

        val expireTime =
            Date().time + TimeUnit.MILLISECONDS.convert(validTimeInSecond, TimeUnit.SECONDS)
        SharePreferenceWrapper.putLong(TOKEN_EXPIRED_TIME_KEY, expireTime)
    }

    fun clearToken() {
        SharePreferenceWrapper.remove(TOKEN_KEY)
        SharePreferenceWrapper.remove(TOKEN_EXPIRED_TIME_KEY)
    }

    fun needRefresh(): Boolean {
        val expiredTime = SharePreferenceWrapper.getLong(TOKEN_EXPIRED_TIME_KEY, 0L)
        val current = Date().time

        return (expiredTime - current) < TOKEN_DIFF_IN_SECOND * 1000
    }

    fun refreshToken(): Completable {
        return tokenApiController.grantToken(GRANT_TYPE, USER_ID, USER_SECRET_KEY)
            .applySchedulers()
            .flatMapCompletable {
                setToken("${it.tokenType} ${it.token}", it.validTimeInSecond)
                null
            }
    }

    companion object {

        private const val TOKEN_KEY = "kaitom-token"
        private const val TOKEN_EXPIRED_TIME_KEY = "kaitom-token-expire"
        private const val TOKEN_DIFF_IN_SECOND = 45

        private const val GRANT_TYPE = "client_credentials"
        private const val USER_ID = "kaitom-269bbfebb0d639f3"
        private const val USER_SECRET_KEY =
            "a3707aed741382be82b128ef001d3dee9c021643c7c1b0b9f70dd171295c2587"
    }
}