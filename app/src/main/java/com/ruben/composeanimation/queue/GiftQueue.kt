package com.ruben.composeanimation.queue

import com.ruben.composeanimation.data.GiftMessage
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ruben Quadros on 02/12/21
 **/
interface GiftQueue {
    suspend fun initialize()
    suspend fun enqueue(giftMessage: GiftMessage)
    suspend fun dequeue(giftMessage: GiftMessage)
    suspend fun getGifts(): Flow<List<GiftMessage>>
    fun pauseQueue()
    fun resumeQueue()
    fun shutDown()
}