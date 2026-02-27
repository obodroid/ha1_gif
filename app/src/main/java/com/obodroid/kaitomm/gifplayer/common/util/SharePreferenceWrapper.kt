package com.obodroid.kaitomm.gifplayer.common.util

import android.content.Context
import android.content.SharedPreferences

object SharePreferenceWrapper {

    private lateinit var preferences: SharedPreferences
    private const val NAME = "Kaitom"

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int = preferences.getInt(key, defaultValue)
    fun putInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()

    fun getLong(key: String, defaultValue: Long = 0L): Long = preferences.getLong(key, defaultValue)
    fun putLong(key: String, value: Long) = preferences.edit().putLong(key, value).apply()

    fun getString(key: String, defaultValue: String = ""): String = preferences.getString(key, defaultValue) ?: defaultValue
    fun putString(key: String, value: String) = preferences.edit().putString(key, value).apply()

    fun remove(key: String) = preferences.edit().remove(key).apply()
}