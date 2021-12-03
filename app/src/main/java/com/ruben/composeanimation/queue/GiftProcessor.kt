package com.ruben.composeanimation.queue

import com.ruben.composeanimation.data.GiftMessage

/**
 * Created by Ruben Quadros on 02/12/21
 **/
interface GiftProcessor {
    suspend fun processGift(giftMessage: GiftMessage): GiftMessage
    fun removeProcessedGift(giftMessage: GiftMessage)
}