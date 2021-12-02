package com.ruben.composeanimation.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.github.penfeizhou.animation.loader.ResourceStreamLoader
import com.github.penfeizhou.animation.webp.WebPDrawable
import com.ruben.composeanimation.R
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.ui.component.StickyComments
import com.ruben.composeanimation.ui.theme.ComposeAnimationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val mainViewModel2: MainViewModel2 by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeAnimationTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                   //AnimationContent()
                    MainContent(mainViewModel, mainViewModel2)
                   //StickyCommentContent(mainViewModel, mainViewModel2)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mainViewModel2.onStart()
    }

    override fun onStop() {
        super.onStop()
        mainViewModel2.onStop()
    }
}

@Composable
fun StickyCommentContent(mainViewModel: MainViewModel, mainViewModel2: MainViewModel2) {
    fun onSlabClick(slab: Slab) {
        var count = 0
        count = count.inc()
        mainViewModel.addNewGift(count = count, slab = slab, message = slab.toString())
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val uiStateFlow = mainViewModel2.uiState
    val uiStateFlowLifecycleAware = remember(uiStateFlow, lifecycleOwner) {
        uiStateFlow.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
    }
    val uiState by uiStateFlowLifecycleAware.collectAsState(initial = mainViewModel2.createInitialState())

    Box(modifier = Modifier.fillMaxSize()) {

        Button(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopCenter),
            onClick = { mainViewModel.clearDB() }
        ) {
            Text(text = "Clear DB")
        }

        Box( modifier = Modifier
            .padding(16.dp)
            .align(Alignment.CenterStart)
        ) {
            StickyComments(mainViewModel2)
        }

            LazyRow(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomCenter),
            ) {
                items(items = uiState.slabList, key = { item -> item.duration }) { item ->
                    SlabContent(item, onSlabClick = { slab -> onSlabClick(slab) })
                }
            }
        }
    }

@Composable
fun MainContent(mainViewModel: MainViewModel, mainViewModel2: MainViewModel2) {
    var count by remember { mutableStateOf(0) }
    fun onSlabClick(slab: Slab) {
        count = count.inc()
        mainViewModel.addNewGift(count = count, slab = slab, message = slab.toString())
    }

    fun onGiftCleared(giftMessage: GiftMessage) {
        mainViewModel2.clearGift(giftMessage)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val uiStateFlow = mainViewModel2.uiState
    val uiStateFlowLifecycleAware = remember(uiStateFlow, lifecycleOwner) {
        uiStateFlow.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
    }
    val uiState by uiStateFlowLifecycleAware.collectAsState(initial = mainViewModel2.createInitialState())

    val giftList = uiState.giftList

    Box(modifier = Modifier.fillMaxSize()) {

        Button(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopCenter),
            onClick = { mainViewModel.clearDB() }
        ) {
            Text(text = "Clear DB")
        }

        if (giftList.any { it.slab == Slab.SLAB_5.toString() }) {
            Slab5Anim {
                GiftMessageContent(
                    giftMessage = giftList[0],
                    onGiftClear = { gift -> onGiftCleared(gift) })
            }
//            Slab5Content(
//                giftMessage = giftList[0],
//                onGiftClear = { onGiftCleared() },
//                content = { Gift(giftMessage = giftList[0]) }
//            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterStart)
            ) {
                items(items = uiState.giftList, key = { item -> item.id }) { item ->
                    GiftMessageContent(
                        giftMessage = item,
                        onGiftClear = { gift -> onGiftCleared(gift) }
                    )
                }
            }
        }

        LazyRow(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomCenter),
        ) {
            items(items = uiState.slabList, key = { item -> item.duration }) { item ->
                SlabContent(item, onSlabClick = { slab -> onSlabClick(slab) })
            }
        }
    }

}

@Composable
fun Slab5Anim(
    content: @Composable () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var isStart by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        delay(1000)
        isStart = false
    }

    val offsetAnimation: Dp by animateDpAsState(
        targetValue = if (isStart) (screenHeight/2).dp else (screenHeight/2 - 100).dp,
        animationSpec = tween(durationMillis = 2000, easing = LinearEasing),
    )

    Box(modifier = Modifier.absoluteOffset(x = (screenWidth/2).dp, y = offsetAnimation)) {
        content()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Slab5Content(
    giftMessage: GiftMessage,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    onGiftClear: (GiftMessage) -> Unit
) {

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp

    var isVisible by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(key1 = true) {
        delay(giftMessage.totalDuration)
        onGiftClear.invoke(giftMessage)
        isVisible = false
    }

    AnimatedVisibility(
        visible =isVisible
    ) {
        Layout(
            modifier = modifier,
            content = content,
            measurePolicy = { measurables, constraints ->
                val slab5Gift = measurables.first { it.layoutId == "gift_content" }.measure(constraints)
                placeSlab5Gift(slab5Gift, constraints.maxWidth, constraints.maxHeight)
            }
        )
    }
}

private fun MeasureScope.placeSlab5Gift(
    gift: Placeable,
    width: Int,
    height: Int
): MeasureResult {
    return layout(width, height) {
        gift.place(x = (width/2), y = (height)/2)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GiftMessageContent(giftMessage: GiftMessage, onGiftClear: (GiftMessage) -> Unit) {
    var isVisible by remember { mutableStateOf(true) }
    val density = LocalDensity.current

    LaunchedEffect(key1 = true) {
        delay(giftMessage.totalDuration)
        onGiftClear.invoke(giftMessage)
        isVisible = false
    }

    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier.layoutId("gift_content_1"),
        enter = expandHorizontally(),
        exit = slideOutHorizontally(targetOffsetX = { with(density) { 40.dp.roundToPx() } })
    ) {
        Box(modifier = Modifier.background(shape = RoundedCornerShape(10.dp), color = Color.Red)) {
            Text(modifier = Modifier.padding(8.dp), text = giftMessage.message, fontWeight = FontWeight.W700, color = Color.White)
        }
    }
}

@Composable
fun Gift(giftMessage: GiftMessage) {
    Box(modifier = Modifier
        .size(100.dp)
        .layoutId("gift_content")
        .background(shape = RoundedCornerShape(10.dp), color = Color.Red)) {
        Text(modifier = Modifier.padding(8.dp), text = giftMessage.message, fontWeight = FontWeight.W700, color = Color.White)
    }
}

@Composable
fun SlabContent(slab: Slab, onSlabClick: (Slab) -> Unit) {
    Button(
        modifier = Modifier.padding(4.dp),
        onClick = {
        onSlabClick.invoke(slab)
    }) {
        Text(text = slab.toString())
    }
}

@Composable
fun AnimationContent() {
    val context = LocalContext.current
    val resourceLoader = ResourceStreamLoader(context, R.raw.slab_5_mia_without_confetti)
    val webpDrawable = WebPDrawable(resourceLoader)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .align(Alignment.TopStart),
            painter = painterResource(id = R.drawable.ic_android_black_24dp),
            contentDescription = ""
        )

        DisposableEffect(
            AndroidView(
                factory = {
                    val imageView = ImageView(it)
                    webpDrawable.setLoopLimit(1)
                    webpDrawable.registerAnimationCallback(animationCallback)
                    imageView
                },
                update = {
                    it.setImageDrawable(webpDrawable)
                    if (webpDrawable.isRunning.not()) {
                        webpDrawable.start()
                    }
                },
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.Center)
            )
        ) {
            onDispose {
                webpDrawable.unregisterAnimationCallback(animationCallback)
            }
        }
    }
}

private val animationCallback: Animatable2Compat.AnimationCallback = object : Animatable2Compat.AnimationCallback() {
    override fun onAnimationStart(drawable: Drawable?) {
        super.onAnimationStart(drawable)
    }

    override fun onAnimationEnd(drawable: Drawable?) {
        super.onAnimationEnd(drawable)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeAnimationTheme {
        AnimationContent()
    }
}