package com.arcanox.taskit.data.remote.model

import com.google.gson.annotations.SerializedName

data class UpdateResponse(
    @SerializedName("updates") val updates: List<UpdateInfo>
)

data class UpdateInfo(
    @SerializedName("versionCode") val versionCode: Int,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("apkUrl") val apkUrl: String,
    @SerializedName("changelog") val changelog: String,
    @SerializedName("releaseDate") val releaseDate: String
) {
    val isBeta: Boolean get() = versionName.contains("-beta", ignoreCase = true)
    val isRelease: Boolean get() = !isBeta
}
