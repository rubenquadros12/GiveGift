package com.ruben.composeanimation.data

import com.ruben.composeanimation.domain.GiftMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ruben Quadros on 08/12/21
 **/
interface DbHelper {
    suspend fun insertGift(giftMessage: GiftMessage)
    fun getNewGift(): Flow<GiftMessage>
    suspend fun clearDb()
    suspend fun getGiftAnimation(giftMessage: GiftMessageEntity): GiftAnimation?
    suspend fun insertGiftAnimation(giftAnimation: GiftAnimation)
    suspend fun updateLastUsedTime(source: String, timestamp: Long)
    suspend fun updateGiftDownloadStatus(giftId: String, giftStatus: GiftStatus, animAssetLocation: String = "", audioAssetLocation: String? = null)
    suspend fun syncGiftAnimations()
}