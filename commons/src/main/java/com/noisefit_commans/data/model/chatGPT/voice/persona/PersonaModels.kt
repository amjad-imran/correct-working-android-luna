package com.noisefit_commans.data.model.chatGPT.voice.persona

import com.google.gson.annotations.SerializedName

data class ItemPersonaVoiceResponse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("persona_type") val personaType: String? = null,
    @SerializedName("persona_title") val personaTitle: String? = null,
    @SerializedName("persona_features") val personaFeatures: List<String>? = null,
    @SerializedName("img_url") val imgUrl: String? = null,
    @SerializedName("voice_url") val voiceUrl: String? = null,
    @SerializedName("persona_ai") val persona_ai: String? = null
)