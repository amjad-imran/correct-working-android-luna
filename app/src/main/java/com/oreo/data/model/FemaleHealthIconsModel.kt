package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class FemaleHealthIconsModel(
    @SerializedName("symptoms")
    val symptoms: List<FHSymptomsIconsModel>? = null,
    @SerializedName("flow")
    val flow: List<FHFlowIconsModel>? = null,
)

data class FHSymptomsIconsModel(
    val id: Int? = null,
    @SerializedName("symptom_name")
    val symptomName: String? = null,
    @SerializedName("symptom_short_name")
    val symptomShortName: String? = null,
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("type")
    val type: String? = null,
    var isChecked: Boolean = false
)

data class FHFlowIconsModel(
    val id: Int? = null,
    @SerializedName("symptom_name")
    val symptomName: String? = null,
    @SerializedName("symptom_short_name")
    val symptomShortName: String? = null,
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("type")
    val type: String? = null,
    var isChecked: Boolean = false
)

data class FemaleHealthSymptoms(
    @SerializedName("symptoms")
    val symptoms: List<FHSymptomsIconsModel>? = null,
    @SerializedName("flow_type")
    val flow: FHFlowIconsModel? = null,
)

