package com.direkjames.sophames.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Every row has a UUID id, updatedAt, deleted (soft delete) and dirty flag so the
 * Supabase sync layer can be added later without changing the schema.
 */
@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val note: String,
    val epochDay: Long,
    /** File name inside filesDir/photos, or null when the memory has no photo. */
    val photoFile: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val dirty: Boolean = true,
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val epochDay: Long,
    val repeatYearly: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val dirty: Boolean = true,
)
