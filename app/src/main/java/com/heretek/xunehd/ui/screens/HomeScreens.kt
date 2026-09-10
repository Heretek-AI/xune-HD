package com.heretek.xunehd.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heretek.xunehd.XuneGraph
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.XuneTokens
import com.heretek.xunehd.design.components.AlbumArt
import com.heretek.xunehd.design.components.EdgeCropText
import com.heretek.xunehd.design.components.HomeMenuItem
import com.heretek.xunehd.design.components.StaggerEntrance
import com.heretek.xunehd.data.model.PinKind
import com.heretek.xunehd.data.repo.QuickplayCard
import com.heretek.xunehd.ui.LocalXuneGraph
import com.heretek.xunehd.ui.components.LocalContextMenu
import com.heretek.xunehd.ui.components.SectionLabel
import com.heretek.xunehd.ui.nav.XuneDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The Zune HD home: two pages side by side. The home text menu sits on the
 * right; Quickplay is parked to its left ("left and to the rear, in a bit of
 * visual 3D trickery" — canon §3.2): its content trails the page frame at
 * 0.6x and gains slight scale as it settles forward.
 */
@Composable
fun HomePages(canvasWidth: Dp) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        val behind = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
            .coerceIn(0f, 1f)
        Box(
            Modifier
                .fillMaxSize()
                .clipToBounds()
                .graphicsLayer {
                    if (page == 0) {
                        translationX = behind * 0.4f * size.width
                        val depth = 0.92f + 0.08f * (1f - behind)
                        scaleX = depth
                        scaleY = depth
                        alpha = 0.8f + 0.2f * (1f - behind)
                    }
                },
        ) {
            if (page == 0) QuickplayScreen(canvasWidth) else HomeMenuScreen(canvasWidth)
        }
    }
}

@Composable
fun HomeMenuScreen(canvasWidth: Dp) {
    val graph = LocalXuneGraph.current
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(48.dp))
        StaggerEntrance(index = 0) {
            HomeMenuItem(label = "music", onClick = { graph.nav.push(XuneDestination.Music) })
        }
        StaggerEntrance(index = 1) {
            HomeMenuItem(label = "videos", onClick = { graph.nav.push(XuneDestination.Videos) })
        }
        StaggerEntrance(index = 2) {
            HomeMenuItem(label = "pictures", onClick = { graph.nav.push(XuneDestination.Pictures) })
        }
        StaggerEntrance(index = 3) {
            HomeMenuItem(label = "radio", onClick = { graph.nav.push(XuneDestination.Radio) })
        }
        StaggerEntrance(index = 4) {
            HomeMenuItem(label = "marketplace", onClick = { graph.nav.push(XuneDestination.Marketplace) })
        }
        StaggerEntrance(index = 5) {
            HomeMenuItem(label = "social", onClick = { graph.nav.push(XuneDestination.Social) })
        }
        StaggerEntrance(index = 6) {
            HomeMenuItem(label = "podcasts", onClick = { graph.nav.push(XuneDestination.Podcasts) })
        }
        StaggerEntrance(index = 7) {
            HomeMenuItem(label = "internet", onClick = { graph.nav.push(XuneDestination.Internet) })
        }
        StaggerEntrance(index = 8) {
            HomeMenuItem(label = "settings", onClick = { graph.nav.push(XuneDestination.Settings) })
        }
        Spacer(Modifier.weight(1f))
        EdgeCropText(
            text = "xune hd",
            fontSize = XuneTokens.TYPE_CROSSBAR.dp,
            alpha = 0.08f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = XuneTokens.EDGE.dp, bottom = XuneTokens.EDGE.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickplayScreen(canvasWidth: Dp) {
    val graph = LocalXuneGraph.current
    val controller = graph.controller
    val scope = rememberCoroutineScope()
    val menus = LocalContextMenu.current

    val nowPlaying by controller.nowPlaying.collectAsState()
    val pins by graph.quickplay.pins().collectAsState(initial = emptyList())
    val history by graph.quickplay.history(12).collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(XuneTokens.EDGE.dp))
        SectionLabel("now playing")

        val track = nowPlaying
        Row(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .combinedClickable(
                    onClick = { if (track != null) graph.nav.push(XuneDestination.NowPlaying) },
                    onLongClick = {},
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (track != null) {
                AlbumArt(
                    model = track.albumArtUri,
                    contentDescription = track.album,
                    modifier = Modifier
                        .padding(start = XuneTokens.EDGE.dp)
                        .size(64.dp),
                )
                Column(Modifier.padding(start = 12.dp)) {
                    EdgeCropText(text = track.title, fontSize = XuneTokens.TYPE_NOW_META.dp)
                    EdgeCropText(
                        text = track.artist,
                        fontSize = XuneTokens.TYPE_LIST.dp,
                        color = LocalXuneColors.current.textSecondary,
                    )
                }
            } else {
                EdgeCropText(
                    text = "nothing playing",
                    fontSize = XuneTokens.TYPE_NOW_META.dp,
                    alpha = 0.4f,
                    modifier = Modifier.padding(start = XuneTokens.EDGE.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        SectionLabel("pins")
        QuickplayRow(graph, scope, cards = pins)

        Spacer(Modifier.height(8.dp))
        SectionLabel("history")
        QuickplayRow(graph, scope, cards = history)

        Spacer(Modifier.height(8.dp))
        SectionLabel("new")
        NewRow()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickplayRow(graph: XuneGraph, scope: CoroutineScope, cards: List<QuickplayCard>) {
    val menus = LocalContextMenu.current
    if (cards.isEmpty()) {
        EdgeCropText(
            text = "empty",
            fontSize = XuneTokens.TYPE_LIST.dp,
            alpha = 0.4f,
            modifier = Modifier.padding(horizontal = XuneTokens.EDGE.dp, vertical = 4.dp),
        )
        return
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = XuneTokens.EDGE.dp),
        horizontalArrangement = Arrangement.spacedBy(XuneTokens.GRID_GUTTER.dp),
    ) {
        items(cards, key = { "${it.kind}:${it.refId}:${it.label}" }) { card ->
            Column(
                Modifier
                    .width(72.dp)
                    .combinedClickable(
                        onClick = { openCard(graph, scope, card) },
                        onLongClick = {
                            menus.show(
                                title = card.label,
                                actions = listOf(
                                    com.heretek.xunehd.ui.components.MenuAction("unpin") {
                                        scope.launch { graph.quickplay.unpin(card.kind, card.refId) }
                                    },
                                ),
                            )
                        },
                    ),
            ) {
                AlbumArt(
                    model = card.artAlbumId.takeIf { it > 0 }
                        ?.let { android.net.Uri.parse("content://media/external/audio/albumart/$it") },
                    contentDescription = card.label,
                    modifier = Modifier.size(72.dp),
                )
                EdgeCropText(
                    text = card.label,
                    fontSize = XuneTokens.TYPE_CAPTION.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NewRow() {
    val graph = LocalXuneGraph.current
    val scope = rememberCoroutineScope()
    val albums by graph.library.albums().collectAsState(initial = emptyList())
    val recent = albums.sortedByDescending { it.dateAdded }.take(12)
    LazyRow(
        contentPadding = PaddingValues(horizontal = XuneTokens.EDGE.dp),
        horizontalArrangement = Arrangement.spacedBy(XuneTokens.GRID_GUTTER.dp),
    ) {
        items(recent, key = { it.albumId }) { album ->
            Column(
                Modifier
                    .width(72.dp)
                    .combinedClickable(
                        onClick = { graph.nav.push(XuneDestination.Album(album.albumId)) },
                        onLongClick = {},
                    ),
            ) {
                AlbumArt(
                    model = album.albumArtUri,
                    contentDescription = album.title,
                    modifier = Modifier.size(72.dp),
                )
                EdgeCropText(
                    text = album.title,
                    fontSize = XuneTokens.TYPE_CAPTION.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun openCard(graph: XuneGraph, scope: CoroutineScope, card: QuickplayCard) {
    when (card.kind) {
        PinKind.TRACK -> scope.launch {
            graph.library.track(card.refId)?.let { graph.controller.play(listOf(it)) }
        }
        PinKind.ALBUM -> graph.nav.push(XuneDestination.Album(card.refId))
        PinKind.ARTIST -> graph.nav.push(XuneDestination.Artist(card.refId))
        PinKind.PLAYLIST -> graph.nav.push(XuneDestination.PlaylistDetail(card.refId))
        PinKind.PICTURE -> {
            val uri = card.subLabel.takeIf { it.startsWith("content://") || it.startsWith("file://") }
            if (uri != null) graph.nav.push(XuneDestination.PictureDetail(uri))
        }
        PinKind.RADIO -> graph.nav.push(XuneDestination.Radio)
    }
}
