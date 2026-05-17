package com.arcanox.taskit.data.repository

import com.arcanox.taskit.data.local.UpdatePreferenceManager
import com.arcanox.taskit.data.remote.api.UpdateApi
import com.arcanox.taskit.data.remote.model.UpdateResponse
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val api: UpdateApi,
    private val prefs: UpdatePreferenceManager,
    private val gson: Gson,
) {
    // Change the updateJsonUrl to point to your master branch
    private val updateJsonUrl = "https://raw.githubusercontent.com/lucanox-lucifer/TasKit/master/ota/update.json" // Placeholder

    suspend fun checkUpdate(): Result<UpdateResponse> {
        return try {
            val response = api.checkUpdate(updateJsonUrl)
            
            // Find the latest release (non-beta) update
            val latestRelease = response.updates.asSequence()
                .filter { it.isRelease }
                .maxByOrNull { it.versionCode }

            if (latestRelease != null) {
                // All release updates are mandatory within 7 days
                prefs.saveUpdate(
                    versionCode = latestRelease.versionCode,
                    versionName = latestRelease.versionName,
                    changelog = latestRelease.changelog,
                    mandatory = true,
                    apkUrl = latestRelease.apkUrl,
                )
            } else {
                // If no release update is found, check if there's any update at all
                response.updates.maxByOrNull { it.versionCode }?.let { absoluteLatest ->
                    prefs.saveUpdate(
                        versionCode = absoluteLatest.versionCode,
                        versionName = absoluteLatest.versionName,
                        changelog = absoluteLatest.changelog,
                        mandatory = false, // Betas are never mandatory
                        apkUrl = absoluteLatest.apkUrl,
                    )
                }
            }
            
            // Save all updates for the list view
            prefs.saveAllUpdates(gson.toJson(response))

            Result.success(response)
        } catch (_: Exception) {
            Result.failure(Exception("Failed to fetch updates"))
        }
    }

    fun getLocalUpdateData() = prefs.updateData
}
