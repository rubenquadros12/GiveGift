package com.ruben.composeanimation.queue

import android.util.Log
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.utility.GiftConstants
import com.ruben.composeanimation.utility.isHighTierSlab
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Created by Ruben Quadros on 02/12/21
 **/
class GiftProcessorImpl @Inject constructor() : GiftProcessor {

    private val _giftList: MutableList<GiftMessage> = mutableListOf()
    private var _giftIndex = -1
    private var _callback: GiftListCallback? = null

    override suspend fun processGift(giftMessage: GiftMessage): GiftMessage {
        Log.d("Ruben", "incoming from queue ${giftMessage.slab}")
        return if (isProcessEmpty()) {
            //no gifts present you can add
            Log.d("Ruben", "nothing present so add $_giftList")
            addGift(giftMessage = giftMessage)
        } else {
            //process gift
            processIncomingGiftInternal(giftMessage = giftMessage)
        }
    }

    override fun removeProcessedGift(giftMessage: GiftMessage) {
        _giftIndex = _giftList.indexOfFirst { it.id == giftMessage.id }
        Log.d("Ruben", "remove index $_giftIndex")
        _giftList.removeAt(_giftIndex)
        Log.d("Ruben", "remove size ${_giftList.size}")
        _callback?.onGiftListChanged(_giftList.size)
    }

    private fun isProcessEmpty(): Boolean = _giftList.isEmpty()

    private fun isProcessFull(): Boolean = _giftList.size == GiftConstants.MAX_VISIBLE_GIFTS

    private fun isHighTierGiftDisplaying(): Boolean = _giftList[0].slab.isHighTierSlab()

    private suspend fun processIncomingGiftInternal(giftMessage: GiftMessage): GiftMessage {
        if (giftMessage.slab.isHighTierSlab()) {
            Log.d("Ruben", "incoming gift high tier ${giftMessage.slab}")
            //wait for all gifts to be shown
            return addHighTierGift(giftMessage)
        } else {
            when {
                isProcessFull() -> {
                    //wait for a gift to finish displaying
                    Log.d("Ruben", "size is 2 ${giftMessage.slab}")
                    return addLowTierGift(giftMessage)
                }

                isHighTierGiftDisplaying() -> {
                    //wait for all gifts to finish displaying
                    Log.d("Ruben", "already high tier playing ${giftMessage.slab}")
                    return addHighTierGift(giftMessage)
                }
                else -> {
                    //s1 or s2 gift present
                    //can add one more s1 or s2 gift
                    Log.d("Ruben", "s1 or s2 present ${giftMessage.slab}")
                    return if (_giftIndex != -1) {
                        addGift(index = _giftIndex, giftMessage = giftMessage)
                    } else {
                        Log.d("Ruben", "added now after low $_giftList")
                        addGift(index = 1, giftMessage = giftMessage)
                    }
                }
            }
        }
    }

    private fun addGift(index: Int = 0, giftMessage: GiftMessage): GiftMessage {
        _giftList.add(index, giftMessage)
        return _giftList[index]
    }

    private suspend fun addHighTierGift(giftMessage: GiftMessage): GiftMessage {
        suspendCancellableCoroutine<Unit> { continuation ->
            Log.d("Ruben", "in suspendCancellableCoroutine")
            _callback = object : GiftListCallback {
                override fun onGiftListChanged(size: Int) {
                    if (size == 0) {
                        if (continuation.isActive) {
                            Log.d("Ruben", "resume suspendCancellableCoroutine")
                            continuation.resume(Unit)
                        }
                    }
                }
            }
        }
        return addGift(giftMessage = giftMessage)
    }

    private suspend fun addLowTierGift(giftMessage: GiftMessage): GiftMessage {
        suspendCancellableCoroutine<Unit> { continuation ->
            _callback = object : GiftListCallback {
                override fun onGiftListChanged(size: Int) {
                    if (size == 1) {
                        if (continuation.isActive) {
                            Log.d("Ruben", "resume suspendCancellableCoroutine")
                            continuation.resume(Unit)
                        }
                    }
                }
            }
        }
        return addGift(index = _giftIndex, giftMessage = giftMessage)
    }
}

interface GiftListCallback {
    fun onGiftListChanged(size: Int)
}