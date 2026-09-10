package com.heretek.xunehd

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.heretek.xunehd.data.db.XuneDatabase
import com.heretek.xunehd.data.repo.LibraryRepository
import com.heretek.xunehd.data.repo.QuickplayRepository
import com.heretek.xunehd.data.repo.SettingsRepository
import com.heretek.xunehd.media.PlaybackController
import com.heretek.xunehd.net.ArtistImageService
import com.heretek.xunehd.ui.nav.XuneNav
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * The application graph. Deliberately small and hand-wired: no DI framework,
 * mirroring the lean structure of the Zune-era codebase we emulate.
 */
class XuneGraph(
    val library: LibraryRepository,
    val quickplay: QuickplayRepository,
    val settings: SettingsRepository,
    val settingsFlow: kotlinx.coroutines.flow.Flow<com.heretek.xunehd.data.repo.XuneSettings>,
    val controller: PlaybackController,
    val nav: XuneNav,
    val artistImages: ArtistImageService,
    val artistBios: com.heretek.xunehd.net.ArtistBioService,
    val notes: com.heretek.xunehd.data.repo.NotesRepository,
    val calendar: com.heretek.xunehd.data.repo.CalendarRepository,
    val alarms: com.heretek.xunehd.data.repo.AlarmRepository,
    val radio: com.heretek.xunehd.data.repo.RadioRepository,
    val podcasts: com.heretek.xunehd.data.repo.PodcastRepository,
    val games: com.heretek.xunehd.data.repo.GameRepository,
)

class XuneApp : Application() {

    lateinit var graph: XuneGraph
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val db = XuneDatabase.get(this)
        val library = LibraryRepository(this, db)
        val quickplay = QuickplayRepository(db)
        val settings = SettingsRepository(this)
        val controller = PlaybackController(this, library, quickplay)
        val nav = XuneNav()
        val artistImages = ArtistImageService(this, db, settings)
        val artistBios = com.heretek.xunehd.net.ArtistBioService(this)
        val notes = com.heretek.xunehd.data.repo.NotesRepository(db)
        val calendar = com.heretek.xunehd.data.repo.CalendarRepository(db)
        val alarms = com.heretek.xunehd.data.repo.AlarmRepository(db)
        val radio = com.heretek.xunehd.data.repo.RadioRepository(db)
        val podcasts = com.heretek.xunehd.data.repo.PodcastRepository(db)
        val games = com.heretek.xunehd.data.repo.GameRepository(db)

        graph = XuneGraph(
            library, quickplay, settings, settings.settings, controller, nav,
            artistImages, artistBios, notes, calendar, alarms, radio, podcasts, games,
        )

        appScope.launch {
            controller.connect()
        }

        // Keep the artist-image service aware of the user's settings.
        settings.settings
            .onEach { artistImages.settingsSnapshot = it }
            .launchIn(appScope)

        // Opt-in MediaStore watcher (Settings > collection > watch media store).
        // Debounced 2 s to coalesce bursts (e.g. mass-transfer).
        var watcher: ContentObserver? = null
        val handler = Handler(Looper.getMainLooper())
        settings.settings
            .onEach { cfg ->
                val want = cfg.watchMediaStore
                if (want && watcher == null) {
                    val w = object : ContentObserver(handler) {
                        override fun onChange(selfChange: Boolean, uri: Uri?) {
                            appScope.launch {
                                delay(2_000)
                                library.refresh()
                            }
                        }
                    }
                    contentResolver.registerContentObserver(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        true,
                        w,
                    )
                    watcher = w
                } else if (!want && watcher != null) {
                    contentResolver.unregisterContentObserver(watcher!!)
                    watcher = null
                }
            }
            .launchIn(appScope)

        // First launch: build the collection from MediaStore.
        appScope.launch {
            if (settings.settings.first().libraryScanned.not()) {
                library.refresh()
                settings.setLibraryScanned(true)
            }
        }

        // Seed radio station defaults and register mini-apps (canon §8).
        appScope.launch { radio.seedDefaultsIfEmpty() }
        // Touch XuneApps so its lazy registry is materialized before any UI
        // looks it up; equivalent to the prior eager assignment.
        com.heretek.xunehd.ui.apps.XuneApps.all.size
    }
}
