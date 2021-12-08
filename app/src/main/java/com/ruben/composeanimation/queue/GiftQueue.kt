package com.ruben.composeanimation.queue

import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.queue.models.DequeueResult
import com.ruben.composeanimation.queue.models.EnqueueResult
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ruben Quadros on 02/12/21
 **/
interface GiftQueue {
    suspend fun initialize()
    suspend fun enqueue(giftMessage: GiftMessage): Flow<EnqueueResult>
    suspend fun dequeue(giftMessage: GiftMessage): Flow<DequeueResult>
    suspend fun getGifts(): Flow<GiftMessage>
    fun pauseQueue()
    suspend fun resumeQueue()
    fun shutDown()
}