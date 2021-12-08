package com.ruben.composeanimation.queue.models

import com.ruben.composeanimation.data.GiftMessage

/**
 * Created by Ruben Quadros on 07/12/21
 **/
sealed class DequeueResult

data class GiftDequeued(val giftMessage: GiftMessage): DequeueResult()
