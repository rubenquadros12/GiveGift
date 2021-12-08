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
class MainViewModel @Inject constructor(private val repo: MainRepo) :
    ContainerHost<MainState, Nothing>, ViewModel() {

    private val giftMap: MutableMap<Long, GiftMessage> = mutableMapOf()

    override val container: Container<MainState, Nothing> by lazy {
        container(initialState = createInitialState()) {
            getNewGiftsInternal()
        }
    }

    val uiState = container.stateFlow

    fun createInitialState() = MainState()

    fun addNewGift(count: Int, slab: Slab, message: String) = intent {
        Log.d("Ruben", "userdId $count, ${count % 5}")
        repo.insertNewGift(
            GiftMessage(
                id = System.currentTimeMillis(),
                slab = slab.toString(),
                message = message,
                totalDuration = slab.duration,
                userId = if (count % 5 == 0) "ruben" else "pulak",
                resourceName = slab.resourceName
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
                //state.copy(giftList = giftMap.values.toList())
                state.copy()
            }
        }
    }
}

enum class Slab(val resourceName: String, val duration: Long) {

    SLAB_1("tea_samosa_1.webp", 1_500) {
        override fun toString(): String {
            return "slab1"
        }
    },

    SLAB_2("thumbs_up_2.webp", 2_500) {
        override fun toString(): String {
            return "slab2"
        }
    },

    SLAB_3("drums_3.webp", 4_500) {
        override fun toString(): String {
            return "slab3"
        }
    },

    SLAB_4("drums_3.webp", 5_500) {
        override fun toString(): String {
            return "slab4"
        }
    },

    SLAB_5("mia_5.webp", 9_500) {
        override fun toString(): String {
            return "slab5"
        }
    };

    companion object {
        fun isHigherSlab(name: String): Boolean {
            return name == SLAB_3.toString()
                    || name == SLAB_4.toString()
                    || name == SLAB_5.toString()
        }
    }
}