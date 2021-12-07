package com.ruben.composeanimation.queue.models

import com.ruben.composeanimation.data.GiftMessage

/**
 * Created by Ruben Quadros on 07/12/21
 **/
sealed class EnqueueResult

data class GiftEnqueued(val giftMessage: GiftMessage): EnqueueResult()

data class GiftNotDownloaded(val giftMessage: GiftMessage): EnqueueResult()
