package com.direkjames.sophames.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MemoryEntity::class, EventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SophamesDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun eventDao(): EventDao

    companion object {
        fun build(context: Context): SophamesDatabase =
            Room.databaseBuilder(context, SophamesDatabase::class.java, "sophames.db").build()
    }
}
