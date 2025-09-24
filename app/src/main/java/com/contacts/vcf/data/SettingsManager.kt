package com.contacts.vcf.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        // Theme Keys
        val THEME_KEY = intPreferencesKey("theme_option")
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2

        // Country Code Keys
        val COUNTRY_CODE_KEY = stringPreferencesKey("country_code")
        val ALWAYS_ASK_KEY = booleanPreferencesKey("always_ask_for_code")
    }

    // Theme Preferences
    suspend fun saveThemePreference(themeOption: Int) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeOption
        }
    }

    val themePreference: Flow<Int> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_SYSTEM
    }

    // Country Code Preferences
    suspend fun saveCountryCodePreference(countryCode: String, alwaysAsk: Boolean) {
        dataStore.edit { preferences ->
            preferences[COUNTRY_CODE_KEY] = countryCode
            preferences[ALWAYS_ASK_KEY] = alwaysAsk
        }
    }

    val countryCodePreference: Flow<String> = dataStore.data.map { preferences ->
        preferences[COUNTRY_CODE_KEY] ?: "+880" // ডিফল্ট কান্ট্রি কোড
    }

    val alwaysAskPreference: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ALWAYS_ASK_KEY] ?: true // ডিফল্টভাবে জিজ্ঞাসা করবে
    }
}