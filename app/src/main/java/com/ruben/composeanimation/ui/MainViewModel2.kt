package com.ruben.composeanimation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.data.GiftQueue
import com.ruben.composeanimation.data.MessageQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

/**
 * Created by Ruben Quadros on 28/11/21
 **/
@HiltViewModel
class MainViewModel2 @Inject constructor(
    private val useCase: GetGiftUseCase,
    private val giftQueue: GiftQueue,
    private val messageQueue: MessageQueue,
): ContainerHost<MainState, Nothing>, ViewModel() {

    override val container: Container<MainState, Nothing> by lazy {
        container(initialState = createInitialState()) {
            getNewGiftsInternal()
            getQueuedGifts()
        }
    }

    val uiState = container.stateFlow

    fun createInitialState() = MainState()

    private fun getQueuedGifts() = intent {
        giftQueue.getGifts().collect {
            Log.d("Ruben", "ui data $it")
            reduce { state.copy(giftList = it.toList()) }
        }
    }

    private fun getNewGiftsInternal() = intent {
//        fun enqueueIncomingGift(giftMessage: GiftMessage) = intent {
//            messageQueue.enqueue(giftMessage).collect {
//                reduce { state.copy(giftList = it) }
//            }
//        }
//
//        useCase.getGifts().collect {
//            Log.d("Ruben", "got from usecase ${it.slab}")
//            giftChannel.send(it)
//            enqueueIncomingGift(it)
//        }
//        messageQueue.enqueue(useCase.getGifts()).collect {
//            reduce { state.copy(giftList = it) }
//        }

        useCase.getGifts().collect {
            Log.d("Ruben", "got from usecase ${it.slab}")
            giftQueue.queueIncomingGift(it)
        }
    }

    fun clearGift(giftMessage: GiftMessage) = intent {
        //messageQueue.clearGift(giftMessage)
        giftQueue.clearGift(giftMessage)
    }
}

data class GiftRequest(
    val gift: GiftMessage,
    val callback: CompletableDeferred<List<GiftMessage>>
)