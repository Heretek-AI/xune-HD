package com.heretek.xunehd.design.components

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.Selawik
import com.heretek.xunehd.design.XuneMotion
import com.heretek.xunehd.design.XuneTokens
import java.util.Date
import kotlinx.coroutines.launch

/**
 * The wake shade (canon §5): a software shade covering the UI after the app
 * leaves the foreground; slide it up to reveal the home screen, exactly as
 * the device did over the user's wallpaper.
 */
@Composable
fun LockShade(
    visible: Boolean,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalXuneColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(visible) {
        if (visible) {
            offset.snapTo(0f)
            while (visible) {
                now = System.currentTimeMillis()
                kotlinx.coroutines.delay(15_000)
            }
        }
    }

    val unlockPx = with(LocalDensity.current) { 96.dp.toPx() }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(XuneMotion.instant()),
        exit = fadeOut(XuneMotion.pivot()),
        modifier = modifier,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .graphicsLayer { translationY = -offset.value }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, amount ->
                            change.consume()
                            scope.launch { offset.snapTo((offset.value - amount).coerceIn(0f, unlockPx * 1.4f)) }
                        },
                        onDragEnd = {
                            if (offset.value > unlockPx) {
                                scope.launch {
                                    offset.animateTo(unlockPx * 1.6f, XuneMotion.pivot())
                                }
                                onUnlock()
                            } else {
                                scope.launch { offset.animateTo(0f, XuneMotion.pivot()) }
                            }
                        },
                    )
                },
        ) {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = XuneTokens.EDGE.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BasicText(
                    text = DateFormat.getTimeFormat(context).format(Date(now)),
                    style = TextStyle(
                        fontFamily = Selawik,
                        fontWeight = FontWeight.Light,
                        fontSize = XuneTokens.TYPE_HEADER_CROPPED.sp,
                        color = colors.textPrimary,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                BasicText(
                    text = DateFormat.getDateFormat(context).format(Date(now)),
                    style = TextStyle(
                        fontFamily = Selawik,
                        fontSize = XuneTokens.TYPE_LIST.sp,
                        color = colors.textSecondary,
                    ),
                )
            }
            BasicText(
                text = "slide up to unlock",
                style = TextStyle(
                    fontFamily = Selawik,
                    fontSize = XuneTokens.TYPE_NOW_META.sp,
                    color = colors.textSecondary,
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            )
            BasicText(
                text = "xune hd",
                style = TextStyle(
                    fontFamily = Selawik,
                    fontSize = XuneTokens.TYPE_CROSSBAR.sp,
                    color = colors.textWatermark,
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = XuneTokens.EDGE.dp, bottom = XuneTokens.EDGE.dp),
            )
        }
    }
}
