package com.ruben.composeanimation.data

import android.util.Log
import com.ruben.composeanimation.utility.GiftConstants.MAX_VISIBLE_GIFTS
import com.ruben.composeanimation.utility.isHighTierSlab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

/**
 * Created by Ruben Quadros on 28/11/21
 **/
class MessageQueue @Inject constructor() {

    private var giftIndex = -1
    private val giftList: MutableList<GiftMessage> = mutableListOf()
    private val interimQueue: Deque<GiftMessage> = LinkedList()

    suspend fun enqueue(giftMessage: GiftMessage): Flow<List<GiftMessage>> = flow {
        Log.d("Ruben", "enqueue ${giftMessage.slab}")
        interimQueue.add(giftMessage)
        Log.d("Ruben", "in queue? ${interimQueue.size}")
        emit(addIncomingGift().toList())

        /*giftMessage.onEach { gift ->
            Log.d("Ruben", "incoming gift from usecase ${gift.slab}, ${gift.userId}")
            addIncomingGift(gift)
//            if (gift.userId == "123") {
//                interimQueue.addFirst(gift)
//            } else {
//                interimQueue.add(gift)
//            }
//            interimQueue.first { giftList.contains(it).not() }?.let {
//                addIncomingGift(it)
//            }
//            do {
//                //keep waiting
//            } while (giftList.size == 2)
//            if (giftIndex != -1) {
//                giftList.add(giftIndex, gift)
//            } else {
//                giftList.add(gift)
//            }
        }.buffer(capacity = Channel.UNLIMITED).collect {
            Log.d("Ruben", "$giftList")
            emit(giftList.toList())
        }*/
//        giftMessage.onEach { gift ->
//            Log.d("Ruben", "incoming gift from usecase ${gift.slab}, ${gift.userId}")
//            giftList.add(gift)
//        }.buffer(capacity = Channel.UNLIMITED).collect {
//            emit(giftList.toList())
//        }
    }

    fun clearGift(giftMessage: GiftMessage) {
        giftIndex = giftList.indexOfFirst { it.id == giftMessage.id }
        if (interimQueue.isNotEmpty()) interimQueue.removeFirst()
        giftList.removeAt(giftIndex)
    }

    /**
     * @param giftMessage - incoming gift
     * S1 and S2 gifts can be shown in parallel
     * S3, S4 and S5 will be shown one at a time
     */
    private fun addIncomingGift(): List<GiftMessage> {
        if (giftList.isEmpty()) {
            //no gifts you can add
            //Log.d("Ruben", "nothing present")
            interimQueue.peek()?.let {
                giftList.add(it)
            }
            return giftList
        } else {
            if (interimQueue.peek()?.slab?.isHighTierSlab() == true) {
                //Log.d("Ruben", "incoming gift ${giftMessage.slab} ${giftList.size}")
                do {
                    //wait for all gifts to be displayed
                } while (giftList.size >= 1)
                interimQueue.peek()?.let {
                    giftList.add(it)
                }
                return giftList
            } else {
                //Log.d("Ruben", "incoming gift else ${giftMessage.slab} ${giftList.size}")
                when {
                    giftList.size == MAX_VISIBLE_GIFTS -> {
                        //Log.d("Ruben", "size2")
                        do {
                            //wait for gift to finish displaying
                        } while (giftList.size == MAX_VISIBLE_GIFTS)
                        interimQueue.peek()?.let {
                            giftList.add(giftIndex, it)
                        }
                        return giftList
                    }
                    giftList[0].slab.isHighTierSlab() -> {
                        //Log.d("Ruben", "size1 and s3,s4,s5 ${giftList[0].slab}")
                        do {
                            //wait for gift to finish displaying
                        } while (giftList.size >= MAX_VISIBLE_GIFTS - 1)
                        interimQueue.peek()?.let {
                            giftList.add(it)
                        }
                        return giftList
                    }
                    else -> {
                        //s1 or s2 gift present
                        //can add one more s1 or s2 gift
                        //Log.d("Ruben", "s1 or s2 present")
                        return if (giftIndex != -1) {
                            interimQueue.peek()?.let {
                                giftList.add(giftIndex, it)
                            }
                            giftList
                        } else {
                            interimQueue.peek()?.let {
                                giftList.add(it)
                            }
                            giftList
                        }
                    }
                }
            }
        }
    }

    private suspend fun evictGifts(giftMessage: GiftMessage) {
        delay(giftMessage.totalDuration)
        giftIndex = giftList.indexOfFirst { it.id == giftMessage.id }
        giftList.removeAt(giftIndex)
        if (interimQueue.isNotEmpty()) interimQueue.remove(giftMessage)
    }
}