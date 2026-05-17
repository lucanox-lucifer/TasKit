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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "update_prefs")

@Singleton
class UpdatePreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val FIRST_DETECTED_TIME = longPreferencesKey("first_detected_time")
        private val LAST_CHECKED_TIME = longPreferencesKey("last_checked_time")
        private val LATEST_VERSION_CODE = intPreferencesKey("latest_version_code")
        private val LATEST_VERSION_NAME = stringPreferencesKey("latest_version_name")
        private val CHANGELOG = stringPreferencesKey("changelog")
        private val MANDATORY = booleanPreferencesKey("mandatory")
        private val APK_URL = stringPreferencesKey("apk_url")
        private val LAST_SEEN_VERSION = intPreferencesKey("last_seen_version")
        private val ALL_UPDATES_JSON = stringPreferencesKey("all_updates_json")
    }

    val updateData: Flow<UpdatePrefs> = context.dataStore.data.map { preferences ->
        UpdatePrefs(
            firstDetectedTime = preferences[FIRST_DETECTED_TIME] ?: 0L,
            lastCheckedTime = preferences[LAST_CHECKED_TIME] ?: 0L,
            latestVersionCode = preferences[LATEST_VERSION_CODE] ?: -1,
            latestVersionName = preferences[LATEST_VERSION_NAME] ?: "",
            changelog = preferences[CHANGELOG] ?: "",
            mandatory = preferences[MANDATORY] ?: false,
            apkUrl = preferences[APK_URL] ?: "",
            lastSeenVersion = preferences[LAST_SEEN_VERSION] ?: -1,
            allUpdatesJson = preferences[ALL_UPDATES_JSON] ?: "",
        )
    }

    suspend fun saveAllUpdates(json: String) {
        context.dataStore.edit { it[ALL_UPDATES_JSON] = json }
    }

    suspend fun saveLastSeenVersion(version: Int) {
        context.dataStore.edit { it[LAST_SEEN_VERSION] = version }
    }

    suspend fun saveUpdate(
        versionCode: Int,
        versionName: String,
        changelog: String,
        mandatory: Boolean,
        apkUrl: String
    ) {
        context.dataStore.edit { preferences ->
            val currentLatest = preferences[LATEST_VERSION_CODE] ?: -1
            if (versionCode > currentLatest) {
                preferences[LATEST_VERSION_CODE] = versionCode
                preferences[LATEST_VERSION_NAME] = versionName
                preferences[CHANGELOG] = changelog
                preferences[MANDATORY] = mandatory
                preferences[APK_URL] = apkUrl
                // Reset/Set the first detected time for the new version
                preferences[FIRST_DETECTED_TIME] = System.currentTimeMillis()
            }
            preferences[LAST_CHECKED_TIME] = System.currentTimeMillis()
        }
    }
}

data class UpdatePrefs(
    val firstDetectedTime: Long,
    val lastCheckedTime: Long,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val changelog: String,
    val mandatory: Boolean,
    val apkUrl: String,
    val lastSeenVersion: Int,
    val allUpdatesJson: String
)
