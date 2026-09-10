package com.heretek.xunehd.ui.apps

import androidx.compose.runtime.Composable
import com.heretek.xunehd.ui.LocalXuneGraph

/**
 * A single mini-app: id, label, category, and a composable renderer that
 * reads the XuneGraph from [LocalXuneGraph] and calls `nav.pop()` itself
 * (e.g. via the cropped-header scaffold).
 */
data class XuneMiniApp(
    val id: String,
    val title: String,
    val category: String,
    val render: @Composable () -> Unit,
)

/**
 * The registry of installed mini-apps (canon §8). Built once at load time;
 * `byId` is used by the marketplace apps pivot and the destination router.
 */
object XuneApps {
    var all: List<XuneMiniApp> = emptyList()

    fun byId(id: String): XuneMiniApp? = all.firstOrNull { it.id == id }
}

/** The master list of installed mini-apps (Phase 3/4 append here). */
fun buildAppRegistry(): List<XuneMiniApp> = listOf(
    XuneMiniApp("calculator", "calculator", "utilities", { CalculatorApp() }),
    XuneMiniApp("notes", "notes", "utilities", { NotesApp() }),
    XuneMiniApp("stopwatch", "stopwatch", "utilities", { StopwatchApp() }),
    XuneMiniApp("metronome", "metronome", "music", { MetronomeApp() }),
    XuneMiniApp("alarm", "alarm clock", "utilities", { AlarmClockApp() }),
    XuneMiniApp("calendar", "calendar", "utilities", { CalendarApp() }),
    XuneMiniApp("level", "level", "utilities", { LevelApp() }),
    XuneMiniApp("piano", "piano", "music", { PianoApp() }),
    XuneMiniApp("drummachine", "drum machine", "music", { DrumMachineApp() }),
    XuneMiniApp("chordfinder", "chord finder", "music", { ChordFinderApp() }),
    XuneMiniApp("musicquiz", "music quiz", "music", { MusicQuizApp() }),
    XuneMiniApp("shufflebyalbum", "shuffle by album", "music", { ShuffleByAlbumApp() }),
    XuneMiniApp("solitaire", "solitaire", "games", { com.heretek.xunehd.ui.apps.games.SolitaireApp() }),
    XuneMiniApp("sudoku", "sudoku", "games", { com.heretek.xunehd.ui.apps.games.SudokuApp() }),
    XuneMiniApp("hexic", "hexic", "games", { com.heretek.xunehd.ui.apps.games.HexicApp() }),
    XuneMiniApp("reversi", "reversi", "games", { com.heretek.xunehd.ui.apps.games.ReversiApp() }),
    XuneMiniApp("weather", "weather", "reading", { com.heretek.xunehd.ui.apps.mocks.WeatherMock() }),
    XuneMiniApp("twitter", "twitter", "social", { com.heretek.xunehd.ui.apps.mocks.TwitterMock() }),
    XuneMiniApp("facebook", "facebook", "social", { com.heretek.xunehd.ui.apps.mocks.FacebookMock() }),
    XuneMiniApp("email", "email", "networking", { com.heretek.xunehd.ui.apps.mocks.EmailMock() }),
    XuneMiniApp("messenger", "messenger", "networking", { com.heretek.xunehd.ui.apps.mocks.MessengerMock() }),
    XuneMiniApp("msnmoney", "msn money", "reading", { com.heretek.xunehd.ui.apps.mocks.MsnMoneyMock() }),
    XuneMiniApp("zunereader", "zune reader", "reading", { com.heretek.xunehd.ui.apps.mocks.ZuneReaderMock() }),
    XuneMiniApp("social", "social", "social", { com.heretek.xunehd.ui.apps.mocks.SocialMock() }),
)

/** Mini-app scaffold: cropped-header back, fullscreen content. */
@Composable
fun MiniAppScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    com.heretek.xunehd.ui.components.DetailScaffold(title = title) {
        content()
    }
}
