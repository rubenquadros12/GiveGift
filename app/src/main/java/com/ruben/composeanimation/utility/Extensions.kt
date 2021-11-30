package com.ruben.composeanimation.utility

import com.ruben.composeanimation.ui.Slab

/**
 * Created by Ruben Quadros on 29/11/21
 **/
fun String?.isHighTierSlab(): Boolean {
    return this.isNullOrEmpty().not() && this == Slab.SLAB_3.toString() || this == Slab.SLAB_4.toString() || this == Slab.SLAB_5.toString()
}