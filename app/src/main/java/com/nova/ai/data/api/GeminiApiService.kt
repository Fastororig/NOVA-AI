package com.nova.ai.data.api

import com.google.gson.Gson
import com.nova.ai.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiService {
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
        .build()

    suspend fun fetchAvailableModels(apiKey: String): Result<List<UiGeminiModel>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(IllegalArgumentException("Ключ пуст."))
        val collected = mutableListOf<GeminiRemoteModelItem>()
        var pageToken: String? = null
        var loops = 0

        try {
            do {
                val urlBuilder = StringBuilder("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
                if (!pageToken.isNullOrBlank()) urlBuilder.append("&pageToken=$pageToken")

                val request = Request.Builder().url(urlBuilder.toString()).get().build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception(parseApiError(response.code, body)))
                }

                val parsed = gson.fromJson(body, GeminiListModelsResponse::class.java)
                parsed?.models?.let { collected.addAll(it) }
                pageToken = parsed?.nextPageToken
                loops++
                if (loops > 10) break
            } while (!pageToken.isNullOrBlank())

            val valid = collected.filter { it.supportedGenerationMethods?.contains("generateContent") == true }
            if (valid.isEmpty()) return@withContext Result.failure(Exception("Нет моделей с generateContent."))

            val hasGemini38 = valid.any { it.cleanId == "gemini-3.8-flash" }
            val uiModels = valid.map { item ->
                val isRec = if (hasGemini38) item.cleanId == "gemini-3.8-flash" else (item.cleanId == "gemini-2.5-flash")
                UiGeminiModel.fromRemote(item, isRec)
            }
            Result.success(uiModels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateContent(
        apiKey: String,
        model: String,
        conversationHistory: List<ChatMessage>,
        systemInstruction: String? = null,
        temperature: Float = 0.7f
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanModel = model.removePrefix("models/").trim()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent?key=$apiKey"
        // Отправка JSON payload...
        Result.success("...")
    }

    private fun parseApiError(statusCode: Int, responseBody: String): String {
        return "Ошибка API (HTTP $statusCode): $responseBody"
    }
}