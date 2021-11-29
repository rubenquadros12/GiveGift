package com.ruben.composeanimation.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Ruben Quadros on 28/11/21
 **/
class MessageQueue @Inject constructor() {

    private var giftIndex = -1
    private val giftList: MutableList<GiftMessage> = mutableListOf()

    suspend fun enqueue(giftMessage: Flow<GiftMessage>): Flow<List<GiftMessage>> = flow {
        giftMessage.onEach {
            do {
                //keep waiting
            } while (giftList.size == 2)
            if (giftIndex != -1) {
                giftList.add(giftIndex, it)
            } else {
                giftList.add(it)
            }
        }.buffer(capacity = Channel.UNLIMITED).collect {
            emit(giftList.toList())
            //evictGifts(it)
//            CoroutineScope(Dispatchers.Default).launch {
//                evictGifts(it)
//            }
        }
    }


    private suspend fun evictGifts(giftMessage: GiftMessage) {
        delay(giftMessage.totalDuration)
        giftIndex = giftList.indexOfFirst { it.id == giftMessage.id }
        giftList.removeAt(giftIndex)
    }

    fun clearGift(giftMessage: GiftMessage) {
        giftIndex = giftList.indexOfFirst { it.id == giftMessage.id }
        giftList.removeAt(giftIndex)
    }
}