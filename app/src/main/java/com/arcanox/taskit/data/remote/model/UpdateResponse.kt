package com.arcanox.taskit.data.remote.model

import com.google.gson.annotations.SerializedName

data class UpdateResponse(
    @SerializedName("versionCode") val versionCode: Int,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("apkUrl") val apkUrl: String,
    @SerializedName("changelog") val changelog: String,
    @SerializedName("forceUpdateAfterDays") val forceUpdateAfterDays: Int,
    @SerializedName("releaseDate") val releaseDate: String,
    @SerializedName("mandatory") val mandatory: Boolean
)
