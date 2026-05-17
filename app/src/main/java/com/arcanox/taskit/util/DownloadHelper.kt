package com.arcanox.taskit.util

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun startDownload(url: String, fileName: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setTitle("Downloading TasKit Update")
            .setDescription("New version is being downloaded")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        return downloadManager.enqueue(request)
    }

    fun getDownloadStatus(downloadId: Long): Int {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        var status = -1
        if ((cursor != null) && cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex != -1) {
                status = cursor.getInt(statusIndex)
            }
        }
        cursor?.close()
        return status
    }

    fun cancelDownload(downloadId: Long) {
        downloadManager.remove(downloadId)
    }
}
