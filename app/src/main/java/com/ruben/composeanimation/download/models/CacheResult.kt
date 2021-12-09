package com.ruben.composeanimation.download.models

import com.ruben.composeanimation.domain.GiftMessageEntity

/**
 * Created by Ruben Quadros on 08/12/21
 **/
sealed class CacheResult

data class CacheStarted(val giftMessage: GiftMessageEntity): CacheResult()

data class CacheSuccess(val giftMessage: GiftMessageEntity): CacheResult()


