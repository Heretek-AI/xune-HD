package com.heretek.xunehd.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heretek.xunehd.data.official.OfficialApp
import com.heretek.xunehd.data.official.OfficialCatalog
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.XuneTokens
import com.heretek.xunehd.design.components.AlbumArt
import com.heretek.xunehd.design.components.CrossbarBar
import com.heretek.xunehd.design.components.EdgeCropText
import com.heretek.xunehd.design.components.KineticList
import com.heretek.xunehd.design.components.firstLetterOf
import com.heretek.xunehd.ui.LocalXuneGraph
import com.heretek.xunehd.ui.apps.MiniAppScaffold
import com.heretek.xunehd.ui.apps.XuneApps
import com.heretek.xunehd.ui.components.DetailScaffold
import com.heretek.xunehd.ui.nav.XuneDestination
import kotlinx.coroutines.launch

/**
 * The Zune marketplace shell (canon §3.6, §8): a crossbar pivoting over
 * music / videos / podcasts / apps. The `apps` pivot lists every installed
 * mini-app plus the frozen official catalog (unavailable entries dim).
 */
@Composable
fun MarketplaceScreen(canvasWidth: androidx.compose.ui.unit.Dp) {
    val graph = LocalXuneGraph.current
    val scope = rememberCoroutineScope()
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 0,
        pageCount = { 4 },
    )
    Column(Modifier.fillMaxSize()) {
        CrossbarBar(
            labels = listOf("music", "videos", "podcasts", "apps"),
            selected = pagerState.currentPage,
            onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
        )
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> MarketplaceMusic()
                1 -> EmptyPivot("videos", "coming soon")
                2 -> EmptyPivot("podcasts", "add feeds in podcasts")
                else -> AppsPivot()
            }
        }
    }
}

@Composable
private fun MarketplaceMusic() {
    val graph = LocalXuneGraph.current
    val albums by graph.library.albums().collectAsState(initial = emptyList())
    val featured = remember(albums) { albums.take(12) }
    Column(Modifier.fillMaxSize().padding(horizontal = XuneTokens.EDGE.dp, vertical = 8.dp)) {
        EdgeCropText(
            text = "featured",
            fontSize = XuneTokens.TYPE_CROSSBAR.dp,
            alpha = 0.6f,
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(XuneTokens.GRID_GUTTER.dp),
        ) {
            items(featured, key = { it.albumId }) { album ->
                Column(
                    Modifier.width(XuneTokens.APP_TILE.dp).combinedClickable(
                        onClick = { graph.nav.push(XuneDestination.Album(album.albumId)) },
                        onLongClick = {},
                    ),
                ) {
                    AlbumArt(
                        model = album.albumArtUri,
                        contentDescription = album.title,
                        modifier = Modifier.size(XuneTokens.APP_TILE.dp),
                    )
                    EdgeCropText(text = album.title, fontSize = XuneTokens.TYPE_CAPTION.dp, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun EmptyPivot(label: String, note: String) {
    Column(Modifier.fillMaxSize().padding(XuneTokens.EDGE.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EdgeCropText(text = label, fontSize = XuneTokens.TYPE_NOW_META.dp, alpha = 0.4f)
        EdgeCropText(text = note, fontSize = XuneTokens.TYPE_LIST.dp, alpha = 0.4f)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppsPivot() {
    val graph = LocalXuneGraph.current
    val installed = XuneApps.all
    val catalog = OfficialCatalog.all
    Column(Modifier.fillMaxSize()) {
        if (installed.isNotEmpty()) {
            EdgeCropText(
                text = "installed",
                fontSize = XuneTokens.TYPE_CROSSBAR.dp,
                alpha = 0.6f,
                modifier = Modifier.padding(start = XuneTokens.EDGE.dp, top = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = XuneTokens.EDGE.dp),
                horizontalArrangement = Arrangement.spacedBy(XuneTokens.GRID_GUTTER.dp),
            ) {
                items(installed, key = { it.id }) { app ->
                    Column(
                        Modifier.width(XuneTokens.APP_TILE.dp).combinedClickable(
                            onClick = { graph.nav.push(XuneDestination.MiniApp(app.id)) },
                            onLongClick = {},
                        ),
                    ) {
                        AlbumArt(
                            model = null,
                            contentDescription = app.title,
                            modifier = Modifier.size(XuneTokens.APP_TILE.dp),
                        )
                        EdgeCropText(text = app.title, fontSize = XuneTokens.TYPE_CAPTION.dp, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        EdgeCropText(
            text = "frozen catalog",
            fontSize = XuneTokens.TYPE_CROSSBAR.dp,
            alpha = 0.6f,
            modifier = Modifier.padding(start = XuneTokens.EDGE.dp),
        )
        KineticList(
            items = catalog,
            key = { it.exe },
            letter = { firstLetterOf(it.title) },
            rowContent = { entry, _ ->
                AppsCatalogRow(entry)
            },
        )
    }
}

@Composable
private fun AppsCatalogRow(entry: OfficialApp) {
    val graph = LocalXuneGraph.current
    val colors = LocalXuneColors.current
    val isInstalled = entry.installedId != null
    Row(
        Modifier
            .fillMaxWidth()
            .height(XuneTokens.ROW_HEIGHT.dp)
            .combinedClickable(
                enabled = isInstalled,
                onClick = { entry.installedId?.let { graph.nav.push(XuneDestination.MiniApp(it)) } },
                onLongClick = {},
            )
            .padding(horizontal = XuneTokens.EDGE.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            EdgeCropText(
                text = entry.title,
                fontSize = XuneTokens.TYPE_LIST.dp,
                color = if (isInstalled) colors.textPrimary else colors.textInactive,
            )
            EdgeCropText(
                text = entry.category,
                fontSize = XuneTokens.TYPE_CAPTION.dp,
                color = colors.textSecondary,
            )
        }
        EdgeCropText(
            text = if (isInstalled) "open" else "unavailable",
            fontSize = XuneTokens.TYPE_CAPTION.dp,
            color = if (isInstalled) colors.accent else colors.textInactive,
        )
    }
}

/** Mini-app host screen: looks up the renderer and shows the mini-app fullscreen. */
@Composable
fun MiniAppScreen(appId: String, canvasWidth: androidx.compose.ui.unit.Dp) {
    val graph = LocalXuneGraph.current
    val app = remember(appId) { XuneApps.byId(appId) }
    if (app == null) {
        DetailScaffold(title = "app") {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EdgeCropText(
                    text = "unavailable",
                    fontSize = XuneTokens.TYPE_NOW_META.dp,
                    alpha = 0.4f,
                )
            }
        }
        return
    }
    MiniAppScaffold(title = app.title, onBack = { graph.nav.pop() }) {
        app.render()
    }
}
