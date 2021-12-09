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
                commentId = System.currentTimeMillis(),
                giftId = slab.giftId,
                slab = slab.toString(),
                message = message,
                animDuration = slab.animDuration,
                totalDuration = slab.duration,
                userId = if (count%5 == 0) "123" else "456",
                audioUrl = slab.audioUrl,
                animUrl = slab.animUrl
            )
        )
    }

    fun clearDB() = intent {
        repo.clearDB()
    }

    fun clearGift(giftMessage: GiftMessage) {
        giftMap.remove(giftMessage.commentId)
    }

    private fun getNewGiftsInternal() = intent {
        repo.getNewGift().onEach {
            do {
                //wait
            } while (giftMap.size == 2)
        }.buffer().collect {
           giftMap[it.commentId] = it
            reduce {
                //state.copy(giftList = giftMap.values.toList())
                state.copy()
            }
        }
    }
}

enum class Slab(
    val giftId: String,
    val duration: Long,
    val animDuration: Long,
    val animUrl: String,
    val audioUrl: String? = null
) {

    SLAB_1("1", 1_500, 0, "https://www.javatpoint.com/fullformpages/images/png.png") {
        override fun toString(): String {
            return "slab1"
        }
    },

    SLAB_2("2", 2_500, 1_500, "https://www.javatpoint.com/fullformpages/images/png.png") {
        override fun toString(): String {
            return "slab2"
        }
    },

    SLAB_3("3", 4_500, 3_000, "https://www.javatpoint.com/fullformpages/images/png.png") {
        override fun toString(): String {
            return "slab3"
        }
    },

    SLAB_4("4", 5_500, 4_000, "https://www.javatpoint.com/fullformpages/images/png.png") {
        override fun toString(): String {
            return "slab4"
        }
    },

    SLAB_5("5", 9_500, 5_000, "https://www.javatpoint.com/fullformpages/images/png.png") {
        override fun toString(): String {
            return "slab5"
        }
    };

}

enum class Slot {
    SLOT_1, SLOT_2, SPECIAL_SLOT
}