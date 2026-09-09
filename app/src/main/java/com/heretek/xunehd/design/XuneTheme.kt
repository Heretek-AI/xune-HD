package com.heretek.xunehd.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.heretek.xunehd.R

/**
 * Selawik — the OFL Segoe-metric font family that stands in for Zegoe UI.
 * Users may import Zegoe in a later release; Selawik ships by default.
 */
val Selawik = FontFamily(
    Font(R.font.selawkl, FontWeight.Light),
    Font(R.font.selawksl, FontWeight(350)),
    Font(R.font.selawk, FontWeight.Normal),
    Font(R.font.selawksb, FontWeight.SemiBold),
    Font(R.font.selawkb, FontWeight.Bold),
)

val LocalXuneColors = staticCompositionLocalOf<XuneColors> {
    error("XuneColors not provided")
}

@Composable
fun XuneTheme(
    accent: XuneAccent = XuneAccent.PINK,
    content: @Composable () -> Unit,
) {
    val colors = XuneColors(accent = accent.primary, accentBright = accent.bright)
    CompositionLocalProvider(LocalXuneColors provides colors) {
        content()
    }
}
