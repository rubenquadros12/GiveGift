package com.ruben.composeanimation.queue

import android.util.Log
import com.ruben.composeanimation.domain.GiftMessageEntity
import com.ruben.composeanimation.download.GiftCache
import com.ruben.composeanimation.download.models.CacheSuccess
import com.ruben.composeanimation.download.models.DownloadInfo
import com.ruben.composeanimation.queue.models.DequeueResult
import com.ruben.composeanimation.queue.models.EnqueueResult
import com.ruben.composeanimation.queue.models.GiftDequeued
import com.ruben.composeanimation.queue.models.GiftEnqueued
import com.ruben.composeanimation.queue.models.GiftNotDownloaded
import com.ruben.composeanimation.ui.Slab
import javax.inject.Inject
import kotlin.coroutines.resume
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
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Created by Ruben Quadros on 30/11/21
 **/
class GiftQueueImpl @Inject constructor(
    private val giftProcessor: GiftProcessor,
    private val giftCache: GiftCache,
    private val queueUtil: QueueUtil
) : GiftQueue {

    private val _priorityChannels: MutableMap<String, Channel<GiftMessageEntity>> = mutableMapOf()
    private val _giftChannel: Channel<GiftMessageEntity> = Channel(capacity = Channel.UNLIMITED)
    private var _isActive = true
    private var _job: Job? = null
    private var _isPause = false
    private var _resumeCallback: ResumeCallback? = null

    override suspend fun initialize() {
        //giftCache.initialize()
        _isActive = true
        Log.d("Ruben", "init")
        createPriorityChannel()
        observeCache()
        _job = CoroutineScope(Dispatchers.Default).launch { queueGiftsInternal() }
    }

    override suspend fun enqueue(giftMessage: GiftMessageEntity): Flow<EnqueueResult> = flow {
        val result = giftCache.getCachedGift(id = giftMessage.giftId)
        Log.d("Ruben", "cache result $result")
        if (result != null) {
            if (giftMessage.userId == "123") {
                Log.d("Ruben", "send self")
                giftMessage.attachCachedResource(result)
                _priorityChannels["self"]?.send(giftMessage)
            } else {
                Log.d("Ruben", "send others")
                giftMessage.attachCachedResource(result)
                _priorityChannels[giftMessage.slab]?.send(giftMessage)
            }
            emit(GiftEnqueued(giftMessage))
        } else {
            emit(GiftNotDownloaded(giftMessage))
            cacheGift(giftMessage)
        }
    }

    override suspend fun dequeue(giftMessage: GiftMessageEntity): Flow<DequeueResult> = flow {
        giftProcessor.removeProcessedGift(giftMessage)
        emit(GiftDequeued(giftMessage))
    }

    override suspend fun getGifts(): Flow<GiftMessageEntity> = flow {
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
        //for viewer you can shutdown on pause
        //shutDown()
        //if host goes in pause you should hold onto values in queue
        _isPause = true
    }

    override suspend fun resumeQueue() {
        //for viewer you can reinitialize on resume
        //initialize()
        //if host resumes you need to resume queue from previous point
        _resumeCallback?.onResume()
        _isPause = false
    }

    private suspend fun cacheGift(giftMessage: GiftMessageEntity) {
        giftCache.cacheGift(
            DownloadInfo(
                downloadId = giftMessage.giftId,
                animUrl = giftMessage.animUrl,
                audioUrl = giftMessage.audioUrl,
                requestId = giftMessage.commentId
            )
        ).collect {
            //check status
        }
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
            if (_isPause) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    Log.d("Ruben", "pause the collection")
                    _resumeCallback = object : ResumeCallback {
                        override fun onResume() {
                            if (continuation.isActive) {
                                Log.d("Ruben", "resume the collection")
                                continuation.resume(Unit)
                            }
                        }
                    }
                }
            }
            select<Unit> {
                _priorityChannels.values.forEach {
                    it.onReceiveCatching { result ->
                        result.getOrNull()?.let { gift ->
                            Log.d("Ruben", "selection done ${gift.slab}")
                            //can filter if gift doesnt need to be processed
                            _giftChannel.send(giftProcessor.processGift(gift))
                        }
                    }
                }
            }
        }
    }

    private suspend fun observeCache() {
        giftCache.getCacheResult().collect { cacheResult ->
            when (cacheResult) {
                is CacheSuccess -> {
                    //from request id determine ur gift
                    //then send it to queue
                    val giftMessage = queueUtil.getGiftMessage(cacheResult.downloadInfo.requestId)
                    giftMessage?.let {
                        it.attachCachedResource(cacheResult.cachedResource)
                        if (it.userId == "123") {
                            _priorityChannels["self"]?.send(it)
                        } else {
                            _priorityChannels[giftMessage.slab]?.send(it)
                        }
                    }
                }
            }
        }
    }
}

interface ResumeCallback {
    fun onResume()
}