package com.arcanox.taskit.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

@Singleton
class SettingsPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        private val AUTO_UPDATE_ENABLED = booleanPreferencesKey("auto_update_enabled")
        private val APP_THEME = stringPreferencesKey("app_theme")
    }

    val settingsData: Flow<SettingsData> = context.dataStore.data.map { preferences ->
        SettingsData(
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            analyticsEnabled = preferences[ANALYTICS_ENABLED] ?: false,
            autoUpdateEnabled = preferences[AUTO_UPDATE_ENABLED] ?: true,
            appTheme = preferences[APP_THEME] ?: "Dark"
        )
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun saveAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[ANALYTICS_ENABLED] = enabled }
    }

    suspend fun saveAutoUpdateEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_UPDATE_ENABLED] = enabled }
    }

    suspend fun saveAppTheme(theme: String) {
        context.dataStore.edit { it[APP_THEME] = theme }
    }
}

data class SettingsData(
    val notificationsEnabled: Boolean,
    val analyticsEnabled: Boolean,
    val autoUpdateEnabled: Boolean,
    val appTheme: String
)
