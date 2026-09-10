package com.heretek.xunehd.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.Selawik
import com.heretek.xunehd.design.XuneTokens

/**
 * Text that deliberately overflows and is cropped at the right edge — the
 * signature Zune typography gesture ("marketplace" cut off mid-letter).
 */
@Composable
fun EdgeCropText(
    text: String,
    fontSize: Dp,
    modifier: Modifier = Modifier,
    color: Color = LocalXuneColors.current.textPrimary,
    fontWeight: FontWeight = FontWeight.Light,
    alpha: Float = 1f,
) {
    Box(modifier = modifier.clipToBounds()) {
        BasicText(
            text = text,
            style = TextStyle(
                fontFamily = Selawik,
                fontWeight = fontWeight,
                fontSize = fontSize.value.sp,
                color = color.copy(alpha = alpha),
                letterSpacing = XuneTokens.LETTER_SPACING_EDGE.sp,
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}

/**
 * The oversized cropped screen header. Only the bottom sliver of the text is
 * visible (the bottom of "SETT" on Settings) — tapping it navigates back.
 * This is the Zune HD's substitute for a hardware back button.
 */
@Composable
fun CroppedHeader(
    text: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    visibleHeight: Dp = XuneTokens.TYPE_HEADER_CROP_VISIBLE.dp,
    fontSize: Dp = XuneTokens.TYPE_HEADER_CROPPED.dp,
) {
    val colors = LocalXuneColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(visibleHeight)
            .clipToBounds()
            .background(Color.Transparent)
            .clickable(onClick = onBack),
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                fontFamily = Selawik,
                fontWeight = FontWeight.Light,
                fontSize = fontSize.value.sp,
                color = colors.textPrimary.copy(alpha = 0.85f),
                letterSpacing = XuneTokens.LETTER_SPACING_HEADER.sp,
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.offset(y = (visibleHeight - fontSize).coerceAtLeast(0.dp)),
        )
    }
}

/**
 * Home-menu item: giant lowercase text, cropped at the right edge.
 */
@Composable
fun HomeMenuItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    EdgeCropText(
        text = label,
        fontSize = XuneTokens.TYPE_MENU_ITEM.dp,
        alpha = alpha,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = XuneTokens.EDGE.dp, top = 10.dp, bottom = 10.dp),
    )
}
