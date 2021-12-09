package com.ruben.composeanimation.queue.models

import com.ruben.composeanimation.domain.GiftMessageEntity

/**
 * Created by Ruben Quadros on 07/12/21
 **/
sealed class EnqueueResult

data class GiftEnqueued(val giftMessage: GiftMessageEntity): EnqueueResult()

data class GiftNotDownloaded(val giftMessage: GiftMessageEntity): EnqueueResult()
