package com.ruben.composeanimation.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.ruben.composeanimation.domain.GiftMessageEntity
import com.ruben.composeanimation.download.models.DownloadResult
import com.ruben.composeanimation.download.models.DownloadStarted
import com.ruben.composeanimation.download.models.GiftInfo
import com.ruben.composeanimation.download.models.PreDownloadStarted
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Created by Ruben Quadros on 02/12/21
 **/
class GiftDownloaderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : GiftDownloader {

    private val _downloadManager: DownloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    private var _broadcastReceiver: BroadcastReceiver? = null

    override fun initialize() {
        _broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("Ruben", "${intent?.extras}")
            }
        }
        context.registerReceiver(_broadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun shutdown() {
        context.unregisterReceiver(_broadcastReceiver)
    }

    override suspend fun downloadGift(giftMessage: GiftMessageEntity): Flow<DownloadResult> = flow {
        if (giftMessage.audioSource == null) {
            //download only anim
            Log.d("Ruben", "start download ${giftMessage.giftId}, ${giftMessage.animSource}")
            AnimDownloadWorker.enqueueAssetDownloadWork(context, giftMessage.giftId, giftMessage.animUrl)
        } else {
            //download both audio and anim
        }
        emit(DownloadStarted(giftMessage))
    }

    override suspend fun preDownloadGift(giftInfo: GiftInfo, file: File): Flow<DownloadResult> = flow {
//        val request = DownloadManager.Request(Uri.parse(giftInfo.animUrl))
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
//            .setDestinationUri(Uri.fromFile(file))
//            .setAllowedOverMetered(true)
//            .setAllowedOverRoaming(true)
//
//        val downloadId = _downloadManager.enqueue(request)
        Log.d("Ruben", "queuing gift ${giftInfo.giftId}")
        AnimDownloadWorker.enqueueAssetDownloadWork(context, giftInfo.giftId, giftInfo.animUrl)

        emit(PreDownloadStarted(giftInfo))
    }
}