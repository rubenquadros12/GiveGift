package com.ruben.composeanimation.download

import com.ruben.composeanimation.domain.GiftMessageEntity
import com.ruben.composeanimation.download.models.DownloadResult
import com.ruben.composeanimation.download.models.GiftInfo
import java.io.File
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ruben Quadros on 08/12/21
 **/
interface GiftDownloader {
    fun initialize()

    fun shutdown()

    suspend fun downloadGift(giftMessage: GiftMessageEntity): Flow<DownloadResult>

    suspend fun preDownloadGift(giftInfo: GiftInfo, file: File): Flow<DownloadResult>
}