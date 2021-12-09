package com.ruben.composeanimation.data

import com.ruben.composeanimation.domain.GiftMessageEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Created by Ruben Quadros on 08/12/21
 **/
class DbHelperImpl @Inject constructor(private val db: AppDatabase): DbHelper {
    override suspend fun insertGift(giftMessage: GiftMessage) {
        db.giftDao().insertGift(giftMessage)
    }

    override fun getNewGift(): Flow<GiftMessage> {
        return db.giftDao().getNewGift().filterNotNull()
    }

    override suspend fun clearDb() {
        db.giftDao().clearGifts()
    }

    override suspend fun getGiftAnimation(giftMessage: GiftMessageEntity): GiftAnimation? {
        return db.animDao().getAnimation(giftMessage.giftId)
    }

    override suspend fun insertGiftAnimation(giftAnimation: GiftAnimation) {
        db.animDao().insertGiftAnimation(giftAnimation)
    }

    override suspend fun updateLastUsedTime(source: String, timestamp: Long) {
        db.animDao().updateLastUsedTime(timestamp = timestamp, source = source)
    }

    override suspend fun updateGiftDownloadStatus(
        giftId: String,
        giftStatus: GiftStatus,
        animAssetLocation: String,
        audioAssetLocation: String?
    ) {
        db.animDao().updateGiftDownloadStatus(giftId, giftStatus, animAssetLocation, audioAssetLocation)
    }

    override suspend fun syncGiftAnimations() {

    }

}