package com.ruben.composeanimation.data

import androidx.room.*
import androidx.room.Database
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ruben Quadros on 27/11/21
 **/
@Entity(tableName = "gift_message")
data class GiftMessage(
    @ColumnInfo(name = "id")
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "gift_slab")
    val slab: String,
    @ColumnInfo(name = "message")
    val message: String,
    @ColumnInfo(name = "total_duration")
    val totalDuration: Long
)

@Dao
interface GiftDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGift(giftMessage: GiftMessage)

    @Query("SELECT * FROM `gift_message` ORDER BY `id` DESC LIMIT 1")
    fun getNewGift(): Flow<GiftMessage>

    @Query("DELETE FROM `gift_message`")
    suspend fun clearGifts()
}

@Database(entities = [GiftMessage::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun giftDao(): GiftDao

}