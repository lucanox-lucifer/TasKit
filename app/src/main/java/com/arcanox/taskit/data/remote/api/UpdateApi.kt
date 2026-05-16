package com.arcanox.taskit.data.remote.api

import com.arcanox.taskit.data.remote.model.UpdateResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface UpdateApi {
    @GET
    suspend fun checkUpdate(@Url url: String): UpdateResponse
}
