package com.ruben.composeanimation.download

import android.content.Context
import android.os.Environment
import android.util.Log
import com.ruben.composeanimation.data.DbHelper
import com.ruben.composeanimation.data.GiftAnimation
import com.ruben.composeanimation.download.models.CacheDirectoryEmpty
import com.ruben.composeanimation.download.models.CacheResult
import com.ruben.composeanimation.download.models.CacheScanResult
import com.ruben.composeanimation.download.models.CacheScanSuccess
import com.ruben.composeanimation.download.models.CacheStarted
import com.ruben.composeanimation.download.models.CachedResource
import com.ruben.composeanimation.download.models.CleanCacheResult
import com.ruben.composeanimation.download.models.DirectoryNotPresent
import com.ruben.composeanimation.download.models.DownloadInfo
import com.ruben.composeanimation.download.models.FileCleanUpResult
import com.ruben.composeanimation.download.models.NoCacheDirectory
import com.ruben.composeanimation.download.models.PreDownloadStarted
import com.ruben.composeanimation.download.models.PreDownloadSuccess
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
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
    private val _cacheChannel: Channel<DownloadInfo> = Channel(capacity = Channel.UNLIMITED)

    override suspend fun initialize() {
        observeCacheInternal()
        cacheGiftInternal()
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

    override suspend fun preCacheGifts(downloads: List<DownloadInfo>) {
        downloader.preDownloadGifts(downloads = downloads).collect { preDownloadResult ->
            Log.d("Ruben", "pre download $preDownloadResult")
            when (preDownloadResult) {
                is PreDownloadStarted -> {
                    Log.d("Ruben", "pre started ${preDownloadResult.downloadInfo}")
                }

                is PreDownloadSuccess -> {
                    Log.d("Ruben", "pre Success ${preDownloadResult.downloadInfo}")
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

    override suspend fun getCachedGift(id: String): CachedResource? {
        val cachedAnimation: GiftAnimation? = dbHelper.getGiftAnimation(id = id)
        return if (cachedAnimation == null) null
        else CachedResource(cachedAnimAsset = cachedAnimation.giftLocation, cachedAudioAsset = cachedAnimation.soundLocation)
    }

    override suspend fun cacheGift(downloadInfo: DownloadInfo): Flow<CacheResult> = flow {
        _cacheChannel.send(downloadInfo)
        emit(CacheStarted(downloadInfo))
    }

    override fun shutdown() {
        _job?.cancel()
    }

    override fun getCacheDirectory(): File {
        val directory = context.getExternalFilesDir("${Environment.DIRECTORY_DOWNLOADS}/livestream")
        return directory ?: context.filesDir
    }

    override fun getCacheResult(): Flow<CacheResult> = flow {
        emit(CacheStarted(DownloadInfo(downloadId = "", animUrl = "")))
    }

    private suspend fun cacheGiftInternal() {
        _cacheChannel.consumeEach {
            downloader.downloadGift(it)
        }
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