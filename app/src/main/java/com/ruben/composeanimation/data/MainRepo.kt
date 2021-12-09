package com.ruben.composeanimation.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Ruben Quadros on 27/11/21
 **/
@Singleton
class MainRepo @Inject constructor(
    private val dbHelper: DbHelper
) {
    suspend fun insertNewGift(giftMessage: GiftMessage) {
        Log.d("Ruben", "insert new message $giftMessage")
        dbHelper.insertGift(giftMessage)
    }

    fun getNewGift(): Flow<GiftMessage> = dbHelper.getNewGift()

    suspend fun clearDB() = dbHelper.clearDb()
}