package com.ruben.composeanimation.download.models

/**
 * Created by Ruben Quadros on 08/12/21
 **/
sealed class CleanCacheResult

object DirectoryNotPresent: CleanCacheResult()

data class FileCleanUpResult(val isDeleted: Boolean, val fileName: String): CleanCacheResult()
