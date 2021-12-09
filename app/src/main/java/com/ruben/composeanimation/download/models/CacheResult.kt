package com.ruben.composeanimation.download.models

import com.ruben.composeanimation.data.GiftStatus

/**
 * Created by Ruben Quadros on 08/12/21
 **/
sealed class CacheResult

data class CacheStarted(val downloadInfo: DownloadInfo): CacheResult()

data class CacheStatus(val status: GiftStatus, val cachedResource: CachedResource, val requestId: Long): CacheResult()


