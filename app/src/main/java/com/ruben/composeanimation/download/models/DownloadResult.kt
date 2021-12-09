package com.ruben.composeanimation.download.models

import com.ruben.composeanimation.domain.GiftMessageEntity

/**
 * Created by Ruben Quadros on 08/12/21
 **/
sealed class DownloadResult

data class DownloadStarted(val giftMessage: GiftMessageEntity): DownloadResult()

data class DownloadSuccess(val giftMessage: GiftMessageEntity): DownloadResult()

data class PreDownloadStarted(val downloadInfo: DownloadInfo): DownloadResult()

data class PreDownloadSuccess(val downloadInfo: DownloadInfo): DownloadResult()
