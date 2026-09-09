package com.heretek.xunehd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heretek.xunehd.BuildConfig
import com.heretek.xunehd.design.LocalXuneColors
import com.heretek.xunehd.design.XuneAccent
import com.heretek.xunehd.design.XuneTokens
import com.heretek.xunehd.design.components.EdgeCropText
import com.heretek.xunehd.ui.LocalXuneGraph
import com.heretek.xunehd.ui.components.DetailScaffold
import com.heretek.xunehd.ui.components.SectionLabel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(canvasWidth: androidx.compose.ui.unit.Dp) {
    val graph = LocalXuneGraph.current
    val scope = rememberCoroutineScope()
    val settings by graph.settingsFlow.collectAsState(initial = com.heretek.xunehd.data.repo.XuneSettings())
    val colors = LocalXuneColors.current
    val scanState by graph.library.scanning.collectAsState()
    val menus = com.heretek.xunehd.ui.components.LocalContextMenu.current

    DetailScaffold(title = "settings") {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 48.dp),
        ) {
            SectionLabel("accent")
            Row(
                Modifier.padding(horizontal = XuneTokens.EDGE.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                XuneAccent.entries.forEach { accent ->
                    val selected = accent == settings.accent
                    Box(
                        Modifier
                            .size(28.dp)
                            .background(accent.primary)
                            .border(
                                width = if (selected) 2.dp else 0.5.dp,
                                color = if (selected) colors.textPrimary else colors.border,
                            )
                            .clickable { scope.launch { graph.settings.setAccent(accent) } },
                    )
                }
            }

            SectionLabel("display")
            SettingsToggle(
                label = "device mode",
                subLabel = "authentic 480x272 canvas (best in landscape)",
                value = settings.deviceMode,
            ) { enabled ->
                scope.launch { graph.settings.setDeviceMode(enabled) }
            }
            SettingsToggle(
                label = "artist photos",
                subLabel = "now playing backdrop, via musicbrainz",
                value = settings.artistImagesEnabled,
            ) { enabled ->
                scope.launch { graph.settings.setArtistImages(enabled) }
            }
            SettingsRow(
                label = "artist photo source",
                subLabel = settings.artistImageTemplate.ifBlank { "not configured" },
                onClick = {
                    menus.showPrompt("artist photo source", "url template with {mbid}") { template ->
                        scope.launch { graph.settings.setArtistImageTemplate(template) }
                    }
                },
            )

            SectionLabel("collection")
            SettingsRow(
                label = if (scanState) "scanning…" else "refresh collection",
                subLabel = "rebuild the library from device media",
                onClick = { scope.launch { graph.library.refresh() } },
            )

            SectionLabel("about")
            SettingsRow(
                label = "xune hd ${BuildConfig.VERSION_NAME}",
                subLabel = "sister to not-zune — the zune hd, reborn on android",
                onClick = {},
            )
            Spacer(Modifier.height(12.dp))
            EdgeCropText(
                text = "selawik stands in for zegoe (sil ofl). zune, zegoe and the zune hd are trademarks of microsoft; this is an independent homage.",
                fontSize = XuneTokens.TYPE_CAPTION.dp,
                alpha = 0.4f,
                modifier = Modifier.padding(horizontal = XuneTokens.EDGE.dp),
            )
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    subLabel: String,
    onClick: () -> Unit,
) {
    val colors = LocalXuneColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = XuneTokens.EDGE.dp, vertical = 8.dp),
    ) {
        EdgeCropText(text = label, fontSize = XuneTokens.TYPE_LIST.dp)
        EdgeCropText(
            text = subLabel,
            fontSize = XuneTokens.TYPE_LIST_SECONDARY.dp,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    subLabel: String,
    value: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val colors = LocalXuneColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onChange(!value) }
            .padding(horizontal = XuneTokens.EDGE.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            EdgeCropText(text = label, fontSize = XuneTokens.TYPE_LIST.dp)
            EdgeCropText(
                text = subLabel,
                fontSize = XuneTokens.TYPE_LIST_SECONDARY.dp,
                color = colors.textSecondary,
            )
        }
        Box(
            Modifier
                .background(if (value) colors.accent else colors.tile)
                .border(0.5.dp, colors.border)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            EdgeCropText(
                text = if (value) "on" else "off",
                fontSize = XuneTokens.TYPE_LIST.dp,
                color = if (value) colors.textPrimary else colors.textSecondary,
            )
        }
    }
}
