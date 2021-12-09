package com.ruben.composeanimation.data

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

    override suspend fun getGiftAnimation(id: String): GiftAnimation? {
        return db.animDao().getAnimation(id)
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
        audioAssetLocation: String?,
        updatedTime: Long,
    ) {
        db.animDao().updateGiftDownloadStatus(giftId, giftStatus, animAssetLocation, audioAssetLocation, updatedTime)
    }

    override suspend fun getDownloadStatus(): Flow<GiftAnimation> {
        return db.animDao().getDownloadStatus()
    }

    override suspend fun syncGiftAnimations(): List<GiftAnimation> {
        return db.animDao().getFilePaths()
    }

    override suspend fun getGift(commentId: Long): GiftMessage? {
        return db.giftDao().getGift(commentId)
    }

    override suspend fun deleteOutOfSyncFiles(ids: List<String>) {
        db.animDao().deleteOutOfSyncFiles(ids)
    }

}