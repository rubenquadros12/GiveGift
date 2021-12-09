package com.ruben.composeanimation.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ArrayCreatingInputMerger
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Created by Ruben Quadros on 09/12/21
 **/
@HiltWorker
class AudioAnimDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {

        const val TAG = "AudioAnimDownloadWorker"
        const val AUDIO_TAG = "AudioAnimDownloadWorker_Audio"
        const val ANIM_TAG = "AudioAnimDownloadWorker_Anim"

        fun enqueueAssetDownloadWork(context: Context, giftId: String, audioUrl: String, animUrl: String) {

            val audioData = workDataOf(
                "giftId" to giftId,
                "audioUrl" to audioUrl
            )
            val audioWorkRequest = OneTimeWorkRequestBuilder<AudioAnimDownloadWorker>()
                .setInputData(audioData)
                .addTag(AUDIO_TAG + giftId)
                .build()

            val animData = workDataOf(
                "giftId" to giftId,
                "animUrl" to animUrl
            )
            val animWorkRequest = OneTimeWorkRequestBuilder<AudioAnimDownloadWorker>()
                .setInputData(animData)
                .addTag(ANIM_TAG + giftId)
                .build()

            val insertWorkRequest = OneTimeWorkRequestBuilder<AudioAnimDownloadWorker>()
                .setInputMerger(ArrayCreatingInputMerger::class.java)
                .addTag(TAG + giftId)
                .build()

            WorkManager.getInstance(context).beginWith(listOf(audioWorkRequest, animWorkRequest))
                .then(insertWorkRequest)
                .enqueue()
        }

    }

    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }
}