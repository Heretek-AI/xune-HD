package com.heretek.xunehd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.heretek.xunehd.ui.XuneRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val graph = (application as XuneApp).graph
        setContent {
            CompositionLocalProvider(
                com.heretek.xunehd.ui.LocalXuneGraph provides graph,
            ) {
                XuneRoot()
            }
        }
    }
}
