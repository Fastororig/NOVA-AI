package com.nova.ai.data.model

import com.google.gson.annotations.SerializedName

data class GeminiListModelsResponse(
    @SerializedName("models") val models: List<GeminiRemoteModelItem>?,
    @SerializedName("nextPageToken") val nextPageToken: String?
)

data class GeminiRemoteModelItem(
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("supportedGenerationMethods") val supportedGenerationMethods: List<String>?,
    @SerializedName("inputTokenLimit") val inputTokenLimit: Long?,
    @SerializedName("outputTokenLimit") val outputTokenLimit: Long?
) {
    val cleanId: String get() = name.removePrefix("models/").trim()
}

enum class ModelFamily(val title: String) {
    FLASH("Семейство Flash"),
    PRO("Семейство Pro"),
    OTHER("Другие модели")
}

data class UiGeminiModel(
    val id: String,
    val displayName: String,
    val description: String,
    val family: ModelFamily,
    val isRecommended: Boolean = false
) {
    companion object {
        fun fromRemote(item: GeminiRemoteModelItem, isRecommended: Boolean): UiGeminiModel {
            val id = item.cleanId
            val nameLower = (item.displayName ?: id).lowercase()
            val family = when {
                nameLower.contains("flash") -> ModelFamily.FLASH
                nameLower.contains("pro") -> ModelFamily.PRO
                else -> ModelFamily.OTHER
            }
            return UiGeminiModel(
                id = id,
                displayName = item.displayName ?: id,
                description = item.description ?: id,
                family = family,
                isRecommended = isRecommended
            )
        }
    }
}