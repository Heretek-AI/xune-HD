package com.heretek.xunehd.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.Selawik
import com.heretek.xunehd.design.XuneMotion
import com.heretek.xunehd.design.XuneTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Kinetic list with the Zune alphabet rail: faint letters alongside the list;
 * dragging or tapping the rail jumps the list and shows the big letter
 * overlay — the Zune HD "tap any of the letters ... and that pops up the
 * full alphabet" behavior, adapted to a direct-manipulation rail.
 */
@Composable
fun <T> KineticList(
    items: List<T>,
    key: (T) -> Any,
    letter: (T) -> Char?,
    rowContent: @Composable (item: T, index: Int) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    bottomPadding: Dp = 0.dp,
) {
    val colors = LocalXuneColors.current
    val scope = rememberCoroutineScope()

    val letters = remember(items) {
        items.mapNotNull { letter(it) }.distinct().sorted()
    }

    var draggingLetter by remember { mutableStateOf<Char?>(null) }
    val overlayAlpha = remember { Animatable(0f) }

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomPadding),
        ) {
            itemsIndexed(
                items,
                key = { _, item -> key(item) },
            ) { index, item ->
                rowContent(item, index)
            }
        }

        if (letters.isNotEmpty()) {
            AlphabetRail(
                letters = letters,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(18.dp),
                onLetterFocus = { letterChar ->
                    draggingLetter = letterChar
                    scope.launch { overlayAlpha.snapTo(1f) }
                    val index = items.indexOfFirst { letter(it) == letterChar }
                    if (index >= 0) scope.launch { listState.scrollToItem(index) }
                },
                onDragEnd = {
                    scope.launch {
                        delay(350)
                        overlayAlpha.animateTo(0f, XuneMotion.pivot())
                    }
                },
            )

            AnimatedVisibility(
                visible = overlayAlpha.value > 0.01f,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                BasicText(
                    text = draggingLetter?.toString() ?: "",
                    style = TextStyle(
                        fontFamily = Selawik,
                        fontWeight = FontWeight.Light,
                        fontSize = 120.sp,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.graphicsLayer { alpha = overlayAlpha.value },
                )
            }
        }
    }
}

@Composable
private fun AlphabetRail(
    letters: List<Char>,
    modifier: Modifier = Modifier,
    onLetterFocus: (Char) -> Unit,
    onDragEnd: () -> Unit,
) {
    val colors = LocalXuneColors.current
    Column(modifier = modifier) {
        letters.forEach { letterChar ->
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .clickable { onLetterFocus(letterChar) }
                    .draggable(
                        state = rememberDraggableState { },
                        orientation = Orientation.Vertical,
                        onDragStarted = { onLetterFocus(letterChar) },
                        onDragStopped = { onDragEnd() },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = letterChar.toString(),
                    style = TextStyle(
                        fontFamily = Selawik,
                        fontSize = XuneTokens.TYPE_ALPHABET.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

/** Uppercase first letter used by alphabet grouping. */
fun firstLetterOf(text: String): Char =
    text.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar() ?: '#'
