package com.heretek.xunehd.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tracks", indices = [Index("albumId"), Index("artistId"), Index("title")])
data class TrackEntity(
    @PrimaryKey val mediaId: Long,
    val title: String,
    val artist: String,
    val artistId: Long,
    val album: String,
    val albumId: Long,
    val genre: String,
    val durationMs: Long,
    val dateAdded: Long,
    val trackNumber: Int,
    val year: String,
    val uri: String,
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateCreated: Long,
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "mediaId"])
data class PlaylistTrackEntity(
    val playlistId: Long,
    val mediaId: Long,
    val position: Int,
)

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey val mediaId: Long,
    val rating: Int,
)

@Entity(tableName = "pins")
data class PinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val refId: Long,
    val label: String,
    val subLabel: String,
    val artAlbumId: Long,
    val pinnedAt: Long,
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val refId: Long,
    val label: String,
    val subLabel: String,
    val artAlbumId: Long,
    val playedAt: Long,
)

@Entity(tableName = "artist_images")
data class ArtistImageEntity(
    @PrimaryKey val artistKey: String,
    val imagePath: String,
    val fetchedAt: Long,
)
