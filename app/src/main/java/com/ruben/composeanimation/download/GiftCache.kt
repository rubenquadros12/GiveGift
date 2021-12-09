package com.ruben.composeanimation.download

import com.ruben.composeanimation.domain.GiftMessageEntity
import com.ruben.composeanimation.download.models.CacheResult
import com.ruben.composeanimation.download.models.CacheScanResult
import com.ruben.composeanimation.download.models.CachedResource
import com.ruben.composeanimation.download.models.CleanCacheResult
import com.ruben.composeanimation.download.models.GiftInfo
import java.io.File
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ruben Quadros on 08/12/21
 **/
interface GiftCache {

    fun performCacheScan(): Flow<CacheScanResult>

    suspend fun preCacheGifts(gifts: List<GiftInfo>)

    fun cleanCache(fileNames: List<String>): Flow<CleanCacheResult>

    suspend fun getCachedGift(giftMessage: GiftMessageEntity): CachedResource?

    suspend fun cacheGift(giftMessage: GiftMessageEntity): Flow<CacheResult>

    fun getCacheDirectory(): File

    fun shutdown()
}