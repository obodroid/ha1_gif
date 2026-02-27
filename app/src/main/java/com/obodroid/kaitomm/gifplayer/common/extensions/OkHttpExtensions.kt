package com.obodroid.kaitomm.gifplayer.common.extensions

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun OkHttpClient.Builder.enableTrustAllCerts() = apply {
    try {
        val trustAllCerts =
            arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate?> {
                        return arrayOf()
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        certs: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        certs: Array<X509Certificate>,
                        authType: String
                    ) {
                    }
                }
            )
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, null)
        val hostnameVerifier = HostnameVerifier { _, _ -> true }
        hostnameVerifier(hostnameVerifier)
        sslSocketFactory(
            sslContext.socketFactory,
            trustAllCerts[0] as X509TrustManager
        )
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: KeyManagementException) {
        e.printStackTrace()
    }
}