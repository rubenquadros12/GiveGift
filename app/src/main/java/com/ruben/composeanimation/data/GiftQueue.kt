package com.ruben.composeanimation.data

import android.util.Log
import com.ruben.composeanimation.utility.GiftConstants.MAX_VISIBLE_GIFTS
import com.ruben.composeanimation.utility.isHighTierSlab
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Created by Ruben Quadros on 30/11/21
 **/
class GiftQueue @Inject constructor() {

    private val _giftList: MutableList<GiftMessage> = mutableListOf()
    private var _giftIndex = -1
    private val _giftChannelOthers: Channel<GiftMessage> = Channel(capacity = Channel.UNLIMITED)
    private val _giftChannelSelf: Channel<GiftMessage> = Channel(capacity = Channel.UNLIMITED)
    private val _giftChannel: Channel<List<GiftMessage>> = Channel(capacity = Channel.UNLIMITED)
    private var _callback: ListCallback? = null
    private val _isDownloaded = true

    suspend fun initialize() {
        CoroutineScope(Dispatchers.Default).launch { queueGiftsInternal() }
    }

    suspend fun queueIncomingGift(giftMessage: GiftMessage) {
        Log.d("Ruben", "incoming from vm ${giftMessage.slab}")
        if (_isDownloaded) {
            if (giftMessage.userId == "123") {
                Log.d("Ruben", "send self")
                _giftChannelSelf.send(giftMessage)
            } else {
                Log.d("Ruben", "send others")
                _giftChannelOthers.send(giftMessage)
            }
        } else {
            if (giftMessage.userId == "123") {
                downloadAsset().collect {
                    if (it) _giftChannelSelf.send(giftMessage)
                }
            } else {
                downloadAsset().collect {
                    if (it) _giftChannelOthers.send(giftMessage)
                }
            }
        }
    }

    fun clearGift(giftMessage: GiftMessage) {
        Log.d("Ruben", "clear gift ${giftMessage.slab}")
        _giftIndex = _giftList.indexOfFirst { it.id == giftMessage.id }
        _giftList.removeAt(_giftIndex)
        _callback?.onListChanged(_giftList.size)
        Log.d("Ruben", "after clearing gift $_giftList")
    }

    suspend fun getGifts(): Flow<List<GiftMessage>> = flow {
        _giftChannel.consumeEach {
            Log.d("Ruben", "emit")
            emit(it)
        }
    }

    private suspend fun downloadAsset() = flow {
        emit(true)
    }

    private suspend fun queueGiftsInternal() {
//        _giftChannelOthers.consumeEach {
//            Log.d("Ruben", "select others")
//            _giftChannel.send(processGift(it))
//        }
        while (true) {
            select<Unit> {
                _giftChannelSelf.onReceiveCatching { result ->
                    result.getOrNull()?.let { gift ->
                            _giftChannel.send(processGift(gift))
                        }
                }
                _giftChannelOthers.onReceiveCatching { result ->
                    result.getOrNull()?.let { gift ->
                        _giftChannel.send(processGift(gift))
                    }
                }
            }
        }
    }

    private suspend fun processGift(giftMessage: GiftMessage): List<GiftMessage> {
        Log.d("Ruben", "incoming from queue ${giftMessage.slab}")
        if (_giftList.isEmpty()) {
            //no gifts you can add
            _giftList.add(giftMessage)
            Log.d("Ruben", "nothing present so add $_giftList")
            return _giftList.toList()
        } else {
            if (giftMessage.slab.isHighTierSlab()) {
                Log.d("Ruben", "incoming gift high tier ${giftMessage.slab}")
                //wait for all gifts to be shown
                suspendCancellableCoroutine<Unit> { continuation ->
                    Log.d("Ruben", "in suspendCancellableCoroutine")
                    _callback = object : ListCallback {
                        override fun onListChanged(size: Int) {
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
                return _giftList.toList()
            } else {
                //Log.d("Ruben", "incoming gift else ${giftMessage.slab} ${giftList.size}")
                when {
                    _giftList.size == MAX_VISIBLE_GIFTS -> {
                        Log.d("Ruben", "size is 2 ${giftMessage.slab}")
                        //wait for a gift to finish displaying
                        suspendCancellableCoroutine<Unit> { continuation ->
                            _callback = object : ListCallback {
                                override fun onListChanged(size: Int) {
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
                        return _giftList.toList()
                    }
                    _giftList[0].slab.isHighTierSlab() -> {
                        Log.d("Ruben", "already high tier playing ${giftMessage.slab}")
                        //wait for the gift to finish displaying
                        suspendCancellableCoroutine<Unit> { continuation ->
                            _callback = object : ListCallback {
                                override fun onListChanged(size: Int) {
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
                        //_giftListFlow.value = _giftList
                        Log.d("Ruben", "added now after high $_giftList")
                        return _giftList.toList()
                    }
                    else -> {
                        //s1 or s2 gift present
                        //can add one more s1 or s2 gift
                        Log.d("Ruben", "s1 or s2 present ${giftMessage.slab}")
                        if (_giftIndex != -1) {
                            _giftList.add(_giftIndex, giftMessage)
                        } else {
                            _giftList.add(giftMessage)
                        }
                        Log.d("Ruben", "added now after low $_giftList")
                        return _giftList.toList()
                    }
                }
            }
        }
    }
}

interface ListCallback {
    fun onListChanged(size: Int)
}