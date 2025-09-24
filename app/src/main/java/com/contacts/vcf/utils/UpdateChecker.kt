package com.contacts.vcf.utils

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

@Serializable
data class UpdateInfo(
    @SerialName("latestVersion") val latestVersion: String,
    @SerialName("versionCode") val versionCode: Int,
    @SerialName("releaseNotes") val releaseNotes: String,
    @SerialName("apkUrl") val apkUrl: String
)

interface UpdateApiService {
    @GET("iameffat/contactvcf/master/update.json")
    suspend fun getUpdateInfo(): UpdateInfo
}

object UpdateChecker {
    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val apiService = retrofit.create(UpdateApiService::class.java)

    suspend fun getUpdateInfo(): UpdateInfo? {
        return try {
            apiService.getUpdateInfo()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}