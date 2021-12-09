package com.ruben.composeanimation.download.models

/**
 * Created by Ruben Quadros on 08/12/21
 **/
sealed class CacheResult

data class CacheStarted(val downloadInfo: DownloadInfo): CacheResult()

data class CacheSuccess(val downloadInfo: DownloadInfo, val cachedResource: CachedResource): CacheResult()


