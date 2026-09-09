package com.heretek.xunehd.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.heretek.xunehd.data.model.Album
import com.heretek.xunehd.data.model.Artist
import com.heretek.xunehd.data.model.Genre
import kotlinx.coroutines.flow.Flow

data class AlbumRow(
    val albumId: Long,
    val album: String,
    val artist: String,
    val artistId: Long,
    val year: String,
    val trackCount: Int,
    val totalDurationMs: Long,
    val dateAdded: Long,
) {
    fun toAlbum() = Album(albumId, album, artist, artistId, year, trackCount, totalDurationMs, dateAdded)
}

data class ArtistRow(
    val artistId: Long,
    val artist: String,
    val albumCount: Int,
    val trackCount: Int,
) {
    fun toArtist() = Artist(artistId, artist, albumCount, trackCount)
}

data class GenreRow(val genre: String, val trackCount: Int) {
    fun toGenre() = Genre(genre, trackCount)
}

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE")
    fun tracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE mediaId = :id")
    suspend fun track(id: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE mediaId IN (:ids)")
    suspend fun tracksByIds(ids: List<Long>): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE albumId = :albumId ORDER BY trackNumber, title COLLATE NOCASE")
    suspend fun tracksByAlbum(albumId: Long): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE artistId = :artistId ORDER BY album COLLATE NOCASE, trackNumber")
    suspend fun tracksByArtist(artistId: Long): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE genre = :genre ORDER BY album COLLATE NOCASE, trackNumber")
    suspend fun tracksByGenre(genre: String): List<TrackEntity>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun recentlyAdded(limit: Int): List<TrackEntity>

    @Query(
        "SELECT * FROM tracks WHERE title LIKE '%' || :q || '%' COLLATE NOCASE " +
            "OR artist LIKE '%' || :q || '%' COLLATE NOCASE " +
            "OR album LIKE '%' || :q || '%' COLLATE NOCASE " +
            "ORDER BY title COLLATE NOCASE LIMIT 100"
    )
    suspend fun search(q: String): List<TrackEntity>

    @Query(
        "SELECT albumId AS albumId, album AS album, artist AS artist, artistId AS artistId, " +
            "MAX(year) AS year, COUNT(*) AS trackCount, SUM(durationMs) AS totalDurationMs, " +
            "MAX(dateAdded) AS dateAdded FROM tracks GROUP BY albumId ORDER BY album COLLATE NOCASE"
    )
    fun albumRows(): Flow<List<AlbumRow>>

    @Query(
        "SELECT albumId AS albumId, album AS album, artist AS artist, artistId AS artistId, " +
            "MAX(year) AS year, COUNT(*) AS trackCount, SUM(durationMs) AS totalDurationMs, " +
            "MAX(dateAdded) AS dateAdded FROM tracks WHERE artistId = :artistId " +
            "GROUP BY albumId ORDER BY year, album COLLATE NOCASE"
    )
    suspend fun albumRowsByArtist(artistId: Long): List<AlbumRow>

    @Query(
        "SELECT artistId AS artistId, artist AS artist, COUNT(DISTINCT albumId) AS albumCount, " +
            "COUNT(*) AS trackCount FROM tracks GROUP BY artistId ORDER BY artist COLLATE NOCASE"
    )
    fun artistRows(): Flow<List<ArtistRow>>

    @Query("SELECT genre AS genre, COUNT(*) AS trackCount FROM tracks GROUP BY genre ORDER BY genre COLLATE NOCASE")
    fun genreRows(): Flow<List<GenreRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks WHERE mediaId IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun count(): Int

    @Query("SELECT DISTINCT artist FROM tracks ORDER BY artist COLLATE NOCASE")
    suspend fun distinctArtistNames(): List<String>
}

@Dao
interface RatingDao {
    @Query("SELECT rating FROM ratings WHERE mediaId = :id")
    fun ratingOf(id: Long): Flow<Int?>

    @Query("SELECT * FROM ratings WHERE rating = :rating")
    suspend fun tracksWithRating(rating: Int): List<RatingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(entity: RatingEntity)

    @Query("DELETE FROM ratings WHERE mediaId = :id")
    suspend fun clear(id: Long)
}

@Dao
interface PlaylistDao {
    @Query("SELECT id, name, dateCreated, (SELECT COUNT(*) FROM playlist_tracks pt WHERE pt.playlistId = p.id) AS trackCount FROM playlists p ORDER BY name COLLATE NOCASE")
    fun playlists(): Flow<List<PlaylistRow>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun playlist(id: Long): PlaylistEntity?

    @Query(
        "SELECT t.* FROM tracks t JOIN playlist_tracks pt ON pt.mediaId = t.mediaId " +
            "WHERE pt.playlistId = :playlistId ORDER BY pt.position"
    )
    suspend fun tracks(playlistId: Long): List<TrackEntity>

    @Insert
    suspend fun create(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun deleteMemberships(playlistId: Long)

    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToPlaylist(item: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND mediaId = :mediaId")
    suspend fun removeFromPlaylist(playlistId: Long, mediaId: Long)

    @Transaction
    suspend fun deletePlaylistAndMembers(id: Long) {
        deleteMemberships(id)
        delete(id)
    }

    data class PlaylistRow(val id: Long, val name: String, val dateCreated: Long, val trackCount: Int) {
        fun toPlaylist() = com.heretek.xunehd.data.model.Playlist(id, name, dateCreated, trackCount)
    }
}

@Dao
interface PinDao {
    @Query("SELECT * FROM pins ORDER BY pinnedAt DESC")
    fun pins(): Flow<List<PinEntity>>

    @Insert
    suspend fun pin(entity: PinEntity)

    @Query("DELETE FROM pins WHERE kind = :kind AND refId = :refId")
    suspend fun unpin(kind: String, refId: Long)

    @Query("SELECT COUNT(*) FROM pins WHERE kind = :kind AND refId = :refId")
    suspend fun isPinned(kind: String, refId: Long): Int
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY playedAt DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(entity: HistoryEntity)

    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY playedAt DESC LIMIT :keep)")
    suspend fun trim(keep: Int)
}

@Dao
interface ArtistImageDao {
    @Query("SELECT * FROM artist_images WHERE artistKey = :key")
    suspend fun get(key: String): ArtistImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: ArtistImageEntity)
}
