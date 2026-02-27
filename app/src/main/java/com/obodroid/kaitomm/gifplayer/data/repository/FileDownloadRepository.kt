package com.obodroid.kaitomm.gifplayer.data.repository

import com.obodroid.kaitomm.gifplayer.data.apicontroller.FileApiController
import com.obodroid.kaitomm.gifplayer.common.RetrofitFactory
import com.obodroid.kaitomm.gifplayer.common.util.FileUtil
import io.reactivex.Single

class FileDownloadRepository {

    private val fileController: FileApiController by lazy {
        RetrofitFactory.getDynamicApiController(FileApiController::class.java)
    }

    fun downloadFileStream(url: String, destinationPath: String): Single<Boolean> {
        return fileController.downloadFileStream(url)
                .map { response ->
                    response.body()?.let { body ->
                        FileUtil.unzip(body.byteStream(), destinationPath)
                    } ?: false
                }
    }
}