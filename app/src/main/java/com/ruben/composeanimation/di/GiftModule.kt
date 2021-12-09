package com.ruben.composeanimation.di

import com.ruben.composeanimation.data.DbHelper
import com.ruben.composeanimation.data.DbHelperImpl
import com.ruben.composeanimation.data.DownloadService
import com.ruben.composeanimation.download.GiftCache
import com.ruben.composeanimation.download.GiftCacheImpl
import com.ruben.composeanimation.download.GiftDownloader
import com.ruben.composeanimation.download.GiftDownloaderImpl
import com.ruben.composeanimation.queue.GiftProcessor
import com.ruben.composeanimation.queue.GiftProcessorImpl
import com.ruben.composeanimation.queue.GiftQueue
import com.ruben.composeanimation.queue.GiftQueueImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Ruben Quadros on 02/12/21
 **/
@[Module InstallIn(SingletonComponent::class)]
internal interface QueueModule {

    @[Binds Singleton]
    fun bindGiftProcessor(giftProcessor: GiftProcessorImpl): GiftProcessor

    @[Binds Singleton]
    fun bindGiftQueue(giftQueue: GiftQueueImpl): GiftQueue

    @[Binds Singleton]
    fun bindDbHelper(dbHelper: DbHelperImpl): DbHelper

    @[Binds Singleton]
    fun bindDownloader(giftDownloader: GiftDownloaderImpl): GiftDownloader

    @[Binds Singleton]
    fun bindCache(giftCache: GiftCacheImpl): GiftCache
}

@[Module InstallIn(SingletonComponent::class)]
internal object NetworkModule {

    @[Provides Singleton]
    fun provideRetrofitService(): DownloadService =
        Retrofit.Builder()
            .baseUrl("https://ruben.com/")
            .client(OkHttpClient.Builder().build())
            .build().create(DownloadService::class.java)

}