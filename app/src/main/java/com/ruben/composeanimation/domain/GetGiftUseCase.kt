package com.ruben.composeanimation.domain

import android.util.Log
import com.ruben.composeanimation.data.MainRepo
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by Ruben Quadros on 28/11/21
 **/
class GetGiftUseCase @Inject constructor(private val repo: MainRepo) {

    fun getGifts(): Flow<GiftMessageEntity> {
        return repo.getNewGift().map {
            Log.d("Ruben", "mapping")
            GiftMessageEntity(
                commentId = it.commentId,
                giftId = it.giftId,
                animDuration = it.animDuration,
                totalDuration = it.totalDuration,
                userId = it.userId,
                slab = it.slab,
                message = it.message,
                animUrl = it.animUrl,
                audioUrl = it.audioUrl
            )
        }
    }

}

data class GiftMessageEntity(
    val commentId: Long,
    val giftId: String,
    val animDuration: Long,
    val totalDuration: Long,
    val userId: String,
    val slab: String,
    val message: String,
    val animUrl: String,
    val audioUrl: String?,
    var animSource: String = "",
    var audioSource: String? = null
)