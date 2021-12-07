package com.ruben.composeanimation.ui.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ruben.composeanimation.ui.MainState
import com.ruben.composeanimation.ui.MainViewModel2
import com.ruben.composeanimation.ui.Slab
import com.ruben.composeanimation.ui.Slot
import kotlinx.coroutines.delay

/**
 * Created by Ruben Quadros on 01/12/21
 **/
@Composable
fun StickyComments(mainViewModel2: MainViewModel2) {

    fun onGiftClear(giftMessage: GiftMessage, slot: Slot) {
        mainViewModel2.clearGift(giftMessage, slot)
    }

    fun onSpecialSlotClear() {
        mainViewModel2.clearSpecialSlot()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val uiStateFlow = mainViewModel2.uiState
    val uiStateFlowLifecycleAware = remember(uiStateFlow, lifecycleOwner) {
        uiStateFlow.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
    }
    val uiState by uiStateFlowLifecycleAware.collectAsState(initial = mainViewModel2.createInitialState())

    //decide where to place the view
    Box(modifier = Modifier.fillMaxSize()) {
        uiState.specialSlot?.let {
            Slab5Gift(
                modifier = Modifier.align(Alignment.Center),
                giftMessage = it,
                onGiftClear = { gift -> onGiftClear(gift, Slot.SPECIAL_SLOT) },
                onSpecialSlotClear = { onSpecialSlotClear() }
            )
        }

        StickyCommentsUI(
            modifier = Modifier.align(Alignment.CenterStart),
            uiState = uiState,
            onGiftClear = { gift, slot -> onGiftClear(gift, slot) }
        )
    }

}

@Composable
fun StickyCommentsUI(modifier: Modifier = Modifier, uiState: MainState, onGiftClear: (GiftMessage, Slot) -> Unit) {
    Column(modifier = modifier.padding(16.dp)) {

        Log.d("Ruben", "ui state $uiState")

        uiState.slot1?.let {
            Log.d("Ruben", "show in slot1 ${it.id}")
            Slot1(giftMessage = it, onGiftClear = onGiftClear)
        }

        Spacer(modifier = modifier.padding(vertical = 16.dp))

        uiState.slot2?.let {
            Log.d("Ruben", "show in slot2 ${it.id}")
            Slot2(giftMessage = it, onGiftClear = onGiftClear)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Slot1(giftMessage: GiftMessage, onGiftClear: (GiftMessage, Slot) -> Unit) {
    GiftItem(giftMessage = giftMessage, onGiftClear = { gift -> onGiftClear.invoke(gift, Slot.SLOT_1) })
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Slot2(giftMessage: GiftMessage, onGiftClear: (GiftMessage, Slot) -> Unit) {
    GiftItem(giftMessage = giftMessage, onGiftClear = { gift -> onGiftClear.invoke(gift, Slot.SLOT_2) })
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Slab5Gift(modifier: Modifier = Modifier, giftMessage: GiftMessage, onGiftClear: (GiftMessage) -> Unit, onSpecialSlotClear: () -> Unit) {
    GiftItem5(
        modifier = modifier,
        giftMessage = giftMessage,
        onGiftClear = { gift -> onGiftClear.invoke(gift) },
        onSpecialSlotClear = onSpecialSlotClear
    )
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GiftItem(modifier: Modifier = Modifier, giftMessage: GiftMessage, onGiftClear: (GiftMessage) -> Unit) {
    //var isVisible by remember { mutableStateOf(true) }

    //Log.d("Ruben", "giftItem $isVisible")

    LaunchedEffect(key1 = giftMessage) {
        Log.d("Ruben", "launched effect begin")
        delay(giftMessage.totalDuration)
        onGiftClear.invoke(giftMessage)
        Log.d("Ruben", "launched effect clear")
        //isVisible = false
    }

    Box(modifier = modifier
        .layoutId("gift_content")
        .background(shape = RoundedCornerShape(10.dp), color = Color.Red)) {
        Text(modifier = Modifier.padding(8.dp), text = giftMessage.message, fontWeight = FontWeight.W700, color = Color.White)
    }

//    AnimatedVisibility(modifier = modifier, visible = isVisible) {
//        Box(modifier = modifier
//            .layoutId("gift_content")
//            .background(shape = RoundedCornerShape(10.dp), color = Color.Red)) {
//            Text(modifier = Modifier.padding(8.dp), text = giftMessage.message, fontWeight = FontWeight.W700, color = Color.White)
//        }
//    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GiftItem5(modifier: Modifier = Modifier, giftMessage: GiftMessage, onGiftClear: (GiftMessage) -> Unit, onSpecialSlotClear: () -> Unit) {
    LaunchedEffect(key1 = giftMessage) {
        Log.d("Ruben", "launched effect begin")
        delay(giftMessage.animDuration)
        onGiftClear.invoke(giftMessage)
        delay(giftMessage.totalDuration-giftMessage.animDuration)
        Log.d("Ruben", "launched effect clear")
        onSpecialSlotClear.invoke()
        //isVisible = false
    }

    Box(modifier = modifier
        .layoutId("gift_content")
        .background(shape = RoundedCornerShape(10.dp), color = Color.Red)) {
        Text(modifier = Modifier.padding(8.dp), text = giftMessage.message, fontWeight = FontWeight.W700, color = Color.White)
    }
}

enum class CurrentView {
    NONE, TOP, BOTTOM
}