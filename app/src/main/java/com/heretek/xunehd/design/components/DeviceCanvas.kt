package com.heretek.xunehd.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.XuneTokens
import kotlin.math.min

/**
 * The 480x272 Zune HD canvas.
 *
 * In device mode the entire UI is laid out at the authentic design size and
 * scaled to fit (letterboxed on matte black) — hold the phone in landscape
 * for the full-device feel. In adaptive mode the same components reflow to
 * the natural screen size.
 */
@Composable
fun DeviceCanvas(
    deviceMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (canvasWidth: Dp, canvasHeight: Dp) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LocalXuneColors.current.background),
    ) {
        if (!deviceMode) {
            content(maxWidth, maxHeight)
        } else {
            val scale = min(
                maxWidth.value / XuneTokens.CANVAS_WIDTH,
                maxHeight.value / XuneTokens.CANVAS_HEIGHT,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(XuneTokens.CANVAS_WIDTH.dp, XuneTokens.CANVAS_HEIGHT.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clipToBounds(),
            ) {
                content(XuneTokens.CANVAS_WIDTH.dp, XuneTokens.CANVAS_HEIGHT.dp)
            }
        }
    }
}
