package com.ruben.composeanimation.ui

import androidx.lifecycle.ViewModel
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.data.MessageQueue
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val messageQueue: MessageQueue,
): ContainerHost<MainState, Nothing>, ViewModel() {

    override val container: Container<MainState, Nothing> by lazy {
        container(initialState = createInitialState()) {
            getNewGiftsInternal()
        }
    }

    val uiState = container.stateFlow

    fun createInitialState() = MainState()

    private fun getNewGiftsInternal() = intent {
        messageQueue.enqueue(useCase.getGifts()).collect {
            reduce { state.copy(giftList = it) }
        }
    }

    fun clearGift(giftMessage: GiftMessage) {
        messageQueue.clearGift(giftMessage)
    }
}