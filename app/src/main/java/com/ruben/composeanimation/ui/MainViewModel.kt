package com.ruben.composeanimation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.data.MainRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

/**
 * Created by Ruben Quadros on 27/11/21
 **/
@HiltViewModel
class MainViewModel @Inject constructor(private val repo: MainRepo): ContainerHost<MainState, Nothing>,  ViewModel() {

    private val giftMap: MutableMap<Long, GiftMessage> = mutableMapOf()

    override val container: Container<MainState, Nothing> by lazy {
        container(initialState = createInitialState()) {
            getNewGiftsInternal()
        }
    }

    val uiState = container.stateFlow

    fun createInitialState() = MainState()

    fun addNewGift(count: Int, slab: Slab, message: String) = intent {
        Log.d("Ruben", "userdId $count, ${count%5}")
        repo.insertNewGift(
            GiftMessage(
                id = System.currentTimeMillis(),
                slab = slab.toString(),
                message = message,
                totalDuration = slab.duration,
                userId = if (count%5 == 0) "123" else "456"
            )
        )
    }

    fun clearDB() = intent {
        repo.clearDB()
    }

    fun clearGift(giftMessage: GiftMessage) {
        giftMap.remove(giftMessage.id)
    }

    private fun getNewGiftsInternal() = intent {
        repo.getNewGift().onEach {
            do {
                //wait
            } while (giftMap.size == 2)
        }.buffer().collect {
           giftMap[it.id] = it
            reduce {
                state.copy(giftList = giftMap.values.toList())
            }
        }
    }
}

enum class Slab(val duration: Long) {

    SLAB_1(1_500) {
        override fun toString(): String {
            return "slab1"
        }
    },

    SLAB_2(2_500) {
        override fun toString(): String {
            return "slab2"
        }
    },

    SLAB_3(4_500) {
        override fun toString(): String {
            return "slab3"
        }
    },

    SLAB_4(5_500) {
        override fun toString(): String {
            return "slab4"
        }
    },

    SLAB_5(9_500) {
        override fun toString(): String {
            return "slab5"
        }
    };

}