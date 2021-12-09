package com.ruben.composeanimation.queue

import android.util.Log
import com.ruben.composeanimation.data.DbHelper
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.domain.GiftMessageEntity
import com.ruben.composeanimation.download.models.CachedResource
import javax.inject.Inject

/**
 * Created by Ruben Quadros on 09/12/21
 **/
class QueueUtil @Inject constructor(
    private val dbHelper: DbHelper
) {

    suspend fun getGiftMessage(commentId: Long): GiftMessageEntity? {
        return dbHelper.getGift(commentId)?.toUIEntity()
    }

}

fun GiftMessage?.toUIEntity(): GiftMessageEntity? {
    return if (this == null) null
    else GiftMessageEntity(
        commentId = this.commentId,
        giftId = this.giftId,
        animDuration = this.animDuration,
        totalDuration = this.totalDuration,
        userId = this.userId,
        slab = this.slab,
        message = this.message,
        animUrl = this.animUrl,
        audioUrl = this.audioUrl
    )
}

fun GiftMessageEntity.attachCachedResource(cachedResource: CachedResource): GiftMessageEntity {
    this.animSource = cachedResource.cachedAnimAsset
    this.audioSource = cachedResource.cachedAudioAsset
    Log.d("Ruben", "attaching cached values ${this.animSource}, ${this.audioSource}")
    return this
}