package com.ruben.composeanimation.download

import android.content.Context
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.ruben.composeanimation.data.DbHelper
import com.ruben.composeanimation.data.GiftAnimation
import com.ruben.composeanimation.data.GiftStatus
import com.ruben.composeanimation.download.models.DownloadResult
import com.ruben.composeanimation.download.models.DownloadInfo
import com.ruben.composeanimation.download.models.PreDownloadComplete
import com.ruben.composeanimation.download.models.PreDownloadStarted
import com.ruben.composeanimation.download.models.PreDownloadSuccess
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.reduce

/**
 * Created by Ruben Quadros on 02/12/21
 **/
class GiftDownloaderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dbHelper: DbHelper
) : GiftDownloader {

    override suspend fun downloadGift(downloadInfo: DownloadInfo) {
        //update status in db
        updateDownloadStatus(downloadInfo = downloadInfo, giftStatus = GiftStatus.DOWNLOAD_QUEUED)
        if (downloadInfo.audioUrl == null) {
            //download only anim
            Log.d("Ruben", "start download ${downloadInfo.downloadId}, ${downloadInfo.animUrl}")
            DownloadWorker.enqueueAssetDownloadWork(context, downloadInfo.downloadId, downloadInfo.animUrl)
        } else {
            //download both audio and anim
            Log.d("Ruben", "start download for audio and anim ${downloadInfo.downloadId}, ${downloadInfo.audioUrl}, ${downloadInfo.animUrl}")
            DownloadWorker.enqueueAssetDownloadWork(context, downloadInfo.downloadId, downloadInfo.animUrl, downloadInfo.audioUrl)
        }
    }

    override suspend fun preDownloadGifts(downloads: List<DownloadInfo>): Flow<DownloadResult> = flow {
        val downloadList: MutableList<String> = mutableListOf()

        downloads.forEach {
            Log.d("Ruben", "queuing gift ${it.downloadId}")
            //update status in db
            updateDownloadStatus(downloadInfo = it, giftStatus = GiftStatus.DOWNLOAD_QUEUED)

            DownloadWorker.enqueueAssetDownloadWork(context, it.downloadId, it.animUrl)

            emit(PreDownloadStarted(it))

            downloadList.add(DownloadWorker.TAG + it.downloadId)
        }

        val totalDownloads = downloadList.size
        val downloadIds: MutableSet<String> = mutableSetOf()
        var downloadUpdate = 0

        WorkManager.getInstance(context).getWorkInfosLiveData(
            WorkQuery.Builder.fromTags(downloadList)
                .addStates(listOf(WorkInfo.State.SUCCEEDED)).build()
        ).asFlow().filterNot { it.isEmpty() }.distinctUntilChanged().collect { workInfoList ->
            workInfoList.map { workInfo ->
                val giftId = workInfo.outputData.getString("giftId")
                val giftUrl = workInfo.outputData.getString("giftUrl")
                giftId?.let { id ->
                    giftUrl?.let { url ->
                        val isNew = downloadIds.add(id)
                        if (isNew) {
                            downloadUpdate = downloadUpdate.inc()
                            emit(PreDownloadSuccess(DownloadInfo(downloadId = id, animUrl = url)))
                            Log.d("Ruben", "success download $id, $url")
                            if (downloadUpdate >= totalDownloads) {
                                downloadList.clear()
                                emit(PreDownloadComplete)
                            }
                        }
                    }
                }
            }
        }

    }

    override suspend fun getDownloadStatus(): Flow<GiftAnimation> {
        return dbHelper.getDownloadStatus().filterNotNull()
    }

    private suspend fun updateDownloadStatus(downloadInfo: DownloadInfo, giftStatus: GiftStatus) {
        dbHelper.insertGiftAnimation(
            GiftAnimation(
                id = downloadInfo.downloadId,
                giftSource = downloadInfo.animUrl,
                soundSource = downloadInfo.audioUrl,
                createdTime = System.currentTimeMillis(),
                updatedTime = System.currentTimeMillis(),
                giftStatus = giftStatus,
                requestId = downloadInfo.requestId
            )
        )
    }
}