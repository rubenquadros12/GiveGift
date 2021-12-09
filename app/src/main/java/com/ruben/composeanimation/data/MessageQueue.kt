package com.ruben.composeanimation.data

import android.util.Log
import com.ruben.composeanimation.utility.GiftConstants.MAX_VISIBLE_GIFTS
import com.ruben.composeanimation.utility.isHighTierSlab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Created by Ruben Quadros on 28/11/21
 **/
class MessageQueue @Inject constructor() {

    private var giftIndex = -1
    private val giftList: MutableList<GiftMessage> = mutableListOf()
    private val interimQueue: Deque<GiftMessage> = LinkedList()
    private val giftChannel: Channel<Deque<GiftMessage>> = Channel(capacity = Channel.UNLIMITED)
    private var callback: MyCallback? = null

    suspend fun enqueue(giftMessage: GiftMessage) {
        Log.d("Ruben", "enqueue ${giftMessage.slab}")
        if (giftMessage.userId == "123") {
            interimQueue.addFirst(giftMessage)
        } else {
            interimQueue.add(giftMessage)
        }
        giftChannel.send(interimQueue)
    }

    fun clearGift(giftMessage: GiftMessage) {
        giftIndex = giftList.indexOfFirst { it.commentId == giftMessage.commentId }
        if (interimQueue.isNotEmpty()) interimQueue.removeFirst()
        giftList.removeAt(giftIndex)
        callback?.onDataChange(giftList.size)
        Log.d("Ruben", "removed ${giftList.size}")
    }

    /**
     * @param giftMessage - incoming gift
     * S1 and S2 gifts can be shown in parallel
     * S3, S4 and S5 will be shown one at a time
     */
    suspend fun getGifts(): Flow<List<GiftMessage>> = flow {
        giftChannel.consumeEach { queue ->
            if (giftList.isEmpty()) {
                //no gifts you can add
                queue.peek()?.let {
                    giftList.add(it)
                }
                emit(giftList.toList())
            } else {
                if (queue.peek()?.slab?.isHighTierSlab() == true) {
                    //wait for all gifts to be displayed
                    if (giftList.isNotEmpty()) {
                        suspendCancellableCoroutine<Unit> { continuation ->
                            Log.d("Ruben", "in suspendCancellableCoroutine 61")
                            callback = object : MyCallback {
                                override fun onDataChange(size: Int) {
                                    if (size == 0) {
                                        if (continuation.isActive) {
                                            Log.d("Ruben", "resume suspendCancellableCoroutine 66")
                                            continuation.resume(Unit)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    queue.peek()?.let {
                        giftList.add(it)
                    }
                    emit(giftList.toList())
                } else {
                    when {
                        giftList.size == MAX_VISIBLE_GIFTS -> {
                            //wait for a gift to finish displaying
                            suspendCancellableCoroutine<Unit> { continuation ->
                                Log.d("Ruben", "in suspendCancellableCoroutine 82")
                                callback = object : MyCallback {
                                    override fun onDataChange(size: Int) {
                                        if (size == 1) {
                                            if (continuation.isActive) {
                                                Log.d("Ruben", "resume suspendCancellableCoroutine 87")
                                                continuation.resume(Unit)
                                            }
                                        }
                                    }
                                }
                            }
                            queue.peek()?.let {
                                if (giftList.contains(it)) {
                                    val gift = queue.first { gift -> giftList.contains(gift).not() }
                                    giftList.add(giftIndex, gift)
                                } else {
                                    giftList.add(giftIndex, it)
                                }
                            }
                            emit(giftList.toList())
                        }
                        giftList[0].slab.isHighTierSlab() -> {
                            //wait for the gift to finish displaying
                            suspendCancellableCoroutine<Unit> { continuation ->
                                Log.d("Ruben", "in suspendCancellableCoroutine 107")
                                callback = object : MyCallback {
                                    override fun onDataChange(size: Int) {
                                        if (size == 0) {
                                            if (continuation.isActive) {
                                                Log.d("Ruben", "resume suspendCancellableCoroutine 112")
                                                continuation.resume(Unit)
                                            }
                                        }
                                    }
                                }
                            }
                            queue.peek()?.let {
                                giftList.add(it)
                            }
                            emit(giftList.toList())
                        }
                        else -> {
                            //s1 or s2 gift present
                            //can add one more s1 or s2 gift
                            if (giftIndex != -1) {

                                queue.peek()?.let {
                                    if (giftList.contains(it)) {
                                        val gift = queue.first { gift -> giftList.contains(gift).not() }
                                        giftList.add(giftIndex, gift)
                                    } else {
                                        giftList.add(giftIndex, it)
                                    }
                                }
                                emit(giftList.toList())
                            } else {
                                queue.peek()?.let {
                                    if (giftList.contains(it)) {
                                        val gift = queue.first { gift -> giftList.contains(gift).not() }
                                        giftList.add(gift)
                                    } else {
                                        giftList.add(it)
                                    }
                                }
                                emit(giftList.toList())
                            }
                        }
                    }
                }
            }
        }
    }
}

interface MyCallback {
    fun onDataChange(size: Int)
}