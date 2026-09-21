package com.maykol.controlfamiliar.child.network

import com.maykol.controlfamiliar.child.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/** Cliente HTTP mínimo hacia el backend, sin dependencias de Retrofit. */
internal object HttpClient {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun postJson(path: String, body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.API_BASE_URL + path)
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code} en $path: $responseBody")
            }
            if (responseBody.isBlank()) JSONObject() else JSONObject(responseBody)
        }
    }

    suspend fun getJson(path: String): JSONObject = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(BuildConfig.API_BASE_URL + path).get().build()
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code} en $path: $responseBody")
            }
            if (responseBody.isBlank()) JSONObject() else JSONObject(responseBody)
        }
    }

    suspend fun getJsonArray(path: String): JSONArray = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(BuildConfig.API_BASE_URL + path).get().build()
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code} en $path: $responseBody")
            }
            if (responseBody.isBlank()) JSONArray() else JSONArray(responseBody)
        }
    }
}
