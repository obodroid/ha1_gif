package com.obodroid.kaitomm.gifplayer.common.extensions

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.reflect.Type

object JsonUtil {

    val gson = Gson()

    fun toJson(src: Any): String = gson.toJson(src)
    val jsonParser = JsonParser()

    fun parse(jsonString: String): String = gson.toJson(jsonParser.parse(jsonString))

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