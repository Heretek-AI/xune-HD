package com.heretek.xunehd.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heretek.xunehd.R
import com.heretek.xunehd.data.repo.XuneSettings
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.XuneMotion
import com.heretek.xunehd.design.XuneTokens
import com.heretek.xunehd.design.components.AlbumArt
import com.heretek.xunehd.design.components.DeviceCanvas
import com.heretek.xunehd.design.components.EdgeCropText
import com.heretek.xunehd.ui.components.MenuController
import com.heretek.xunehd.ui.nav.XuneDestination
import com.heretek.xunehd.ui.screens.AlbumDetailScreen
import com.heretek.xunehd.ui.screens.ArtistDetailScreen
import com.heretek.xunehd.ui.screens.GenreScreen
import com.heretek.xunehd.ui.screens.HomePages
import com.heretek.xunehd.ui.screens.MusicScreen
import com.heretek.xunehd.ui.screens.NowPlayingScreen
import com.heretek.xunehd.ui.screens.PlaylistDetailScreen
import com.heretek.xunehd.ui.screens.SettingsScreen

@Composable
fun XuneRoot() {
    val graph = LocalXuneGraph.current
    val settings by graph.settingsFlow.collectAsState(initial = XuneSettings())
    val menus = remember { MenuController() }
    val activity = androidx.activity.compose.LocalActivity.current

    com.heretek.xunehd.design.XuneTheme(accent = settings.accent) {
        androidx.compose.runtime.CompositionLocalProvider(
            com.heretek.xunehd.ui.components.LocalContextMenu provides menus,
        ) {
            DeviceCanvas(deviceMode = settings.deviceMode) { canvasWidth, canvasHeight ->
                Box(Modifier.fillMaxSize()) {
                    NavHost(canvasWidth, canvasHeight)
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                    ) {
                        MiniPlayer(canvasWidth)
                    }
                    com.heretek.xunehd.ui.components.ContextMenuOverlay(menus)
                }
            }
        }
    }

    BackHandler {
        val nav = graph.nav
        if (nav.current == XuneDestination.Home) {
            // Let the system move the task to background.
            activity?.moveTaskToBack(false)
        } else {
            nav.pop()
        }
    }
}

@Composable
private fun NavHost(canvasWidth: Dp, canvasHeight: Dp) {
    val graph = LocalXuneGraph.current
    val nav = graph.nav
    val target = nav.current to nav.depth

    AnimatedContent(
        targetState = target,
        transitionSpec = {
            val pushing = targetState.second > initialState.second
            if (pushing) {
                (slideInHorizontally(XuneMotion.pivot()) { it / 2 } + fadeIn(XuneMotion.pivot())) togetherWith
                    (slideOutHorizontally(XuneMotion.pivot()) { -it / 8 } + fadeOut(XuneMotion.pivot()))
            } else {
                (slideInHorizontally(XuneMotion.pivot()) { -it / 8 } + fadeIn(XuneMotion.pivot())) togetherWith
                    (slideOutHorizontally(XuneMotion.pivot()) { it / 2 } + fadeOut(XuneMotion.pivot()))
            }
        },
        label = "nav",
    ) { (destination, _) ->
        when (destination) {
            XuneDestination.Home, XuneDestination.Quickplay -> HomePages(canvasWidth)
            XuneDestination.Music -> MusicScreen(canvasWidth)
            is XuneDestination.Album -> AlbumDetailScreen(destination.albumId, canvasWidth)
            is XuneDestination.Artist -> ArtistDetailScreen(destination.artistId, canvasWidth)
            is XuneDestination.Genre -> GenreScreen(destination.genre, canvasWidth)
            is XuneDestination.PlaylistDetail -> PlaylistDetailScreen(destination.playlistId, canvasWidth)
            XuneDestination.NowPlaying -> NowPlayingScreen(canvasWidth)
            XuneDestination.Settings -> SettingsScreen(canvasWidth)
        }
    }
}

@Composable
private fun MiniPlayer(canvasWidth: Dp) {
    val graph = LocalXuneGraph.current
    val nav = graph.nav
    val controller = graph.controller
    val nowPlaying by controller.nowPlaying.collectAsState()
    val isPlaying by controller.isPlaying.collectAsState()
    val colors = LocalXuneColors.current

    if (nav.current == XuneDestination.NowPlaying || nowPlaying == null) return

    Box(
        Modifier
            .fillMaxWidth()
            .height(XuneTokens.MINI_PLAYER_HEIGHT.dp)
            .background(colors.elevated.copy(alpha = 0.95f)),
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.border),
        )
        Row(
            Modifier
                .fillMaxSize()
                .clickable { nav.push(XuneDestination.NowPlaying) }
                .padding(horizontal = XuneTokens.EDGE.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumArt(
                model = nowPlaying?.albumArtUri,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            EdgeCropText(
                text = "${nowPlaying?.title ?: ""} — ${nowPlaying?.artist ?: ""}",
                fontSize = XuneTokens.TYPE_LIST_SECONDARY.dp,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp),
                color = colors.textSecondary,
            )
            IconButton(
                onClick = { controller.toggle() },
                modifier = Modifier.width(32.dp),
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = if (isPlaying) "pause" else "play",
                    tint = colors.textPrimary,
                )
            }
        }
    }
}
