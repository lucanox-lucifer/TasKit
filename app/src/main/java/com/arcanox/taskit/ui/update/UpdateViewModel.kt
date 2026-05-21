package com.arcanox.taskit.ui.update

import android.app.Application
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcanox.taskit.data.local.UpdatePreferenceManager
import com.arcanox.taskit.data.repository.UpdateRepository
import com.arcanox.taskit.data.remote.model.UpdateInfo
import com.arcanox.taskit.data.remote.model.UpdateResponse
import com.arcanox.taskit.util.DownloadHelper
import com.arcanox.taskit.util.NotificationHelper
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateUIState(
    val currentVersionName: String = "",
    val currentVersionCode: Int = 0,
    val isUpToDate: Boolean = true,
    val latestVersionName: String = "",
    val latestVersionCode: Int = -1,
    val changelog: String = "",
    val isMandatory: Boolean = false,
    val apkUrl: String = "",
    val lastCheckedTime: Long = 0,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val error: String? = null,
    val firstDetectedTime: Long = 0,
    val showWhatsNew: Boolean = false,
    val isServiceOnline: Boolean = false,
    val isChecking: Boolean = false,
    val betaUpdates: List<UpdateInfo> = emptyList(),
)

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repository: UpdateRepository,
    private val downloadHelper: DownloadHelper,
    private val notificationHelper: NotificationHelper,
    private val prefs: UpdatePreferenceManager,
    private val gson: Gson,
    application: Application,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUIState())
    val uiState: StateFlow<UpdateUIState> = _uiState.asStateFlow()

    private var currentDownloadId: Long? = null

    private val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
    private val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode
    }
    private val currentVersionName = packageInfo.versionName

    init {
        _uiState.update { it.copy(
            currentVersionName = currentVersionName ?: "Unknown",
            currentVersionCode = currentVersionCode
        ) }
        
        viewModelScope.launch {
            repository.getLocalUpdateData().collect { prefsData ->
                val allUpdates = try {
                    if (prefsData.allUpdatesJson.isNotEmpty()) {
                        gson.fromJson(prefsData.allUpdatesJson, UpdateResponse::class.java).updates
                    } else emptyList()
                } catch (_: Exception) {
                    emptyList()
                }

                val betaUpdates = allUpdates
                    .filter { it.isBeta && (it.versionCode > currentVersionCode) }
                    .sortedByDescending { it.versionCode }

                val isUpToDate = prefsData.latestVersionCode <= currentVersionCode

                _uiState.update { state ->
                    val showWhatsNew = (prefsData.lastSeenVersion != -1) && 
                                      (currentVersionCode > prefsData.lastSeenVersion)
                    
                    if (showWhatsNew) {
                        dismissWhatsNew()
                    }

                    // Auto-download after 7 days if mandatory and not up to date
                    if (!isUpToDate && prefsData.mandatory && prefsData.firstDetectedTime > 0) {
                        val daysDetected = (System.currentTimeMillis() - prefsData.firstDetectedTime) / (1000 * 60 * 60 * 24)
                        if (daysDetected >= 7 && !state.isDownloading) {
                            startDownload(prefsData.apkUrl, prefsData.latestVersionName)
                        }
                    }

                    state.copy(
                        latestVersionName = prefsData.latestVersionName,
                        latestVersionCode = prefsData.latestVersionCode,
                        changelog = prefsData.changelog,
                        isMandatory = prefsData.mandatory,
                        apkUrl = prefsData.apkUrl,
                        lastCheckedTime = prefsData.lastCheckedTime,
                        isUpToDate = isUpToDate,
                        firstDetectedTime = prefsData.firstDetectedTime,
                        showWhatsNew = showWhatsNew,
                        betaUpdates = betaUpdates
                    )
                }
            }
        }
        
        viewModelScope.launch {
            val data = repository.getLocalUpdateData().first()
            if (data.lastSeenVersion == -1) {
                prefs.saveLastSeenVersion(currentVersionCode)
            }
        }
    }

    fun dismissWhatsNew() {
        viewModelScope.launch {
            prefs.saveLastSeenVersion(currentVersionCode)
            _uiState.update { it.copy(showWhatsNew = false) }
        }
    }

    fun checkUpdate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }
            val result = repository.checkUpdate()
            if (result.isSuccess) {
                val update = result.getOrNull()?.updates?.maxByOrNull { it.versionCode }
                if (update != null && update.versionCode > currentVersionCode) {
                    notificationHelper.showUpdateNotification(update.versionName, update.changelog)
                }
            }
            _uiState.update { it.copy(isServiceOnline = result.isSuccess, isChecking = false) }
        }
    }

    fun startDownload(url: String? = null, versionName: String? = null) {
        val finalUrl = url ?: uiState.value.apkUrl
        val finalVersion = versionName ?: uiState.value.latestVersionName
        if (finalUrl.isNotEmpty()) {
            currentDownloadId = downloadHelper.startDownload(finalUrl, "TasKit_$finalVersion.apk")
            _uiState.update { it.copy(isDownloading = true) }
        }
    }

    fun cancelDownload() {
        currentDownloadId?.let { id ->
            downloadHelper.cancelDownload(id)
            currentDownloadId = null
            _uiState.update { it.copy(isDownloading = false) }
        }
    }
}
