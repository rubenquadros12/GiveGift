package com.ruben.composeanimation.download.models

/**
 * Created by Ruben Quadros on 08/12/21
 **/
data class DownloadInfo(
    val requestId: Long = -1,
    val downloadId: String,
    val animUrl: String,
    val audioUrl: String? = null
)
