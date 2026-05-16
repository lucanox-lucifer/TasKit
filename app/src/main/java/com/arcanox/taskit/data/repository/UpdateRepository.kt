package com.arcanox.taskit.data.repository

import com.arcanox.taskit.data.local.UpdatePreferenceManager
import com.arcanox.taskit.data.remote.api.UpdateApi
import com.arcanox.taskit.data.remote.model.UpdateResponse
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val api: UpdateApi,
    private val prefs: UpdatePreferenceManager
) {
    // Change the updateJsonUrl to point to your master branch
    private val updateJsonUrl = "https://raw.githubusercontent.com/lucanox-lucifer/TasKit/master/ota/update.json" // Placeholder

    suspend fun checkUpdate(): Result<UpdateResponse> {
        return try {
            val response = api.checkUpdate(updateJsonUrl)
            prefs.saveUpdate(
                versionCode = response.versionCode,
                versionName = response.versionName,
                changelog = response.changelog,
                mandatory = response.mandatory,
                apkUrl = response.apkUrl
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLocalUpdateData() = prefs.updateData

    suspend fun ignoreVersion(versionCode: Int) = prefs.ignoreVersion(versionCode)
}
