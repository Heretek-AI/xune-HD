package com.heretek.xunehd.data.repo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.heretek.xunehd.design.XuneAccent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "xune_settings")

data class XuneSettings(
    val accent: XuneAccent = XuneAccent.PINK,
    val deviceMode: Boolean = false,
    val artistImagesEnabled: Boolean = true,
    val artistImageTemplate: String = "",
    val libraryScanned: Boolean = false,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val ACCENT = intPreferencesKey("accent")
        val DEVICE_MODE = booleanPreferencesKey("device_mode")
        val ARTIST_IMAGES = booleanPreferencesKey("artist_images")
        val ARTIST_IMAGE_TEMPLATE = stringPreferencesKey("artist_image_template")
        val LIBRARY_SCANNED = booleanPreferencesKey("library_scanned")
    }

    val settings: Flow<XuneSettings> = context.dataStore.data.map { p ->
        XuneSettings(
            accent = XuneAccent.entries.firstOrNull { it.id == p[Keys.ACCENT] } ?: XuneAccent.PINK,
            deviceMode = p[Keys.DEVICE_MODE] ?: false,
            artistImagesEnabled = p[Keys.ARTIST_IMAGES] ?: true,
            artistImageTemplate = p[Keys.ARTIST_IMAGE_TEMPLATE] ?: "",
            libraryScanned = p[Keys.LIBRARY_SCANNED] ?: false,
        )
    }

    suspend fun setAccent(accent: XuneAccent) = context.dataStore.edit { it[Keys.ACCENT] = accent.id }
    suspend fun setDeviceMode(enabled: Boolean) = context.dataStore.edit { it[Keys.DEVICE_MODE] = enabled }
    suspend fun setArtistImages(enabled: Boolean) = context.dataStore.edit { it[Keys.ARTIST_IMAGES] = enabled }
    suspend fun setArtistImageTemplate(template: String) = context.dataStore.edit { it[Keys.ARTIST_IMAGE_TEMPLATE] = template }
    suspend fun setLibraryScanned(scanned: Boolean) = context.dataStore.edit { it[Keys.LIBRARY_SCANNED] = scanned }
}
