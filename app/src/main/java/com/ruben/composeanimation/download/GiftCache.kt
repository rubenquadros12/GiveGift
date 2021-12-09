package com.ruben.composeanimation.download

import com.ruben.composeanimation.download.models.CacheResult
import com.ruben.composeanimation.download.models.CacheScanResult
import com.ruben.composeanimation.download.models.CachedResource
import com.ruben.composeanimation.download.models.CleanCacheResult
import com.ruben.composeanimation.download.models.DownloadInfo
import java.io.File
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ruben Quadros on 08/12/21
 **/
interface GiftCache {

    suspend fun initialize()

    fun performCacheScan(): Flow<CacheScanResult>

    suspend fun preCacheGifts(downloads: List<DownloadInfo>)

    fun cleanCache(fileNames: List<String>): Flow<CleanCacheResult>

    suspend fun getCachedGift(id: String): CachedResource?

    suspend fun cacheGift(downloadInfo: DownloadInfo): Flow<CacheResult>

    fun getCacheDirectory(): File

    fun getCacheResult(): Flow<CacheResult>

    fun shutdown()
}