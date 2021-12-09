package com.ruben.composeanimation.data

import kotlinx.coroutines.flow.Flow

/**
 * Created by Ruben Quadros on 08/12/21
 **/
interface DbHelper {
    suspend fun insertGift(giftMessage: GiftMessage)
    fun getNewGift(): Flow<GiftMessage>
    suspend fun clearDb()
    suspend fun getGiftAnimation(id: String): GiftAnimation?
    suspend fun insertGiftAnimation(giftAnimation: GiftAnimation)
    suspend fun updateLastUsedTime(source: String, timestamp: Long)
    suspend fun updateGiftDownloadStatus(
        giftId: String,
        giftStatus: GiftStatus,
        animAssetLocation: String = "",
        audioAssetLocation: String? = null,
        updatedTime: Long
    )
    suspend fun getDownloadStatus(): Flow<GiftAnimation>
    suspend fun syncGiftAnimations()
    suspend fun getGift(commentId: Long): GiftMessage?
}