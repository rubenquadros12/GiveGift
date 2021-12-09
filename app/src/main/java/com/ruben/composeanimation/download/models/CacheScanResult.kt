package com.ruben.composeanimation.download.models

/**
 * Created by Ruben Quadros on 08/12/21
 **/
sealed class CacheScanResult

object NoCacheDirectory: CacheScanResult()

object CacheDirectoryEmpty: CacheScanResult()

data class CacheScanSuccess(val fileNames: List<String>, val cacheSize: Long): CacheScanResult()