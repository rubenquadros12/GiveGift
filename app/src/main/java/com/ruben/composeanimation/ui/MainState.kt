package com.ruben.composeanimation.ui

import com.ruben.composeanimation.domain.GiftMessageEntity

/**
 * Created by Ruben Quadros on 27/11/21
 **/
data class MainState(
    val slot1: GiftMessageEntity? = null,
    val slot2: GiftMessageEntity? = null,
    val specialSlot: GiftMessageEntity? = null,
    val slabList: List<Slab> = listOf(Slab.SLAB_1, Slab.SLAB_2, Slab.SLAB_3, Slab.SLAB_4, Slab.SLAB_5)
)