package com.ruben.composeanimation.data

import android.util.Log
import com.ruben.composeanimation.download.GiftCache
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import okhttp3.ResponseBody
import retrofit2.await

/**
 * Created by Ruben Quadros on 08/12/21
 **/
class DownloadRepo @Inject constructor(
    private val downloadService: DownloadService,
    private val giftCache: GiftCache,
    private val dbHelper: DbHelper
) {
    suspend fun downloadAsset(url: String, giftId: String): String? {
        return try {
            //downloading resource now
            updateDB(giftId, GiftStatus.DOWNLOADING)
            val response = downloadService.downloadAsset(url).await()
            Log.d("Ruben", "Resposne success")
            val cacheResponse = writeToDisk(response, giftId)
            if (cacheResponse != null) {
                updateDB(giftId, GiftStatus.DOWNLOADED, cacheResponse)
            } else {
                updateDB(giftId, GiftStatus.NOT_PRESENT)
            }
            cacheResponse
        } catch (e: Exception) {
            Log.d("Ruben", "Resposne fail $e")
            updateDB(giftId, GiftStatus.NOT_PRESENT)
            null
        }
    }

    private suspend fun updateDB(giftId: String, giftStatus: GiftStatus, animLocation: String = "", audioLocation: String? = null) {
        dbHelper.updateGiftDownloadStatus(giftId, giftStatus, animLocation, audioLocation, System.currentTimeMillis())
    }

    private fun writeToDisk(body: ResponseBody, giftId: String): String? {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            //need to figure out extension from download url
            val newFile = File(giftCache.getCacheDirectory(), "${giftId}.png")
            val fileReader = ByteArray(4096)
            val fileSize = body.contentLength()

            Log.d("Ruben", "file size $fileSize")

            var fileSizeDownloaded = 0

            inputStream = body.byteStream()
            outputStream = FileOutputStream(newFile)

            while (true) {
                val read = inputStream.read(fileReader)

                if (read == -1) break

                outputStream.write(fileReader, 0, read)

                fileSizeDownloaded += read

            }

            outputStream.flush()
            return newFile.absolutePath

        } catch (e: Exception) {
            Log.d("Ruben", "error $e")
            return null
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

}