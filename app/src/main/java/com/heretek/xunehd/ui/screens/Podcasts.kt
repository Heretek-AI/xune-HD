@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.heretek.xunehd.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.heretek.xunehd.data.db.PodcastEpisodeEntity
import com.heretek.xunehd.data.db.PodcastFeedEntity
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.XuneTokens
import com.heretek.xunehd.design.components.EdgeCropText
import com.heretek.xunehd.design.components.KineticList
import com.heretek.xunehd.design.components.firstLetterOf
import com.heretek.xunehd.ui.LocalXuneGraph
import com.heretek.xunehd.ui.components.DetailScaffold
import com.heretek.xunehd.ui.components.LocalContextMenu
import com.heretek.xunehd.ui.components.MenuAction
import com.heretek.xunehd.ui.nav.XuneDestination
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Pure RSS / Atom podcast feed parser (Android's built-in XmlPullParser — no
 * new dependency). Returns null when the body is malformed; tolerates both
 * RSS 2.0 and Atom feeds by picking whichever <channel>/<feed> root is
 * present.
 */
object PodcastRss {
    data class Parsed(
        val title: String,
        val description: String,
        val artworkUrl: String,
        val episodes: List<ParsedEpisode>,
    )
    data class ParsedEpisode(
        val title: String,
        val pubAt: Long,
        val durationMs: Long,
        val enclosureUrl: String,
    )

    fun parse(stream: InputStream): Parsed? {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser().apply { setInput(stream, null) }
        var inChannel = false
        var title = ""; var description = ""; var artworkUrl = ""
        var currentTitle = ""; var currentDate = 0L; var currentDur = 0L; var currentEnc = ""
        val episodes = mutableListOf<ParsedEpisode>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.lowercase()
                    when {
                        tag == "channel" || tag == "feed" -> inChannel = true
                        tag == "title" && inChannel -> title = parser.nextText()
                        tag == "description" || tag == "subtitle" || tag == "itunes:subtitle" -> {
                            if (inChannel) description = parser.nextText()
                        }
                        tag == "image" || tag == "itunes:image" -> {
                            artworkUrl = parser.getAttributeValue(null, "href") ?: ""
                        }
                        tag == "item" || tag == "entry" -> {
                            currentTitle = ""; currentDate = 0L; currentDur = 0L; currentEnc = ""
                        }
                        tag == "title" && inChannel.not() -> { /* per-episode below */ }
                        tag == "title" -> if (inChannel && title.isNotEmpty().not()) title = parser.nextText()
                        tag == "title" -> currentTitle = parser.nextText()
                        tag == "pubdate" || tag == "published" -> currentDate = parseDate(parser.nextText())
                        tag == "duration" -> currentDur = parseDurationMs(parser.nextText())
                        tag == "enclosure" -> currentEnc = parser.getAttributeValue(null, "url") ?: ""
                        tag == "link" && currentEnc.isEmpty() -> currentEnc = parser.getAttributeValue(null, "href") ?: ""
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.lowercase()
                    if (tag == "item" || tag == "entry") {
                        if (currentTitle.isNotEmpty() && currentEnc.isNotEmpty()) {
                            episodes += ParsedEpisode(currentTitle, currentDate, currentDur, currentEnc)
                        }
                    }
                }
            }
            event = parser.next()
        }
        if (title.isEmpty() && episodes.isEmpty()) return null
        return Parsed(title.ifEmpty { "untitled feed" }, description, artworkUrl, episodes)
    }

    private fun parseDate(s: String): Long = try {
        java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.US).parse(s)?.time ?: 0L
    } catch (_: Exception) { 0L }

    private fun parseDurationMs(s: String): Long {
        val parts = s.trim().split(":")
        return try {
            when (parts.size) {
                1 -> parts[0].toLong() * 1000L
                2 -> parts[0].toLong() * 60_000L + parts[1].toLong() * 1000L
                3 -> parts[0].toLong() * 3_600_000L + parts[1].toLong() * 60_000L + parts[2].toLong() * 1000L
                else -> 0L
            }
        } catch (_: Exception) { 0L }
    }
}

object PodcastFetcher {
    suspend fun fetch(feedUrl: String): PodcastRss.Parsed? {
        return try {
            val conn = URL(feedUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 15000
            conn.setRequestProperty("User-Agent", "XuneHD/0.1")
            if (conn.responseCode !in 200..299) null
            else PodcastRss.parse(conn.inputStream)
        } catch (_: Exception) { null }
    }
}

@Composable
fun PodcastsScreen(canvasWidth: androidx.compose.ui.unit.Dp) {
    val graph = LocalXuneGraph.current
    val menus = LocalContextMenu.current
    val scope = rememberCoroutineScope()
    val feeds by graph.podcasts.feeds().collectAsState(initial = emptyList())

    DetailScaffold(title = "podcasts") {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = XuneTokens.EDGE.dp, vertical = 8.dp)
                    .combinedClickable(
                        onClick = {
                            menus.showPrompt("add feed", "https://example.com/feed.xml") { url ->
                                scope.launch {
                                    val parsed = PodcastFetcher.fetch(url)
                                    if (parsed != null) {
                                        val feedId = graph.podcasts.addFeed(
                                            PodcastFeedEntity(
                                                title = parsed.title,
                                                feedUrl = url,
                                                artworkUrl = parsed.artworkUrl,
                                                description = parsed.description,
                                                subscribedAt = System.currentTimeMillis(),
                                            ),
                                        )
                                        graph.podcasts.addEpisodes(
                                            parsed.episodes.map { e ->
                                                PodcastEpisodeEntity(
                                                    feedId = feedId,
                                                    title = e.title,
                                                    pubAt = e.pubAt,
                                                    durationMs = e.durationMs,
                                                    enclosureUrl = e.enclosureUrl,
                                                    played = false,
                                                    positionMs = 0,
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        },
                        onLongClick = {},
                    ),
            ) {
                EdgeCropText(text = "+ add feed by url", fontSize = XuneTokens.TYPE_LIST.dp, color = LocalXuneColors.current.accent)
            }
            KineticList(
                items = feeds,
                key = { it.id },
                letter = { firstLetterOf(it.title) },
                rowContent = { feed, _ ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(XuneTokens.ROW_HEIGHT.dp)
                            .combinedClickable(
                                onClick = { graph.nav.push(XuneDestination.PodcastFeed(feed.id)) },
                                onLongClick = {
                                    menus.show(
                                        title = feed.title,
                                        actions = listOf(
                                            MenuAction("remove") {
                                                scope.launch { graph.podcasts.deleteFeed(feed.id) }
                                            },
                                        ),
                                    )
                                },
                            )
                            .padding(horizontal = XuneTokens.EDGE.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            EdgeCropText(text = feed.title, fontSize = XuneTokens.TYPE_LIST.dp)
                            EdgeCropText(text = feed.description.take(60), fontSize = XuneTokens.TYPE_CAPTION.dp, color = LocalXuneColors.current.textSecondary)
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun PodcastFeedScreen(feedId: Long, canvasWidth: androidx.compose.ui.unit.Dp) {
    val graph = LocalXuneGraph.current
    val scope = rememberCoroutineScope()
    val episodes by graph.podcasts.episodes(feedId).collectAsState(initial = emptyList())
    val feedTitle = remember(feedId) { "podcast" }

    DetailScaffold(title = feedTitle) {
        KineticList(
            items = episodes,
            key = { it.id },
            letter = { firstLetterOf(it.title) },
            rowContent = { ep, _ ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(XuneTokens.ROW_HEIGHT.dp)
                        .combinedClickable(
                            onClick = {
                                scope.launch {
                                    val track = com.heretek.xunehd.data.model.Track(
                                        mediaId = ep.id,
                                        title = ep.title,
                                        artist = "",
                                        artistId = 0,
                                        album = feedTitle,
                                        albumId = 0,
                                        genre = "podcast",
                                        durationMs = ep.durationMs,
                                        dateAdded = ep.pubAt,
                                        trackNumber = 0,
                                        year = "",
                                        uri = android.net.Uri.parse(ep.enclosureUrl),
                                    )
                                    graph.controller.play(listOf(track))
                                }
                            },
                            onLongClick = {},
                        )
                        .padding(horizontal = XuneTokens.EDGE.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        EdgeCropText(text = ep.title, fontSize = XuneTokens.TYPE_LIST.dp)
                        EdgeCropText(text = formatDuration(ep.durationMs), fontSize = XuneTokens.TYPE_CAPTION.dp, color = LocalXuneColors.current.textSecondary)
                    }
                }
            },
        )
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return ""
    val s = ms / 1000; val m = s / 60; val h = m / 60
    return when {
        h > 0 -> "%d:%02d:%02d".format(h, m % 60, s % 60)
        else -> "%d:%02d".format(m, s % 60)
    }
}
