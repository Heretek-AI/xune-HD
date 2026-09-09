package com.heretek.xunehd.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        RatingEntity::class,
        PinEntity::class,
        HistoryEntity::class,
        ArtistImageEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class XuneDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun ratingDao(): RatingDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun pinDao(): PinDao
    abstract fun historyDao(): HistoryDao
    abstract fun artistImageDao(): ArtistImageDao

    companion object {
        @Volatile
        private var instance: XuneDatabase? = null

        fun get(context: Context): XuneDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, XuneDatabase::class.java, "xune.db")
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
