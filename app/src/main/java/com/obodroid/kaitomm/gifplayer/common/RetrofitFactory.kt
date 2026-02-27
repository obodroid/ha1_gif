package com.obodroid.kaitomm.gifplayer.common

import android.util.Log
import com.obodroid.kaitomm.gifplayer.BuildConfig
import com.obodroid.kaitomm.gifplayer.common.extensions.enableTrustAllCerts
import com.obodroid.kaitomm.gifplayer.data.interceptor.TokenRequestHeaderInterceptor
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {

    private const val API_BASE_URL = BuildConfig.API_BASE_URL
    private const val AUTH_BASE_URL = BuildConfig.AUTH_BASE_URL

    private val apiRetrofit: Retrofit by lazy {
        val builder = createBaseOkHttpClientBuilder()
        builder.addInterceptor(TokenRequestHeaderInterceptor())

        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(builder.build())
            .baseUrl(API_BASE_URL)
            .build()
    }

    private val dynamicApiRetrofit: Retrofit by lazy {
        val builder = createBaseOkHttpClientBuilder()

        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(builder.build())
            .baseUrl("http://url")
            .build()
    }

    private val authRetrofit: Retrofit by lazy {
        val builder = createBaseOkHttpClientBuilder()

        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(builder.build())
            .baseUrl(AUTH_BASE_URL)
            .build()
    }

    fun <T> getApiController(clazz: Class<T>): T {
        return apiRetrofit.create(clazz)
    }

    fun <T> getDynamicApiController(clazz: Class<T>): T {
        return dynamicApiRetrofit.create(clazz)
    }

    fun <T> getAuthApiController(clazz: Class<T>): T {
        return authRetrofit.create(clazz)
    }

    private val interceptor by lazy {
        logging.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val logging by lazy {
        HttpLoggingInterceptor { message -> Log.d("OkHttp", message) }
    }

    private fun createBaseOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .enableTrustAllCerts()
    }

    private const val TIMEOUT = 20L
}