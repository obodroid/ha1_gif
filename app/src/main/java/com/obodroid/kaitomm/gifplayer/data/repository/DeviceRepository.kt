package com.obodroid.kaitomm.gifplayer.data.repository

import android.content.SharedPreferences
import com.obodroid.kaitomm.gifplayer.common.setting.SettingKey

class DeviceRepository(
    private val preferences: SharedPreferences
) {
    fun getString(key: String) = preferences.getString(key, null)
    fun putString(key: String, value: String) = preferences.edit().putString(key, value).apply()

    fun getInt(key: String, defValue: Int) = preferences.getInt(key, defValue)
    fun putInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()

    fun getBoolean(key: String, defValue: Boolean) = preferences.getBoolean(key, defValue)
    fun putBoolean(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).apply()

    fun getStringSet(key: String) = preferences.getStringSet(key, setOf())
    fun putStringSet(key: String, value: Set<String>) =
        preferences.edit().putStringSet(key, value).apply()

    fun getSsid() = getString(KEY_SSID)
    fun updateSsid(value: String) = putString(KEY_SSID, value)

    fun getPrivateIp() = getString(KEY_PRIVATE_IP)
    fun updatePrivateIp(value: String) = putString(KEY_PRIVATE_IP, value)

    fun getStatus(key: SettingKey) = getString(key.name) ?: "UNKNOWN"
    fun updateStatus(key: SettingKey, value: String) = putString(key.name, value)

    fun getFirebaseInstanceId() = getString(KEY_FIREBASE_INSTANCE_ID)
    fun updateFirebaseInstanceId(value: String) = putString(KEY_FIREBASE_INSTANCE_ID, value)

    companion object {
        private const val KEY_SSID = "SSID"
        private const val KEY_PRIVATE_IP = "PRIVATE_IP"

        private const val KEY_FIREBASE_INSTANCE_ID = "FirebaseInstanceId"
    }
}