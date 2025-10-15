package com.noisefit.data.model.timeline

import com.google.gson.annotations.SerializedName

data class SupplementsListResponse(
    val options: List<SupplementOption>? = null
)

data class SupplementOption(
    val id: Int? = null,
    val options: String? = null,
    val type: String? = null,
    val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,

    // for app
    var isChecked: Boolean = false,
)