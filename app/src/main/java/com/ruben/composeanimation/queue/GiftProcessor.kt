package com.ruben.composeanimation.queue

import com.ruben.composeanimation.domain.GiftMessageEntity

/**
 * Created by Ruben Quadros on 02/12/21
 **/
interface GiftProcessor {
    suspend fun processGift(giftMessage: GiftMessageEntity): GiftMessageEntity
    fun removeProcessedGift(giftMessage: GiftMessageEntity)
}