package com.ruben.composeanimation.ui

import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.data.MainRepo
import com.ruben.composeanimation.data.MessageQueue
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by Ruben Quadros on 28/11/21
 **/
class GetGiftUseCase @Inject constructor(private val repo: MainRepo) {

    fun getGifts(): Flow<GiftMessage> {
        return repo.getNewGift()
    }

}