@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.heretek.xunehd.ui.screens

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.webkit.WebView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.Selawik
import com.heretek.xunehd.design.XuneTokens
import com.heretek.xunehd.design.components.AlbumArt
import com.heretek.xunehd.design.components.EdgeCropText
import com.heretek.xunehd.design.components.firstLetterOf
import com.heretek.xunehd.design.components.KineticList
import com.heretek.xunehd.ui.LocalXuneGraph
import com.heretek.xunehd.ui.components.DetailScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* ============================================================ */
/*                          Videos                                */
/* ============================================================ */

data class VideoItem(val id: Long, val title: String, val artist: String, val uri: Uri)

@Composable
fun VideosScreen(canvasWidth: androidx.compose.ui.unit.Dp) {
    val graph = LocalXuneGraph.current
    val context = LocalContext.current
    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var playing by remember { mutableStateOf<VideoItem?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val list = mutableListOf<VideoItem>()
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.ARTIST,
                    MediaStore.Video.Media.DISPLAY_NAME,
                ),
                null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC LIMIT 200",
            )
            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
                while (c.moveToNext()) {
                    list += VideoItem(
                        id = c.getLong(idCol),
                        title = c.getString(titleCol) ?: "untitled",
                        artist = c.getString(artistCol) ?: "",
                        uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, c.getLong(idCol)),
                    )
                }
            }
            videos = list
        }
    }

    val cur = playing
    if (cur != null) {
        VideoPlayerScreen(cur) { playing = null }
        return
    }
    DetailScaffold(title = "videos") {
        KineticList(
            items = videos,
            key = { it.id },
            letter = { firstLetterOf(it.title) },
            rowContent = { v, _ ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(XuneTokens.ROW_HEIGHT.dp)
                        .combinedClickable(onClick = { playing = v }, onLongClick = {})
                        .padding(horizontal = XuneTokens.EDGE.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        EdgeCropText(text = v.title, fontSize = XuneTokens.TYPE_LIST.dp)
                        EdgeCropText(text = v.artist, fontSize = XuneTokens.TYPE_CAPTION.dp, color = LocalXuneColors.current.textSecondary)
                    }
                }
            },
        )
    }
}

@Composable
private fun VideoPlayerScreen(item: VideoItem, onExit: () -> Unit) {
    val context = LocalContext.current
    val player = remember { androidx.media3.exoplayer.ExoPlayer.Builder(context).build() }
    DisposableEffect(item.id) {
        player.setMediaItem(androidx.media3.common.MediaItem.fromUri(item.uri))
        player.prepare()
        player.play()
        onDispose { player.release() }
    }
    DetailScaffold(title = "video") {
        Box(Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    android.view.SurfaceView(ctx).also { player.setVideoSurface(it.holder.surface) }
                },
            )
            EdgeCropText(
                text = "<- back",
                fontSize = XuneTokens.TYPE_LIST.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .combinedClickable(onClick = onExit, onLongClick = {}),
                color = LocalXuneColors.current.accent,
            )
            BasicText(
                text = item.title,
                style = TextStyle(fontFamily = Selawik, fontSize = XuneTokens.TYPE_NOW_META.sp, color = LocalXuneColors.current.textPrimary),
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
            )
        }
    }
}

/* ============================================================ */
/*                         Pictures                                */
/* ============================================================ */

data class PictureBucket(val name: String, val items: List<PictureItem>)
data class PictureItem(val id: Long, val displayName: String, val uri: Uri)

@Composable
fun PicturesScreen(canvasWidth: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    val graph = LocalXuneGraph.current
    var buckets by remember { mutableStateOf<List<PictureBucket>>(emptyList()) }
    var selectedBucket by remember { mutableStateOf<PictureBucket?>(null) }
    var viewerIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val byBucket = linkedMapOf<String, MutableList<PictureItem>>()
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                ),
                null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT 500",
            )
            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val bucketCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val item = PictureItem(
                        id = id,
                        displayName = c.getString(nameCol) ?: "untitled",
                        uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
                    )
                    val bucket = c.getString(bucketCol) ?: "unknown"
                    byBucket.getOrPut(bucket) { mutableListOf() } += item
                }
            }
            buckets = byBucket.map { (k, v) -> PictureBucket(k, v) }
        }
    }

    val viewing = selectedBucket
    if (viewing != null) {
        val pagerState = rememberPagerState(initialPage = viewerIndex.coerceAtMost(viewing.items.lastIndex), pageCount = { viewing.items.size })
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { p ->
                val item = viewing.items[p]
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                )
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .combinedClickable(
                        onClick = {
                            val item = viewing.items[pagerState.currentPage]
                            scope.launch { graph.quickplay.pin(com.heretek.xunehd.data.model.PinKind.PICTURE, item.id, item.displayName, "", 0) }
                        },
                        onLongClick = {},
                    ),
            ) {
                EdgeCropText(text = "pin", fontSize = XuneTokens.TYPE_LIST.dp, color = LocalXuneColors.current.accent)
            }
            Box(Modifier.align(Alignment.TopStart).padding(8.dp).combinedClickable(onClick = { selectedBucket = null }, onLongClick = {})) {
                EdgeCropText(text = "<- albums", fontSize = XuneTokens.TYPE_LIST.dp, color = LocalXuneColors.current.accent)
            }
        }
        return
    }

    DetailScaffold(title = "pictures") {
        Column(Modifier.fillMaxSize()) {
            buckets.forEach { bucket ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(XuneTokens.ROW_HEIGHT.dp)
                        .combinedClickable(
                            onClick = { selectedBucket = bucket; viewerIndex = 0 },
                            onLongClick = {},
                        )
                        .padding(horizontal = XuneTokens.EDGE.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        EdgeCropText(text = bucket.name, fontSize = XuneTokens.TYPE_LIST.dp)
                        EdgeCropText(text = "${bucket.items.size} photos", fontSize = XuneTokens.TYPE_CAPTION.dp, color = LocalXuneColors.current.textSecondary)
                    }
                }
            }
        }
    }
}

/* ============================================================ */
/*                          Internet                                */
/* ============================================================ */

@Composable
fun InternetScreen(canvasWidth: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    var url by remember { mutableStateOf("https://duckduckgo.com") }
    var currentUrl by remember { mutableStateOf(url) }
    var input by remember(url) { mutableStateOf(url) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    DetailScaffold(title = "internet") {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(XuneTokens.EDGE.dp)) {
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = Selawik, fontSize = XuneTokens.TYPE_LIST.sp, color = LocalXuneColors.current.textPrimary),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(LocalXuneColors.current.accent),
                    modifier = Modifier.weight(1f).background(LocalXuneColors.current.elevated).padding(6.dp),
                )
                Box(
                    Modifier
                        .padding(start = 6.dp)
                        .combinedClickable(
                            onClick = {
                                val candidate = if (input.startsWith("http")) input else "https://$input"
                                currentUrl = candidate
                                url = candidate
                                webViewRef.value?.loadUrl(candidate)
                            },
                            onLongClick = {},
                        ),
                ) {
                    EdgeCropText(text = "go", fontSize = XuneTokens.TYPE_LIST.dp, color = LocalXuneColors.current.accent)
                }
            }
            AndroidView(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        loadUrl(url)
                        webViewRef.value = this
                    }
                },
                update = { wv ->
                    wv.setBackgroundColor(android.graphics.Color.BLACK)
                },
            )
        }
    }
}

/* ============================================================ */
/*                          Social                                */
/* ============================================================ */

data class MockPost(val author: String, val text: String, val when_: String)

private val FROZEN_FEED = listOf(
    MockPost("zune team", "the Zune Pass lives on in our hearts. thanks for the years.", "september 2012"),
    MockPost("a fellow listener", "what are you zuning today?", "october 2012"),
    MockPost("mix master mika", "shuffled by album all morning — life-changing.", "november 2012"),
)

@Composable
fun SocialScreen(canvasWidth: androidx.compose.ui.unit.Dp) {
    DetailScaffold(title = "social") {
        Column(Modifier.fillMaxSize()) {
            EdgeCropText(
                text = "the social feed is frozen in time.",
                fontSize = XuneTokens.TYPE_NOW_META.dp,
                alpha = 0.6f,
                modifier = Modifier.padding(horizontal = XuneTokens.EDGE.dp, vertical = 8.dp),
            )
            FROZEN_FEED.forEach { post ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = XuneTokens.EDGE.dp, vertical = 8.dp),
                ) {
                    EdgeCropText(text = post.author, fontSize = XuneTokens.TYPE_NOW_META.dp, color = LocalXuneColors.current.accent)
                    EdgeCropText(text = post.text, fontSize = XuneTokens.TYPE_LIST.dp)
                    EdgeCropText(text = post.when_, fontSize = XuneTokens.TYPE_CAPTION.dp, color = LocalXuneColors.current.textSecondary)
                }
            }
        }
    }
}
