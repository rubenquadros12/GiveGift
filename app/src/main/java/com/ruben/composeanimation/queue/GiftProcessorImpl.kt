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
        if (_giftList.isEmpty()) {
            //no gifts you can add
            _giftList.add(giftMessage)
            Log.d("Ruben", "nothing present so add $_giftList")
            return _giftList[0]
        } else {
            if (giftMessage.slab.isHighTierSlab()) {
                Log.d("Ruben", "incoming gift high tier ${giftMessage.slab}")
                //wait for all gifts to be shown
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
                _giftList.add(giftMessage)
                Log.d("Ruben", "added high tier now $_giftList")
                return _giftList[0]
            } else {
                //Log.d("Ruben", "incoming gift else ${giftMessage.slab} ${giftList.size}")
                when {
                    _giftList.size == GiftConstants.MAX_VISIBLE_GIFTS -> {
                        Log.d("Ruben", "size is 2 ${giftMessage.slab}")
                        //wait for a gift to finish displaying
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
                        _giftList.add(_giftIndex, giftMessage)
                        Log.d("Ruben", "added now low $_giftList")
                        return _giftList[_giftIndex]
                    }
                    _giftList[0].slab.isHighTierSlab() -> {
                        Log.d("Ruben", "already high tier playing ${giftMessage.slab}")
                        //wait for the gift to finish displaying
                        suspendCancellableCoroutine<Unit> { continuation ->
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
                        _giftList.add(giftMessage)
                        Log.d("Ruben", "added now after high $_giftList")
                        return _giftList[0]
                    }
                    else -> {
                        //s1 or s2 gift present
                        //can add one more s1 or s2 gift
                        Log.d("Ruben", "s1 or s2 present ${giftMessage.slab}")
                        if (_giftIndex != -1) {
                            _giftList.add(_giftIndex, giftMessage)
                            Log.d("Ruben", "added now after low $_giftList")
                            return _giftList[_giftIndex]
                        } else {
                            _giftList.add(giftMessage)
                            Log.d("Ruben", "added now after low $_giftList")
                            return _giftList[1]
                        }
                    }
                }
            }
        }
    }

    override fun removeProcessedGift(giftMessage: GiftMessage) {
        _giftIndex = _giftList.indexOfFirst { it.id == giftMessage.id }
        Log.d("Ruben", "remove index $_giftIndex")
        _giftList.removeAt(_giftIndex)
        Log.d("Ruben", "remove size ${_giftList.size}")
        _callback?.onGiftListChanged(_giftList.size)
    }
}

interface GiftListCallback {
    fun onGiftListChanged(size: Int)
}