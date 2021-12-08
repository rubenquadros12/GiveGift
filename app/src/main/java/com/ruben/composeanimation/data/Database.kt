package com.ruben.composeanimation.data

import android.content.Context
import androidx.room.*
import androidx.room.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

/**
 * Created by Ruben Quadros on 27/11/21
 **/
@Entity(tableName = "gift_message")
data class GiftMessage(
    @ColumnInfo(name = "id")
    @PrimaryKey
    val id: Long = -1,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
    @ColumnInfo(name = "gift_slab")
    val slab: String = "",
    @ColumnInfo(name = "message")
    val message: String = "",
    @ColumnInfo(name = "anim_duration")
    val animDuration: Long = 0,
    @ColumnInfo(name = "total_duration")
    val totalDuration: Long = 0,
    @ColumnInfo(name = "res_name")
    val resourceName: String = ""
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

@[Module InstallIn(SingletonComponent::class)]
internal object DbModule {

    @[Provides Singleton]
    fun provideAppDb(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "gift.db").build()
    }

}