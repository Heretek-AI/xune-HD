package com.heretek.xunehd.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.heretek.xunehd.XuneGraph

val LocalXuneGraph = staticCompositionLocalOf<XuneGraph> {
    error("XuneGraph not provided")
}
