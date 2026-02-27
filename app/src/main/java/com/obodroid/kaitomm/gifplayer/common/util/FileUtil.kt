package com.obodroid.kaitomm.gifplayer.common.util

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.collections.ArrayList


object FileUtil {

    fun saveToFile(source: ByteArray, destinationPath: String, filename: String) {
        try {
            val folder = File(destinationPath)
            if (!folder.exists()) {
                folder.mkdir()
            }

            val outputStream = FileOutputStream("$destinationPath/$filename")
            outputStream.write(source, 0, source.size)
            outputStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun writeStringToFile(context: Context, filename: String, sourceString: String) {
        try {
            val writer = OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE))
            writer.write(sourceString)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadListFromFile(inputStream: InputStream): ArrayList<String> {
        return try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val labels = ArrayList(reader.readLines())
            reader.close()
            labels
        } catch (exception: IOException) {
            ArrayList()
        }
    }

    fun loadDataFromFile(filePath: String): ByteArray {
        val file = File(filePath)
        return loadDataFromFile(file)
    }

    private fun loadDataFromFile(file: File): ByteArray {
        val data = ByteArray(file.length().toInt())
        try {
            val inputStream = FileInputStream(file)
            inputStream.read(data)
            inputStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return data
    }

    fun unzip(inputStream: InputStream, destinationPath: String): Boolean {
        return unzip(ZipInputStream(inputStream), destinationPath)
    }

    private fun unzip(zipInputStream: ZipInputStream, destinationPath: String): Boolean {
        File(destinationPath).apply {
            if (exists()) delete()
            mkdir()
        }

        try {
            zipInputStream.use { zin ->
                var entry = zin.nextEntry
                while (entry != null) {
                    if (entry.isDirectory) {
                        File(destinationPath, entry.name).apply {
                            if (exists()) delete()
                            mkdir()
                        }
                    } else {
                        FileOutputStream(File(destinationPath, entry.name)).use { outputStream ->
                            val buffer = ByteArray(2048)
                            var len = zin.read(buffer)

                            while (len != -1) {
                                outputStream.write(buffer, 0, len)
                                len = zin.read(buffer)
                            }
                            zin.closeEntry()
                        }
                    }
                    entry = zin.nextEntry
                }
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun loadStringFromFile(file: File): String {
        val bufferedReader = BufferedReader(FileReader(file))
        return bufferedReader.use { it.readText() }
    }

    fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyCameraApp"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    return null
                }
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return when (type) {
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
            }
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
            }
            else -> null
        }
    }
}
