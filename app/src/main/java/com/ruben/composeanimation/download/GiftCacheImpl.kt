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
import com.ruben.composeanimation.download.models.CacheStatus
import com.ruben.composeanimation.download.models.CachedResource
import com.ruben.composeanimation.download.models.CleanCacheResult
import com.ruben.composeanimation.download.models.DirectoryNotPresent
import com.ruben.composeanimation.download.models.DownloadInfo
import com.ruben.composeanimation.download.models.FileCleanUpResult
import com.ruben.composeanimation.download.models.NoCacheDirectory
import com.ruben.composeanimation.download.models.PreDownloadComplete
import com.ruben.composeanimation.download.models.PreDownloadStarted
import com.ruben.composeanimation.download.models.PreDownloadSuccess
import com.ruben.composeanimation.utility.isHighTierSlab
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine

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
    private var _isSyncInProgress = false
    private var _syncCallback: SyncCallback? = null

    override suspend fun initialize() {
        observeCacheInternal()
        cacheGiftInternal()
    }


    override fun performCacheScan(): Flow<CacheScanResult> = flow {
        val cacheDirectory = getCacheDirectory()
        if (cacheDirectory.exists().not()) {
            //cache directory not present or deleted
            cacheDirectory.mkdirs()
            _isSyncInProgress = true
            emit(NoCacheDirectory)
        } else {
            //check if files present in cache
            val files = cacheDirectory.listFiles()
            if (files.isNullOrEmpty()) {
                //there are no files in directory
                _isSyncInProgress = true
                emit(CacheDirectoryEmpty)
            } else {
                //files are present no need to fresh download
                //get size and sync with db
                val info = getCacheInfo(cacheDirectory)
                //sync with db
                syncWithDb(info.first)
                Log.d("Ruben", "sync complete, db")
                _isSyncInProgress = false
                _syncCallback?.onSyncComplete()
                emit(CacheScanSuccess(info.first, info.second))
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
                is PreDownloadComplete -> {
                    Log.d("Ruben", "sync complete, predownload")
                    _isSyncInProgress = false
                    _syncCallback?.onSyncComplete()
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
        //if sync in progress wait to respond
        Log.d("Ruben", "sync status $_isSyncInProgress")
        if (_isSyncInProgress) {
            suspendCancellableCoroutine<Unit> { continuation ->
                _syncCallback = object : SyncCallback {
                    override fun onSyncComplete() {
                        if (continuation.isActive) {
                            Log.d("Ruben", "sync complete")
                            continuation.resume(Unit)
                        }
                    }
                }
            }
        }
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
        downloader.getDownloadStatus().collect {
            emit(CacheStatus(
                status = it.giftStatus,
                cachedResource = CachedResource(cachedAnimAsset = it.giftLocation, cachedAudioAsset = it.soundLocation),
                requestId = it.requestId
            ))
        }
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
        val filePaths: MutableList<String> = mutableListOf()
        val files: Array<File>? = cacheDirectory.listFiles()
        if (files != null && files.isNotEmpty()) {
            files.forEach {
                filePaths.add(it.absolutePath)
                size += it.length()
            }
        }
        return Pair(filePaths, size)
    }

    private suspend fun syncWithDb(filePaths: List<String>) {
        //sync db and files
        val outOfSyncFiles: MutableList<String> = mutableListOf()
        val dbFilePaths = dbHelper.syncGiftAnimations()
        dbFilePaths.forEach { dbGift ->
            if (dbGift.slab.isHighTierSlab()) {
                //for higher tabs
                //check both anim and sound path
                if (filePaths.contains(dbGift.giftLocation).not() || filePaths.contains(dbGift.soundLocation).not()) {
                    outOfSyncFiles.add(dbGift.id)
                }
            } else {
                //if slab 1 or slab 2
                //only check for anim path
                if (filePaths.contains(dbGift.giftLocation).not()) {
                    outOfSyncFiles.add(dbGift.id)
                }
            }
        }

        if (outOfSyncFiles.isNotEmpty()) {
            //delete items from db as they are not preset in files
            dbHelper.deleteOutOfSyncFiles(outOfSyncFiles)
        }
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

interface SyncCallback {
    fun onSyncComplete()
}