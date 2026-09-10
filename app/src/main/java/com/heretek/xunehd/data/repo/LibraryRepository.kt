package com.heretek.xunehd.data.repo

import android.content.Context
import com.heretek.xunehd.data.db.XuneDatabase
import com.heretek.xunehd.data.model.Album
import com.heretek.xunehd.data.model.Artist
import com.heretek.xunehd.data.model.Genre
import com.heretek.xunehd.data.model.Playlist
import com.heretek.xunehd.data.model.Track
import com.heretek.xunehd.data.scan.MediaLibraryScanner
import com.heretek.xunehd.data.scan.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class LibraryRepository(
    private val context: Context,
    private val db: XuneDatabase,
) {
    val scanning = MutableStateFlow(false)
    val lastScanResult = MutableStateFlow<MediaLibraryScanner.ScanResult?>(null)

    fun tracks(): Flow<List<Track>> = db.trackDao().tracks().map { list -> list.map { it.toModel() } }
    fun albums(): Flow<List<Album>> = db.trackDao().albumRows().map { rows -> rows.map { it.toAlbum() } }
    fun artists(): Flow<List<Artist>> = db.trackDao().artistRows().map { rows -> rows.map { it.toArtist() } }
    fun genres(): Flow<List<Genre>> = db.trackDao().genreRows().map { rows -> rows.map { it.toGenre() } }
    fun playlists(): Flow<List<Playlist>> = db.playlistDao().playlists().map { rows -> rows.map { it.toPlaylist() } }

    suspend fun track(id: Long): Track? = db.trackDao().track(id)?.toModel()
    suspend fun tracksByAlbum(albumId: Long): List<Track> = db.trackDao().tracksByAlbum(albumId).map { it.toModel() }
    suspend fun tracksByArtist(artistId: Long): List<Track> = db.trackDao().tracksByArtist(artistId).map { it.toModel() }
    suspend fun tracksByGenre(genre: String): List<Track> = db.trackDao().tracksByGenre(genre).map { it.toModel() }
    suspend fun tracksInPlaylist(playlistId: Long): List<Track> = db.playlistDao().tracks(playlistId).map { it.toModel() }
    suspend fun recentlyAdded(limit: Int = 24): List<Track> = db.trackDao().recentlyAdded(limit).map { it.toModel() }
    suspend fun search(q: String): List<Track> = db.trackDao().search(q).map { it.toModel() }
    suspend fun albumsByArtist(artistId: Long): List<Album> = db.trackDao().albumRowsByArtist(artistId).map { it.toAlbum() }

    /** Artists sharing this artist's genres — the artist page's `related` pivot. */
    suspend fun relatedArtists(artistId: Long): List<Artist> {
        val genres = db.trackDao().tracksByArtist(artistId).map { it.genre }.distinct()
        if (genres.isEmpty()) return emptyList()
        return db.trackDao().relatedArtistRows(artistId, genres, 12).map { it.toArtist() }
    }
    suspend fun trackCount(): Int = db.trackDao().count()

    suspend fun refresh(): MediaLibraryScanner.ScanResult {
        scanning.value = true
        try {
            return MediaLibraryScanner(context, db).scan().also { lastScanResult.value = it }
        } finally {
            scanning.value = false
        }
    }

    suspend fun createPlaylist(name: String): Long = db.playlistDao().create(
        com.heretek.xunehd.data.db.PlaylistEntity(name = name, dateCreated = System.currentTimeMillis()),
    )

    suspend fun addToPlaylist(playlistId: Long, track: Track) {
        val max = db.playlistDao().maxPosition(playlistId) ?: -1
        db.playlistDao().addToPlaylist(
            com.heretek.xunehd.data.db.PlaylistTrackEntity(playlistId, track.mediaId, max + 1),
        )
    }

    suspend fun removeFromPlaylist(playlistId: Long, mediaId: Long) = db.playlistDao().removeFromPlaylist(playlistId, mediaId)
    suspend fun deletePlaylist(playlistId: Long) = db.playlistDao().deletePlaylistAndMembers(playlistId)
}
