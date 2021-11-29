package com.ruben.composeanimation.data

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Ruben Quadros on 27/11/21
 **/
@Singleton
class MainRepo @Inject constructor(
    @ApplicationContext context: Context
) {

    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "gift.db").build()

    suspend fun insertNewGift(giftMessage: GiftMessage) = db.giftDao().insertGift(giftMessage)

    fun getNewGift(): Flow<GiftMessage> = db.giftDao().getNewGift().filterNotNull()

    suspend fun clearDB() = db.giftDao().clearGifts()
}