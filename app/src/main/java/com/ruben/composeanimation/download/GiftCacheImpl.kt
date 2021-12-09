package com.ruben.composeanimation.download

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.ruben.composeanimation.data.DbHelper
import com.ruben.composeanimation.data.GiftAnimation
import com.ruben.composeanimation.data.GiftStatus
import com.ruben.composeanimation.domain.GiftMessageEntity
import com.ruben.composeanimation.download.models.CacheDirectoryEmpty
import com.ruben.composeanimation.download.models.CacheResult
import com.ruben.composeanimation.download.models.CacheScanResult
import com.ruben.composeanimation.download.models.CacheScanSuccess
import com.ruben.composeanimation.download.models.CacheStarted
import com.ruben.composeanimation.download.models.CacheSuccess
import com.ruben.composeanimation.download.models.CachedResource
import com.ruben.composeanimation.download.models.CleanCacheResult
import com.ruben.composeanimation.download.models.DirectoryNotPresent
import com.ruben.composeanimation.download.models.DownloadStarted
import com.ruben.composeanimation.download.models.FileCleanUpResult
import com.ruben.composeanimation.download.models.GiftInfo
import com.ruben.composeanimation.download.models.NoCacheDirectory
import com.ruben.composeanimation.download.models.PreDownloadStarted
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flow

/**
 * Created by Ruben Quadros on 08/12/21
 **/
class GiftCacheImpl @Inject constructor(
    private val dbHelper: DbHelper,
    private val downloader: GiftDownloader,
    @ApplicationContext private val context: Context
): GiftCache {

    private var _job: Job? = null

    init {
        downloader.initialize()
        observeCacheInternal()
    }


    override fun performCacheScan(): Flow<CacheScanResult> = flow {
        val cacheDirectory = getCacheDirectory()
        if (cacheDirectory.exists().not()) {
            //cache directory not present or deleted
            cacheDirectory.mkdirs()
            emit(NoCacheDirectory)
        } else {
            //check if files present in cache
            val files = cacheDirectory.listFiles()
            if (files.isNullOrEmpty()) {
                //there are no files in directory
                emit(CacheDirectoryEmpty)
            } else {
                //files are present no need to fresh download
                //get size and sync with db
                val info = getCacheInfo(cacheDirectory)
                emit(CacheScanSuccess(info.first, info.second))
                //sync with db
                syncWithDb()
            }
        }

    }

    override suspend fun preCacheGifts(gifts: List<GiftInfo>) {
        val cacheDirectory = getCacheDirectory()

        val downloadList: MutableList<String> = mutableListOf()

        gifts.forEach {
            //insert status in db
            dbHelper.insertGiftAnimation(
                GiftAnimation(
                    id = it.giftId,
                    giftSource = it.animUrl,
                    createdTime = System.currentTimeMillis(),
                    giftStatus = GiftStatus.DOWNLOAD_QUEUED
                )
            )

            //queue for download
            val newFile = File(cacheDirectory, "${it.giftId}.webp")
            downloader.preDownloadGift(giftInfo = it, file = newFile).collect { preDownloadResult ->
                Log.d("Ruben", "pre download $preDownloadResult")
                when (preDownloadResult) {
                    is PreDownloadStarted -> {
                        dbHelper.updateGiftDownloadStatus(preDownloadResult.giftInfo.giftId, GiftStatus.DOWNLOAD_QUEUED)
                        downloadList.add(AnimDownloadWorker.TAG + preDownloadResult.giftInfo.giftId)
                    }
                }
            }
        }

        WorkManager.getInstance(context).getWorkInfosLiveData(
            WorkQuery.Builder.fromTags(downloadList)
                .addStates(listOf(WorkInfo.State.SUCCEEDED)).build()
        ).asFlow().filterNot { it.isEmpty() }.distinctUntilChanged().collect { workInfoList ->
            workInfoList.map { workInfo ->
                workInfo.outputData.getString("giftId")?.let { giftId ->
                    val index = downloadList.indexOfFirst { it == AnimDownloadWorker.TAG + giftId }
                    if (index >= 0) downloadList.removeAt(index)
                    Log.d("Ruben", "success $giftId")
                }
            }
        }
    }

    override fun cleanCache(fileNames: List<String>): Flow<CleanCacheResult> = flow {
        val cacheDirectory = getCacheDirectory()
        if (cacheDirectory.exists().not()) {
            emit(DirectoryNotPresent)
        } else {
            fileNames.forEach {
                val file = File(cacheDirectory, it)
                val isDeleted = file.delete()
                emit(FileCleanUpResult(isDeleted, it))
            }
        }
    }

    override suspend fun getCachedGift(giftMessage: GiftMessageEntity): CachedResource? {
        val cachedAnimation: GiftAnimation? = dbHelper.getGiftAnimation(giftMessage)
        return if (cachedAnimation == null) null
        else CachedResource(cachedAnimAsset = cachedAnimation.giftLocation, cachedAudioAsset = cachedAnimation.soundLocation)
    }

    override suspend fun cacheGift(giftMessage: GiftMessageEntity): Flow<CacheResult> = flow {
        val downloadList: MutableList<String> = mutableListOf()
        downloader.downloadGift(giftMessage).collect { downloadResult ->
            when (downloadResult) {
                is DownloadStarted -> {
                    dbHelper.insertGiftAnimation(
                        GiftAnimation(
                            id = giftMessage.giftId,
                            giftSource = giftMessage.animUrl,
                            createdTime = System.currentTimeMillis(),
                            giftStatus = GiftStatus.DOWNLOAD_QUEUED
                        )
                    )
                    emit(CacheStarted(downloadResult.giftMessage))
                }
            }
        }

        emit(CacheSuccess(giftMessage))

//        WorkManager.getInstance(context).getWorkInfosLiveData(
//            WorkQuery.Builder.fromTags(downloadList)
//                .addStates(listOf(WorkInfo.State.SUCCEEDED)).build()
//        ).asFlow().filterNot { it.isEmpty() }.distinctUntilChanged().collect { workInfoList ->
//            workInfoList.map { workInfo ->
//                val animLocation = workInfo.outputData.getString("localPath")
//                val giftId = workInfo.outputData.getString("giftId")
//                giftId?.let { id ->
//                    animLocation?.let { anim ->
//                        emit(CacheSuccess(giftMessage))
//                        dbHelper.updateGiftDownloadStatus(giftId = id, giftStatus = GiftStatus.DOWNLOADED, animAssetLocation = anim)
//                        giftMessage.animSource = anim
//                        val index = downloadList.indexOfFirst { it == AnimDownloadWorker.TAG + id }
//                        if (index >= 0) downloadList.removeAt(index)
//                        Log.d("Ruben", "success $id, $anim")
//                    }
//                }
//            }
//        }
    }

    override fun shutdown() {
        _job?.cancel()
        downloader.shutdown()
    }

    override fun getCacheDirectory(): File {
        val directory = context.getExternalFilesDir("${Environment.DIRECTORY_DOWNLOADS}/livestream")
        return directory ?: context.filesDir
    //return File(context.cacheDir, ".livestream")
    }

    fun getCachePath(): String {
        return getCacheDirectory().absolutePath
    }

    private fun getCacheInfo(cacheDirectory: File): Pair<List<String>, Long> {
        var size: Long = 0
        val fileNames: MutableList<String> = mutableListOf()
        val files: Array<File>? = cacheDirectory.listFiles()
        if (files != null && files.isNotEmpty()) {
            files.forEach {
                fileNames.add(it.name)
                size += it.length()
            }
        }
        return Pair(fileNames, size)
    }

    private fun syncWithDb() {

    }

    private fun observeCacheInternal() {
//        val cacheDirectory = getCacheDirectory()
//
//        object : FileObserver(cacheDirectory) {
//            override fun onEvent(event: Int, file: String?) {
//                _job = CoroutineScope(Dispatchers.IO).launch {
//                    file?.let { dbHelper.updateLastUsedTime(it, System.currentTimeMillis()) }
//                }
//            }
//        }.startWatching()
    }
}