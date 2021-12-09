package com.ruben.composeanimation.download

import com.ruben.composeanimation.data.GiftAnimation
import com.ruben.composeanimation.download.models.DownloadResult
import com.ruben.composeanimation.download.models.DownloadInfo
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ruben Quadros on 08/12/21
 **/
interface GiftDownloader {

    suspend fun downloadGift(downloadInfo: DownloadInfo)

    suspend fun preDownloadGifts(downloads: List<DownloadInfo>): Flow<DownloadResult>

    suspend fun getDownloadStatus(): Flow<GiftAnimation>
}