package com.direkjames.sophames.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sophames_settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class Settings(
    val startDate: LocalDate,
    val nameOne: String,
    val nameTwo: String,
    val themeMode: ThemeMode,
    val onboarded: Boolean,
    val remindersEnabled: Boolean,
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val START = longPreferencesKey("start_epoch_day")
        val NAME_ONE = stringPreferencesKey("name_one")
        val NAME_TWO = stringPreferencesKey("name_two")
        val THEME = stringPreferencesKey("theme_mode")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val REMINDERS = booleanPreferencesKey("reminders")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            startDate = LocalDate.ofEpochDay(p[Keys.START] ?: DEFAULT_START.toEpochDay()),
            nameOne = p[Keys.NAME_ONE] ?: "Sophia",
            nameTwo = p[Keys.NAME_TWO] ?: "James",
            themeMode = p[Keys.THEME]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.LIGHT,
            onboarded = p[Keys.ONBOARDED] ?: false,
            remindersEnabled = p[Keys.REMINDERS] ?: true,
        )
    }

    suspend fun setStartDate(date: LocalDate) {
        context.dataStore.edit { it[Keys.START] = date.toEpochDay() }
    }

    suspend fun setNames(one: String, two: String) {
        context.dataStore.edit {
            it[Keys.NAME_ONE] = one
            it[Keys.NAME_TWO] = two
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setOnboarded(value: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDED] = value }
    }

    suspend fun setRemindersEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.REMINDERS] = value }
    }

    companion object {
        /** The day it all began. */
        val DEFAULT_START: LocalDate = LocalDate.of(2025, 1, 18)
    }
}
