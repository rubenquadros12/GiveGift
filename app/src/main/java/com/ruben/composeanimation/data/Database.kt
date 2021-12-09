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
    @ColumnInfo(name = "comment_id")
    @PrimaryKey
    val commentId: Long = -1,
    @ColumnInfo(name = "gift_id")
    val giftId: String = "-1",
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
    @ColumnInfo(name = "anim_url")
    val animUrl: String,
    @ColumnInfo(name = "audio_url")
    val audioUrl: String?
)

@Entity(tableName = "gift_animation")
data class GiftAnimation(
    @ColumnInfo(name = "gift_id")
    @PrimaryKey
    val id: String = "-1",
    @ColumnInfo(name = "animation_location")
    val giftLocation: String = "",
    @ColumnInfo(name = "animation_source")
    val giftSource: String,
    @ColumnInfo(name = "sound_location")
    val soundLocation: String? = null,
    @ColumnInfo(name = "sound_source")
    val soundSource: String? = null,
    @ColumnInfo(name = "created_at")
    val createdTime: Long,
    @ColumnInfo(name = "updated_at")
    val updatedTime: Long = -1,
    @ColumnInfo(name = "status")
    val giftStatus: GiftStatus,
    @ColumnInfo(name = "request_id")
    val requestId: Long
)

enum class GiftStatus {
    DOWNLOADED, DOWNLOADING, DOWNLOAD_QUEUED, NOT_PRESENT
}

@Dao
interface GiftDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGift(giftMessage: GiftMessage)

    @Query("SELECT * FROM `gift_message` ORDER BY `comment_id` DESC LIMIT 1")
    fun getNewGift(): Flow<GiftMessage>

    @Query("DELETE FROM `gift_message`")
    suspend fun clearGifts()

    @Query("SELECT * FROM `gift_message` WHERE `comment_id` =:commentId")
    suspend fun getGift(commentId: Long): GiftMessage?
}

@Dao
interface AnimationDao {
    @Query("SELECT * FROM `gift_animation` WHERE `gift_id` =:giftId AND `status` = 'DOWNLOADED'")
    suspend fun getAnimation(giftId: String): GiftAnimation?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGiftAnimation(giftAnimation: GiftAnimation)

    @Query("UPDATE `gift_animation` SET `updated_at` =:timestamp WHERE (`animation_source` =:source OR `sound_source` =:source)")
    suspend fun updateLastUsedTime(timestamp: Long, source: String)

    @Query("UPDATE `gift_animation` SET `updated_at` =:updatedTime, `sound_location` =:audioLocation, `animation_location` =:animLocation, `status` =:giftStatus WHERE `gift_id` =:giftId")
    suspend fun updateGiftDownloadStatus(
        giftId: String,
        giftStatus: GiftStatus,
        animLocation: String,
        audioLocation: String?,
        updatedTime: Long
    )

    @Query("SELECT * FROM `gift_animation` ORDER BY `updated_at` DESC LIMIT 1")
    fun getDownloadStatus(): Flow<GiftAnimation>

}

@Database(entities = [GiftMessage::class, GiftAnimation::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun giftDao(): GiftDao

    abstract fun animDao(): AnimationDao

}

@[Module InstallIn(SingletonComponent::class)]
internal object DbModule {

    @[Provides Singleton]
    fun provideAppDb(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "gift.db").build()
    }

}