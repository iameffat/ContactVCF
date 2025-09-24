package com.contacts.vcf.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val THEME_KEY = intPreferencesKey("theme_option")
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }

    suspend fun saveThemePreference(themeOption: Int) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeOption
        }
    }

    val themePreference: Flow<Int> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_SYSTEM
    }
}