package com.ruben.composeanimation.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Ruben Quadros on 27/11/21
 **/
@Singleton
class MainRepo @Inject constructor(
    private val db: AppDatabase
) {
    suspend fun insertNewGift(giftMessage: GiftMessage) {
        Log.d("Ruben", "insert new message $giftMessage")
        db.giftDao().insertGift(giftMessage)
    }

    fun getNewGift(): Flow<GiftMessage> = db.giftDao().getNewGift().filterNotNull()

    suspend fun clearDB() = db.giftDao().clearGifts()
}