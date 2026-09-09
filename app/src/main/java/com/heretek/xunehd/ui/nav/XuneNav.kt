package com.heretek.xunehd.ui.nav

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Lightweight hierarchical navigation. The Zune HD has no hardware back:
 * going back is "tap the cut-off header", which calls [pop]. Slides are
 * horizontal with the Zune deceleration curve.
 */
sealed interface XuneDestination {
    data object Home : XuneDestination
    data object Quickplay : XuneDestination
    data object Music : XuneDestination
    data class Album(val albumId: Long) : XuneDestination
    data class Artist(val artistId: Long) : XuneDestination
    data class Genre(val genre: String) : XuneDestination
    data class PlaylistDetail(val playlistId: Long) : XuneDestination
    data object NowPlaying : XuneDestination
    data object Settings : XuneDestination
}

class XuneNav {
    private val stack = mutableStateListOf<XuneDestination>(XuneDestination.Home)

    val current: XuneDestination
        get() = stack.lastOrNull() ?: XuneDestination.Home

    val depth: Int
        get() = stack.size

    fun push(destination: XuneDestination) {
        stack.add(destination)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun toHome() {
        stack.clear()
        stack.add(XuneDestination.Home)
    }

    fun replaceTop(destination: XuneDestination) {
        if (stack.isNotEmpty()) stack[stack.lastIndex] = destination else stack.add(destination)
    }
}
