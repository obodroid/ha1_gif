package com.obodroid.kaitomm.gifplayer

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.text.format.Formatter
import androidx.multidex.MultiDexApplication
import com.facebook.drawee.backends.pipeline.Fresco
import com.obodroid.kaitomm.gifplayer.common.CustomTree
import com.obodroid.kaitomm.gifplayer.common.RetrofitFactory
import com.obodroid.kaitomm.gifplayer.common.extensions.runService
import com.obodroid.kaitomm.gifplayer.common.util.SharePreferenceWrapper
import com.obodroid.kaitomm.gifplayer.data.apicontroller.PushNotificationMappingApiController
import com.obodroid.kaitomm.gifplayer.data.repository.DeviceRepository
import com.obodroid.kaitomm.gifplayer.service.GifPlayerService
import timber.log.Timber
import java.net.NetworkInterface
import java.util.*

class MainApplication : MultiDexApplication() {

    var wifiManager: WifiManager? = null
    var info: WifiInfo? = null
    val preference by lazy {
        getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
    val deviceRepository by lazy {
        DeviceRepository(preference)
    }
    val pushNotificationApiController by lazy {
        RetrofitFactory.getApiController(PushNotificationMappingApiController::class.java)
    }

    override fun onCreate() {
        super.onCreate()

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        info = wifiManager?.connectionInfo

        SharePreferenceWrapper.init(this)
        SharePreferenceWrapper.putString(
            DEVICE_ID_KEY,
            getDeviceId().replace(":", "").toLowerCase(Locale.US)
        )
        Fresco.initialize(this)
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+7"))

        SharePreferenceWrapper.putString(PACKAGE_NAME, packageName)
        SharePreferenceWrapper.putString("SSID", getWifiSSID())
        SharePreferenceWrapper.putString("PRIVATE_IP", getPrivateIp())

        if (Timber.treeCount() == 0) {
            Timber.plant(CustomTree(this, packageName, BuildConfig.VERSION_CODE))
        }
        Thread.setDefaultUncaughtExceptionHandler { _, error ->
            Timber.e("Global catch --> Restarting GifPlayerService")
            Timber.e(error)
            runService<GifPlayerService>()
        }
    }

    private fun getWifiSSID(): String {
        return info!!.ssid.replace("\"", "")
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String {
        val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        var address = info.macAddress
        if (address == null || address == "02:00:00:00:00:00") {
            try {
                val all = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (nif in all) {
                    if (!nif.name.equals("wlan0", ignoreCase = true)) continue

                    val macBytes = nif.hardwareAddress ?: return ""

                    val res1 = StringBuilder()
                    for (b in macBytes) {
                        //res1.append(Integer.toHexString(b & 0xFF) + ":");
                        res1.append(String.format("%02X:", b))
                    }

                    if (res1.isNotEmpty()) {
                        res1.deleteCharAt(res1.length - 1)
                    }
                    address = res1.toString()
                    break
                }
            } catch (ignored: Exception) {
            }
        }
        if (address == null) {
            return "02:00:00:00:00:00"
        }
        return address
    }

    private fun getPrivateIp(): String {
        return Formatter.formatIpAddress(wifiManager?.dhcpInfo!!.ipAddress)
    }

    companion object {
        var DEVICE_ID_KEY = "DEVICE_ID_KEY"
        const val PREFERENCE_NAME = "Kaitom"
    }
}
