package com.ruben.composeanimation.ui.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.ui.Gift
import com.ruben.composeanimation.ui.GiftMessageContent
import com.ruben.composeanimation.ui.MainViewModel2
import kotlinx.coroutines.delay

/**
 * Created by Ruben Quadros on 01/12/21
 **/
@Composable
fun StickyComments(mainViewModel2: MainViewModel2) {

    fun onGiftClear(giftMessage: GiftMessage) {
        mainViewModel2.clearGift(giftMessage)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val uiStateFlow = mainViewModel2.uiState
    val uiStateFlowLifecycleAware = remember(uiStateFlow, lifecycleOwner) {
        uiStateFlow.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
    }
    val uiState by uiStateFlowLifecycleAware.collectAsState(initial = mainViewModel2.createInitialState())

    //decide where to place the view
    if (uiState.giftList.isNotEmpty()) {
        GiftMessageContent(giftMessage = uiState.giftList[0], onGiftClear = {onGiftClear(it)})
//        PlaceableView(
//            currentView = CurrentView.NONE,
//            giftMessage = uiState.giftList[0],
//            onGiftClear = {onGiftClear(it)},
//            content = {
//                Gift(giftMessage = uiState.giftList[0])
//            }
//        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlaceableView(
    currentView: CurrentView,
    giftMessage: GiftMessage,
    onGiftClear: (GiftMessage) -> Unit,
    content: @Composable () -> Unit
) {

    Log.d("Ruben", "in placeable ${giftMessage.slab}")

    var isVisible by remember {
        mutableStateOf(true)
    }

    Log.d("Ruben", "in placeable $isVisible")

    LaunchedEffect(key1 = true) {
        delay(giftMessage.totalDuration)
        onGiftClear.invoke(giftMessage)
        isVisible = false
    }

    AnimatedVisibility(
        visible = isVisible
    ) {
        content()
    }

}

@Composable
fun GiftItem(giftMessage: GiftMessage) {
    Box(modifier = Modifier
        .size(100.dp)
        .layoutId("gift_content")
        .background(shape = RoundedCornerShape(10.dp), color = Color.Red)) {
        Text(modifier = Modifier.padding(8.dp), text = giftMessage.message, fontWeight = FontWeight.W700, color = Color.White)
    }
}

enum class CurrentView {
    NONE, TOP, BOTTOM
}