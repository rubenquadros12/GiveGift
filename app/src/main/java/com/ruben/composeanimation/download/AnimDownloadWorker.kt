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
import com.ruben.composeanimation.data.GiftStatus
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

        fun enqueueAssetDownloadWork(context: Context, giftId: String, animUrl: String, audioUrl: String? = null) {
            val data = workDataOf(
                "giftId" to giftId,
                "giftUrl" to animUrl,
                "audioUrl" to audioUrl
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
        val audioUrl = inputData.getString("audioUrl")

        Log.d("Ruben", "Start work $giftId, $giftUrl")

        if (giftId.isEmpty() || giftUrl.isEmpty()) {
            //failed
            Log.d("Ruben", "fail work $giftId")
            return Result.failure()
        }

        return withContext(Dispatchers.IO) {
            val animResponse = downloadRepo.downloadAsset(giftId = giftId, url = giftUrl)
            val audioResponse = audioUrl?.let { downloadRepo.downloadAsset(giftId = giftId, url = it) }
            when {
                animResponse == null -> {
                    //failed as anim asset not downloaded
                    Result.retry()
                }
                audioUrl != null && audioResponse == null -> {
                    //failed as audio asset not downloaded
                    Result.retry()
                }
                else -> {
                    val output = workDataOf(
                        "giftId" to giftId,
                        "giftUrl" to giftUrl,
                        "localAnimPath" to animResponse,
                    )
                    Log.d("Ruben", "Success work $giftId")
                    downloadRepo.updateDB(giftId = giftId, giftStatus = GiftStatus.DOWNLOADED, animLocation = animResponse, audioLocation = audioResponse)
                    Result.success(output)
                }
            }
        }
    }
}