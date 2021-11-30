package com.ruben.composeanimation.data

import android.util.Log
import com.ruben.composeanimation.utility.GiftConstants.MAX_VISIBLE_GIFTS
import com.ruben.composeanimation.utility.isHighTierSlab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Created by Ruben Quadros on 30/11/21
 **/
class GiftQueue @Inject constructor() {

    private val _giftList: MutableList<GiftMessage> = mutableListOf()
    //private val _giftListFlow: MutableStateFlow<MutableList<GiftMessage>> = MutableStateFlow(_giftList)
    private var _giftIndex = -1
    private val _giftChannel: Channel<GiftMessage> = Channel(capacity = Channel.UNLIMITED)
    private var callback: ListCallback? = null

    suspend fun queueIncomingGift(giftMessage: GiftMessage) {
        Log.d("Ruben", "incoming from vm ${giftMessage.slab}")
        _giftChannel.send(giftMessage)
    }

    fun clearGift(giftMessage: GiftMessage) {
        Log.d("Ruben", "clear gift ${giftMessage.slab}")
        _giftIndex = _giftList.indexOfFirst { it.id == giftMessage.id }
        _giftList.removeAt(_giftIndex)
        //_giftListFlow.value = _giftList
        callback?.onListChanged(_giftList.size)
        Log.d("Ruben", "after clearing gift $_giftList")
    }

    suspend fun getGifts(): Flow<List<GiftMessage>> = flow {
        _giftChannel.consumeEach {
            Log.d("Ruben", "incoming from queue ${it.slab}")
            if (_giftList.isEmpty()) {
                //no gifts you can add
                _giftList.add(it)
                //_giftListFlow.value = _giftList
                Log.d("Ruben", "nothing present so add $_giftList")
                emit(_giftList.toList())
            } else {
                if (it.slab.isHighTierSlab()) {
                    Log.d("Ruben", "incoming gift high tier ${it.slab}")
                    //wait for all gifts to be shown
                    suspendCancellableCoroutine<Unit> { continuation ->
                        Log.d("Ruben", "in suspendCancellableCoroutine")
                        callback = object : ListCallback {
                            override fun onListChanged(size: Int) {
                                if (size == 0) {
                                    if (continuation.isActive) {
                                        Log.d("Ruben", "resume suspendCancellableCoroutine")
                                        continuation.resume(Unit)
                                    }
                                }
                            }
                        }
//                        if (_giftListFlow.value.size == 0) {
//                            if (continuation.isActive) {
//                                Log.d("Ruben", "resume suspendCancellableCoroutine")
//                                continuation.resume(Unit)
//                            }
//                        }
                    }
                    _giftList.add(it)
                    //_giftListFlow.value = _giftList
                    Log.d("Ruben", "added high tier now $_giftList")
                    emit(_giftList.toList())
                } else {
                    //Log.d("Ruben", "incoming gift else ${giftMessage.slab} ${giftList.size}")
                    when {
                        _giftList.size == MAX_VISIBLE_GIFTS -> {
                            Log.d("Ruben", "size is 2 ${it.slab}")
                            //wait for a gift to finish displaying
                            suspendCancellableCoroutine<Unit> { continuation ->
                                callback = object : ListCallback {
                                    override fun onListChanged(size: Int) {
                                        if (size == 1) {
                                            if (continuation.isActive) {
                                                Log.d("Ruben", "resume suspendCancellableCoroutine")
                                                continuation.resume(Unit)
                                            }
                                        }
                                    }
                                }
//                                if (_giftListFlow.value.size == 1) {
//                                    if (continuation.isActive) continuation.resume(Unit)
//                                }
                            }
                            _giftList.add(_giftIndex, it)
                            //_giftListFlow.value = _giftList
                            Log.d("Ruben", "added now low $_giftList")
                            emit(_giftList.toList())
                        }
                        _giftList[0].slab.isHighTierSlab() -> {
                            Log.d("Ruben", "already high tier playing ${it.slab}")
                            //wait for the gift to finish displaying
                            suspendCancellableCoroutine<Unit> { continuation ->
                                callback = object : ListCallback {
                                    override fun onListChanged(size: Int) {
                                        if (size == 0) {
                                            if (continuation.isActive) {
                                                Log.d("Ruben", "resume suspendCancellableCoroutine")
                                                continuation.resume(Unit)
                                            }
                                        }
                                    }
                                }
//                                if (_giftListFlow.value.size == 0) {
//                                    if (continuation.isActive) continuation.resume(Unit)
//                                }
                            }
                            _giftList.add(it)
                            //_giftListFlow.value = _giftList
                            Log.d("Ruben", "added now after high $_giftList")
                            emit(_giftList.toList())
                        }
                        else -> {
                            //s1 or s2 gift present
                            //can add one more s1 or s2 gift
                            Log.d("Ruben", "s1 or s2 present ${it.slab}")
                            if (_giftIndex != -1) {
                                _giftList.add(_giftIndex, it)
                            } else {
                                _giftList.add(it)
                            }
                            //_giftListFlow.value = _giftList
                            Log.d("Ruben", "added now after low $_giftList")
                            emit(_giftList.toList())
                        }
                    }
                }
            }
        }
    }
}

interface ListCallback {
    fun onListChanged(size: Int)
}