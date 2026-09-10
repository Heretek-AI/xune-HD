package com.heretek.xunehd.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.Selawik
import com.heretek.xunehd.design.XuneTokens

/**
 * The Zune crossbar: lowercase pivot labels across the top of the screen.
 * Active pivot is white; the rest dim to 40%. Tapping selects; horizontal
 * content drags page between pivots (CrossbarScreen wires a pager to [selected]).
 */
@Composable
fun CrossbarBar(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalXuneColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(XuneTokens.CROSSBAR_HEIGHT.dp)
            .horizontalScroll(rememberScrollState())
            .padding(start = XuneTokens.CROSSBAR_LEAD.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEachIndexed { index, label ->
            val target = if (index == selected) colors.textPrimary else colors.textInactive
            val color by animateColorAsState(target, label = "crossbar")
            BasicText(
                text = label,
                style = TextStyle(
                    fontFamily = Selawik,
                    fontWeight = FontWeight.Light,
                    fontSize = XuneTokens.TYPE_CROSSBAR.sp,
                    color = color,
                    letterSpacing = XuneTokens.LETTER_SPACING_CROSSBAR.sp,
                ),
                modifier = Modifier
                    .clickable(onClick = { onSelect(index) })
                    .padding(end = 22.dp),
                maxLines = 1,
            )
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(colors.border),
    )
}
