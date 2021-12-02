package com.ruben.composeanimation.di

import com.ruben.composeanimation.queue.GiftProcessor
import com.ruben.composeanimation.queue.GiftProcessorImpl
import com.ruben.composeanimation.queue.GiftQueue
import com.ruben.composeanimation.queue.GiftQueueImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by Ruben Quadros on 02/12/21
 **/
@[Module InstallIn(SingletonComponent::class)]
internal interface QueueModule {

    @[Binds Singleton]
    fun bindGiftProcessor(giftProcessor: GiftProcessorImpl): GiftProcessor

    @[Binds Singleton]
    fun bindGiftQueue(giftQueue: GiftQueueImpl): GiftQueue
}