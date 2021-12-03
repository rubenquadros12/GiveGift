package com.ruben.composeanimation.queue

import android.util.Log
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.download.GiftDownloader
import com.ruben.composeanimation.ui.Slab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import javax.inject.Inject

/**
 * Created by Ruben Quadros on 30/11/21
 **/
class GiftQueueImpl @Inject constructor(
    private val giftProcessor: GiftProcessor,
    private val giftDownloader: GiftDownloader
) : GiftQueue {

    private val _priorityChannels: MutableMap<String, Channel<GiftMessage>> = mutableMapOf()
    private val _giftChannel: Channel<GiftMessage> = Channel(capacity = Channel.UNLIMITED)
    private var _isActive = true
    private var _job: Job? = null

    override suspend fun initialize() {
        createPriorityChannel()
        _job = CoroutineScope(Dispatchers.Default).launch { queueGiftsInternal() }
    }

    override suspend fun enqueue(giftMessage: GiftMessage) {
        if (giftDownloader.isDownloadRequired(giftMessage)) {
            if (giftMessage.userId == "123") {
                giftDownloader.downloadAsset(giftMessage).collect {
                    if (it) _priorityChannels["self"]?.send(giftMessage)
                }
            } else {
                giftDownloader.downloadAsset(giftMessage).collect {
                    if (it) _priorityChannels[giftMessage.slab]?.send(giftMessage)
                }
            }
        } else {
            if (giftMessage.userId == "123") {
                Log.d("Ruben", "send self")
                _priorityChannels["self"]?.send(giftMessage)
            } else {
                Log.d("Ruben", "send others")
                _priorityChannels[giftMessage.slab]?.send(giftMessage)
            }
        }
    }

    override suspend fun dequeue(giftMessage: GiftMessage) {
        giftProcessor.removeProcessedGift(giftMessage)
    }

    override suspend fun getGifts(): Flow<GiftMessage> = flow {
        _giftChannel.consumeEach {
            Log.d("Ruben", "emit")
            emit(it)
        }
    }

    override fun shutDown() {
        fun closeChannels() {
            _priorityChannels.values.forEach {
                it.close()
            }
        }

        closeChannels()
        _isActive = false
        _job?.cancel()
    }

    override fun pauseQueue() {

    }

    override fun resumeQueue() {

    }

    private fun createPriorityChannel() {
        _priorityChannels["self"] = Channel(capacity = Channel.UNLIMITED)
        _priorityChannels[Slab.SLAB_5.toString()] = Channel(capacity = Channel.UNLIMITED)
        _priorityChannels[Slab.SLAB_4.toString()] = Channel(capacity = Channel.UNLIMITED)
        _priorityChannels[Slab.SLAB_3.toString()] = Channel(capacity = Channel.UNLIMITED)
        _priorityChannels[Slab.SLAB_2.toString()] = Channel(capacity = Channel.UNLIMITED)
        _priorityChannels[Slab.SLAB_1.toString()] = Channel(capacity = Channel.UNLIMITED)
    }

    private suspend fun queueGiftsInternal() {
        //wait for collection if paused
        while (_isActive) {
            select<Unit> {
                _priorityChannels.values.forEach {
                    it.onReceiveCatching { result ->
                        result.getOrNull()?.let { gift ->
                            _giftChannel.send(giftProcessor.processGift(gift))
                        }
                    }
                }
            }
        }
    }
}