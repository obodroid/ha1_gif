package com.obodroid.kaitomm.gifplayer.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.obodroid.kaitomm.gifplayer.common.configurations.PackageName
import com.obodroid.kaitomm.gifplayer.common.extensions.broadcast
import com.obodroid.kaitomm.gifplayer.common.intents.ActionIntent
import com.obodroid.kaitomm.gifplayer.common.util.JsonUtil
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern

class CustomTree(
    private val applicationContext: Context,
    private val packageName: String,
    private val versionCode: Int,
) : Timber.DebugTree() {
    var tagObj: Tag? = null

    override fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.className
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        tag = tag.substring(tag.lastIndexOf('.') + 1)
        tagObj = Tag(tag, element.lineNumber, element.methodName)
        // Tag length limit was removed in API 24.
        return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            "Class: ${tag}, Line: ${element.lineNumber}, Method: ${element.methodName}"
        } else tag.substring(0, MAX_TAG_LENGTH)
    }

    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        if (priority >= android.util.Log.INFO) {
            val time = Date().time
            val log = Log(
                priority,
                message,
                tagObj?.method,
                tagObj?.`class`,
                tagObj?.lineNumber,
                time,
                packageName,
                tag,
                versionCode,
            )
            applicationContext.broadcast(ActionIntent.ACTION_LOG) {
                setPackage(PackageName.APP_MAIN)
                putExtra("log", JsonUtil.toJson(log))
            }
        }
    }

    companion object {
        private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
        private const val MAX_TAG_LENGTH = 50
    }

    data class Tag(
        val `class`: String?,
        val lineNumber: Int?,
        val method: String?
    )
}