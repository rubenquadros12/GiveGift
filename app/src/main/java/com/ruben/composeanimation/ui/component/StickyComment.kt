package com.ruben.composeanimation.ui.component

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.penfeizhou.animation.loader.AssetStreamLoader
import com.github.penfeizhou.animation.webp.WebPDrawable
import com.ruben.composeanimation.data.GiftMessage
import com.ruben.composeanimation.ui.Slab
import kotlinx.coroutines.delay

@Composable
fun StickyCommentItem(
    giftMessage: GiftMessage,
    showCenterAnim: (CenterGift) -> Unit,
    onGiftClear: (GiftMessage) -> Unit = {}
) {

    LaunchedEffect(key1 = giftMessage) {
        Log.d("Ruben", "launched effect begin")
        delay(giftMessage.totalDuration)
        onGiftClear.invoke(giftMessage)
        Log.d("Ruben", "launched effect clear")
        //isVisible = false
    }

    val assetLoader = AssetStreamLoader(
        LocalContext.current,
        giftMessage.resourceName
    )
    val webpDrawable = WebPDrawable(assetLoader)

    Row(
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(50.dp))
            .background(brush = getBackgroundGradient())
            .border(width = 1.dp, brush = getBorderGradient(), shape = RoundedCornerShape(50.dp))
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = ColorPainter(Color.Green),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(top = 2.dp)
                .border(1.dp, Color.White.copy(alpha = 0.1f), shape = CircleShape)
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
        ) {
            Text(
                "@${giftMessage.userId}",
                color = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = giftMessage.message,
                color = Color.White,
                fontSize = 14.sp,
            )
        }
        AndroidView(factory = {
            if (Slab.isHigherSlab(giftMessage.slab)) {
                View(it)
            } else {
                val imageView = ImageView(it)
                imageView.setImageDrawable(webpDrawable)
                webpDrawable.start()
                imageView
            }
        }, update = {

        }, modifier = Modifier
            .padding(2.dp)
            .width(32.dp)
            .height(32.dp)
            .onGloballyPositioned {
                if (Slab.isHigherSlab(giftMessage.slab)) {
                    val offset = IntOffset(
                        x = it.positionInRoot().x.toInt(),
                        y = it.positionInRoot().y.toInt()
                    )
                    showCenterAnim.invoke(
                        CenterGift(
                            giftMessage.resourceName,
                            offsetDest = offset,
                            destSize = it.size.width
                        )
                    )
                }
            }
        )
    }
}

private fun getBackgroundGradient(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            Color(0x660500FF),
            Color(0x66C400C8)
        )
    )
}

private fun getBorderGradient(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1E3675),
            Color(0xFFC9CBFF),
            Color(0xFF20D2F9),
            Color(0xFF0772F0),
            Color(0xFFC3C1FF),
            Color(0xFFB000EE)
        )
    )
}

//Preview Section
//@Preview(
//    name = "My Preview",
//    showBackground = true,
//    backgroundColor = 0x989a82
//)
//@Composable
//fun PreviewStickyCommentItem() {
//    StickyCommentItem(GiftMessage())
//}