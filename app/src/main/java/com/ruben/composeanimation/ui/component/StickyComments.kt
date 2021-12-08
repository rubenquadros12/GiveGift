package com.ruben.composeanimation.ui.component

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.github.penfeizhou.animation.loader.AssetStreamLoader
import com.github.penfeizhou.animation.webp.WebPDrawable
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.ui.MainState
import com.ruben.composeanimation.ui.MainViewModel2
import com.ruben.composeanimation.ui.Slab
import com.ruben.composeanimation.ui.Slot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Ruben Quadros on 01/12/21
 **/
@ExperimentalAnimationApi
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

    var centerGiftState by remember {
        mutableStateOf<CenterGift?>(null)
    }

    val webpDrawable = if (centerGiftState != null) {
        val assetLoader = AssetStreamLoader(
            LocalContext.current,
            centerGiftState!!.assetName
        )
        WebPDrawable(assetLoader)
    } else {
        null
    }


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
            showCenterAnim = {
                centerGiftState = it
            },
            onGiftClear = { gift, slot ->
                onGiftClear(gift, slot)
                centerGiftState = null
            }
        )
        if (webpDrawable != null)
            CenterAnimation(webpDrawable, centerGiftState!!)
    }

}

data class CenterGift(
    val assetName: String?,
    val offsetDest: IntOffset = IntOffset.Zero,
    val destSize: Int,
)

enum class AnimationState {
    ENTER,
    SETTLE,
    EXIT
}

@Composable
fun CenterAnimation(webpDrawable: WebPDrawable, centerGift: CenterGift) {

    val coroutine = rememberCoroutineScope()
    var animationState by remember { mutableStateOf(AnimationState.ENTER) }

    var offsetSrc by remember { mutableStateOf(IntOffset.Zero) }
    val animateOffset by animateIntOffsetAsState(
        targetValue = if (animationState == AnimationState.ENTER) offsetSrc else centerGift.offsetDest,
        animationSpec = TweenSpec(durationMillis = 1000),
        finishedListener = {

        }
    )
    var scaleDest by remember {
        mutableStateOf(0f)
    }
    Log.d("scale", scaleDest.toString())
    val scale by animateFloatAsState(
        targetValue = if (animationState == AnimationState.SETTLE) scaleDest else 1f,
        animationSpec = TweenSpec(durationMillis = 1000)
    )
    Layout(content = {
        val callback = object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationStart(drawable: Drawable?) {
                super.onAnimationStart(drawable)
                coroutine.launch {
                    delay(1000)
                    animationState = AnimationState.SETTLE
                }
            }

            override fun onAnimationEnd(drawable: Drawable?) {
                super.onAnimationEnd(drawable)
            }
        }
        DisposableEffect(
            AndroidView(factory = {
                val imageView = ImageView(it)
                imageView.setImageDrawable(webpDrawable)
                webpDrawable.registerAnimationCallback(callback)
                webpDrawable.start()
                imageView
            }, update = {
            }, modifier = Modifier
                .layoutId("center_anim")
                .size(200.dp)
                .graphicsLayer(
                    scaleX = if (animationState == AnimationState.ENTER) 1f else scale,
                    scaleY = if (animationState == AnimationState.ENTER) 1f else scale,
                    transformOrigin = TransformOrigin(
                        0f,
                        0f
                    )
                )
            )
        ) {
            onDispose {
                webpDrawable.unregisterAnimationCallback(callback)
            }
        }
    }, measurePolicy = { measurables, constraints ->
        val placeable =
            measurables.firstOrNull { it.layoutId == "center_anim" }?.measure(constraints)
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable?.let { placeable ->
                offsetSrc = IntOffset(
                    x = (constraints.maxWidth / 2) - (placeable.width / 2),
                    y = (constraints.maxHeight / 2) - (placeable.height / 2)
                )
                scaleDest = centerGift.destSize.toFloat() / placeable.width.toFloat()
                placeable.place(if (animationState == AnimationState.SETTLE) animateOffset else offsetSrc)
            }
        }
    })
}

@ExperimentalAnimationApi
@Composable
fun StickyCommentsUI(
    modifier: Modifier = Modifier,
    uiState: MainState,
    showCenterAnim: (CenterGift) -> Unit,
    onGiftClear: (GiftMessage, Slot) -> Unit
) {
    Column(modifier = modifier.padding(16.dp)) {

        Log.d("Ruben", "ui state $uiState")

        uiState.slot1?.let {
            Log.d("Ruben", "show in slot1 ${it.id}")
            StickyCommentItem(
                giftMessage = it,
                showCenterAnim = showCenterAnim,
                onGiftClear = { gift -> onGiftClear.invoke(gift, true) })
        }

        Spacer(modifier = modifier.padding(vertical = 16.dp))

        uiState.slot2?.let {
            Log.d("Ruben", "show in slot2 ${it.id} ${it.resourceName}")
            StickyCommentItem(
                giftMessage = it,
                showCenterAnim = showCenterAnim,
                onGiftClear = { gift -> onGiftClear.invoke(gift, true) })
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun Slot1(giftMessage: GiftMessage, onGiftClear: (GiftMessage, Slot) -> Unit) {
    GiftItem(giftMessage = giftMessage, onGiftClear = { gift -> onGiftClear.invoke(gift, Slot.SLOT_1) })
}

@ExperimentalAnimationApi
@Composable
fun Slot2(giftMessage: GiftMessage, onGiftClear: (GiftMessage, Slot) -> Unit) {
    GiftItem(giftMessage = giftMessage, onGiftClear = { gift -> onGiftClear.invoke(gift, Slot.SLOT_2) })
}

@ExperimentalAnimationApi
@Composable
fun Slab5Gift(modifier: Modifier = Modifier, giftMessage: GiftMessage, onGiftClear: (GiftMessage) -> Unit, onSpecialSlotClear: () -> Unit) {
    GiftItem5(
        modifier = modifier,
        giftMessage = giftMessage,
        onGiftClear = { gift -> onGiftClear.invoke(gift) },
        onSpecialSlotClear = onSpecialSlotClear
    )
}

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
@Composable
fun GiftItem(
    modifier: Modifier = Modifier,
    giftMessage: GiftMessage,
    onGiftClear: (GiftMessage) -> Unit
) {
    //var isVisible by remember { mutableStateOf(true) }

    //Log.d("Ruben", "giftItem $isVisible")

    LaunchedEffect(key1 = giftMessage) {
        Log.d("Ruben", "launched effect begin")
        delay(giftMessage.totalDuration)
        onGiftClear.invoke(giftMessage)
        Log.d("Ruben", "launched effect clear")
        //isVisible = false
    }

    Box(
        modifier = modifier
            .layoutId("gift_content")
            .background(shape = RoundedCornerShape(10.dp), color = Color.Red)
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = giftMessage.message,
            fontWeight = FontWeight.W700,
            color = Color.White
        )
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