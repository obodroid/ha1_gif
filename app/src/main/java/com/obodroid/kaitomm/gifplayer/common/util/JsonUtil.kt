package com.obodroid.kaitomm.gifplayer.common.util

import android.content.res.Resources
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.reflect.Type


object JsonUtil {

    val gson = Gson()
    fun toJson(src: Any): String = gson.toJson(src)
    fun isValidJson(jsonString: String): Boolean {
        try {
            JSONObject(jsonString)
        } catch (ex: JSONException) {
            try {
                JSONArray(jsonString)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }

//    fun toJson(src: Any): String = gson.toJson(src)
//    inline fun <reified T> fromJson(jsonString: String): T = gson.fromJson(jsonString, T::class.java)

    class Response(json: String) : JSONObject(json) {
        val type: String? = this.optString("type")
        val data = this.optJSONArray("data")
            ?.let { 0.until(it.length()).map { i -> it.optJSONObject(i) } } // returns an array of JSONObject
            ?.map { Foo(it.toString()) } // transforms each JSONObject of the array into Foo
    }

    class Foo(json: String) : JSONObject(json) {
        val id = this.optInt("id")
        val title: String? = this.optString("title")
    }

    fun stringify(jsonObject: JSONObject): String {
        return jsonObject.toString()
    }

    fun <T> loadJsonFromRaw(resource: Resources, resourceId: Int, clazz: Class<T>): T {
        val jsonString = resource.openRawResource(resourceId).bufferedReader().use { it.readText() }
        return gson.fromJson(jsonString, clazz)
    }

    fun <T> loadJsonFromFile(filePath: String, clazz: Class<T>): T? {
        return try {
            val reader = BufferedReader(FileReader(filePath))
            gson.fromJson(reader, clazz)
        } catch (e: IOException) {
            null
        }
    }

    inline fun <reified T> loadJsonArrayFromFile(filePath: String, type: Type): T? {
        return try {
            val reader = BufferedReader(FileReader(filePath))
            gson.fromJson(reader, type)
        } catch (e: IOException) {
            null
        }
    }
}