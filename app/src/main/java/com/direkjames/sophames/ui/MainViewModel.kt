package com.direkjames.sophames.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.direkjames.sophames.SophamesApp
import com.direkjames.sophames.data.AppContainer
import com.direkjames.sophames.data.EventEntity
import com.direkjames.sophames.data.MemoryEntity
import com.direkjames.sophames.data.Settings
import com.direkjames.sophames.data.ThemeMode
import com.direkjames.sophames.reminders.Reminders
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

class MainViewModel(private val container: AppContainer) : ViewModel() {

    private val memoryDao = container.database.memoryDao()
    private val eventDao = container.database.eventDao()
    private val store = container.settings

    /** Null until DataStore has loaded (the splash screen waits for this). */
    val settings: StateFlow<Settings?> =
        store.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val memories: StateFlow<List<MemoryEntity>> =
        memoryDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val events: StateFlow<List<EventEntity>> =
        eventDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Re-emits every minute so the counters roll over at midnight even if the app stays open. */
    val today: StateFlow<LocalDate> = flow {
        while (true) {
            emit(LocalDate.now())
            delay(60_000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocalDate.now())

    init {
        viewModelScope.launch {
            store.settings.map { it.remindersEnabled }.distinctUntilChanged().collect { enabled ->
                if (enabled) Reminders.schedule(container.appContext) else Reminders.cancel(container.appContext)
            }
        }
    }

    // ---- photos ----
    fun photoFile(name: String): File = container.photos.fileFor(name)

    // ---- memories ----
    fun observeMemory(id: String): Flow<MemoryEntity?> = memoryDao.observe(id)

    suspend fun getMemory(id: String): MemoryEntity? = memoryDao.get(id)

    fun saveMemory(
        id: String?,
        title: String,
        note: String,
        date: LocalDate,
        newPhoto: Uri?,
        removePhoto: Boolean,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val existing = id?.let { memoryDao.get(it) }
            var photoFile = existing?.photoFile
            if (removePhoto && photoFile != null) {
                container.photos.delete(photoFile)
                photoFile = null
            }
            if (newPhoto != null) {
                val imported = runCatching { container.photos.importPhoto(newPhoto) }.getOrNull()
                if (imported != null) {
                    container.photos.delete(photoFile)
                    photoFile = imported
                }
            }
            val base = existing ?: MemoryEntity(title = "", note = "", epochDay = 0L)
            memoryDao.upsert(
                base.copy(
                    title = title.trim(),
                    note = note.trim(),
                    epochDay = date.toEpochDay(),
                    photoFile = photoFile,
                    updatedAt = System.currentTimeMillis(),
                    dirty = true,
                ),
            )
            onDone()
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            val existing = memoryDao.get(id) ?: return@launch
            container.photos.delete(existing.photoFile)
            memoryDao.upsert(
                existing.copy(deleted = true, dirty = true, updatedAt = System.currentTimeMillis()),
            )
        }
    }

    // ---- custom dates ----
    fun addEvent(title: String, date: LocalDate, repeatYearly: Boolean) {
        viewModelScope.launch {
            eventDao.upsert(EventEntity(title = title.trim(), epochDay = date.toEpochDay(), repeatYearly = repeatYearly))
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch { eventDao.softDelete(id, System.currentTimeMillis()) }
    }

    // ---- settings ----
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { store.setThemeMode(mode) }
    }

    /** Quick light/dark flip used by the sun/moon button on Home. */
    fun toggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch { store.setThemeMode(if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK) }
    }

    fun setNames(one: String, two: String) {
        viewModelScope.launch { store.setNames(one.trim(), two.trim()) }
    }

    fun setStartDate(date: LocalDate) {
        viewModelScope.launch { store.setStartDate(date) }
    }

    fun setRemindersEnabled(value: Boolean) {
        viewModelScope.launch { store.setRemindersEnabled(value) }
    }

    fun setOnboarded(value: Boolean) {
        viewModelScope.launch { store.setOnboarded(value) }
    }
}

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SophamesApp
            MainViewModel(app.container)
        }
    }
}
