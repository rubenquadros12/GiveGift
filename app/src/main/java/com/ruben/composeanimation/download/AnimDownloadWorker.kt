package com.ruben.composeanimation.download

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ruben.composeanimation.R
import com.ruben.composeanimation.data.DownloadRepo
import com.ruben.composeanimation.download.models.GiftInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Ruben Quadros on 08/12/21
 **/
@HiltWorker
class AnimDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
): CoroutineWorker(context, params) {

    companion object {

        const val TAG = "AnimDownloadWorker"

        fun enqueueAssetDownloadWork(context: Context, giftId: String, url: String) {
            val data = workDataOf(
                "giftId" to giftId,
                "giftUrl" to url
            )

            val workRequest = OneTimeWorkRequestBuilder<AnimDownloadWorker>()
                .setInputData(data)
                .addTag(TAG + giftId)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(TAG + giftId, ExistingWorkPolicy.KEEP, workRequest)
        }
    }

    @Inject
    lateinit var downloadRepo: DownloadRepo

    override suspend fun doWork(): Result {
        val giftId = inputData.getString("giftId").orEmpty()
        val giftUrl = inputData.getString("giftUrl").orEmpty()

        Log.d("Ruben", "Start work $giftId, $giftUrl")

        if (giftId.isEmpty() || giftUrl.isEmpty()) {
            //failed
            Log.d("Ruben", "fail work $giftId")
            return Result.failure()
        }

        return withContext(Dispatchers.IO) {
            val response = downloadRepo.downloadAsset(giftId = giftId, url = giftUrl)
            if (response != null) {
                val output = workDataOf(
                    "giftId" to giftId,
                    "giftUrl" to giftUrl,
                    "localPath" to response
                )
                Log.d("Ruben", "Success work $giftId")
                Result.success(output)
            } else {
                Log.d("Ruben", "Fail retry work $giftId")
                Result.retry()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            applicationContext,
            "101"
        )
            .setContentTitle("OK")
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                101,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                101,
                notification
            )
        }
    }
}