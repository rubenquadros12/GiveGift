package com.ruben.composeanimation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ruben.composeanimation.data.MessageQueue
import com.ruben.composeanimation.data.MockData
import com.ruben.composeanimation.domain.GetGiftUseCase
import com.ruben.composeanimation.domain.GiftMessageEntity
import com.ruben.composeanimation.download.GiftCache
import com.ruben.composeanimation.download.models.CacheDirectoryEmpty
import com.ruben.composeanimation.download.models.CacheScanSuccess
import com.ruben.composeanimation.download.models.NoCacheDirectory
import com.ruben.composeanimation.queue.GiftQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

/**
 * Created by Ruben Quadros on 28/11/21
 **/
@HiltViewModel
class MainViewModel2 @Inject constructor(
    private val useCase: GetGiftUseCase,
    private val giftQueue: GiftQueue,
    private val messageQueue: MessageQueue,
    private val giftCache: GiftCache
): ContainerHost<MainState, Nothing>, ViewModel() {

    override val container: Container<MainState, Nothing> by lazy {
        container(initialState = createInitialState()) {
            initCache()
            scanCache()
            getNewGiftsInternal()
            getQueuedGifts()
            initializeQueue()
        }
    }

    val uiState = container.stateFlow

    fun createInitialState() = MainState()

    private fun initCache() = intent {
        giftCache.initialize()
    }

    private fun scanCache() = intent {
        giftCache.performCacheScan().collect { cacheScanResult ->
            Log.d("Ruben", "cache scan $cacheScanResult")
            when (cacheScanResult) {
                is NoCacheDirectory, CacheDirectoryEmpty -> {
                    //download all files
                    giftCache.preCacheGifts(MockData.getMockAnimAssets())
                }
                is CacheScanSuccess -> {
                    //sync with db
                }
            }
        }
    }

    private fun initializeQueue() = intent {
        giftQueue.initialize()
    }

    private fun getQueuedGifts() = intent {
//        messageQueue.getGifts().collect {
//            Log.d("Ruben", "ui data $it")
//            reduce { state.copy(giftList = it.toList()) }
//        }
        giftQueue.getGifts().collect {
            Log.d("Ruben", "ui data $it")
            when {
                it.slab == Slab.SLAB_5.toString() -> {
                    reduce { state.copy(specialSlot = it) }
                }
                state.slot1 == null && state.slot2 == null -> {
                    Log.d("Ruben", "select slot both null ${it.slab}, ${it.commentId}")
                    reduce { state.copy(slot1 = it) }
                }
                state.slot1 == null -> {
                    Log.d("Ruben", "select slot slot1 null ${it.slab}, ${it.commentId}")
                    reduce { state.copy(slot1 = it) }
                }
                state.slot2 == null -> {
                    Log.d("Ruben", "select slot slot2    null ${it.slab}, ${it.commentId}")
                    reduce { state.copy(slot2 = it) }
                }
            }
            //reduce { state.copy(giftList = it.toList()) }
        }
    }

    private fun getNewGiftsInternal() = intent {
//        useCase.getGifts().collect {
//            Log.d("Ruben", "got from usecase ${it.slab}")
//            messageQueue.enqueue(it)
//        }

        useCase.getGifts().collect {
            Log.d("Ruben", "got from usecase ${it.slab}")
            giftQueue.enqueue(it).collect { enqueueResult ->
                //check status
                Log.d("Ruben", "enqueue result $enqueueResult")
            }
        }
    }

    fun clearGift(giftMessage: GiftMessageEntity, slot: Slot) = intent {
        Log.d("Ruben", "clear gift ${giftMessage.slab}, ${giftMessage.commentId}, ${slot.name}")
        reduce { when (slot) {
            Slot.SLOT_1 -> state.copy(slot1 = null)
            Slot.SLOT_2 -> state.copy(slot2 = null)
            else -> state
        } }.also {
            //delay(100)
            giftQueue.dequeue(giftMessage).collect {
                //check status
            }
        }
        //messageQueue.clearGift(giftMessage)
    }

    fun clearSpecialSlot() = intent {
        reduce { state.copy(specialSlot = null) }
    }

    fun onStart() = intent {
        Log.d("Ruben", "resume")
        giftQueue.resumeQueue()
    }

    fun onStop() = intent {
        Log.d("Ruben", "pause")
        giftQueue.pauseQueue()
    }

    override fun onCleared() {
        super.onCleared()
        giftQueue.shutDown()
    }
}