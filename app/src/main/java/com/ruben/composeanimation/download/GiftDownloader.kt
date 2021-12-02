package com.ruben.composeanimation.download

import com.ruben.composeanimation.data.GiftMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Ruben Quadros on 02/12/21
 **/
class GiftDownloader @Inject constructor() {

    fun isDownloadRequired(giftMessage: GiftMessage): Boolean = false

    fun downloadAsset(giftMessage: GiftMessage): Flow<Boolean> = flow { emit(true) }
}