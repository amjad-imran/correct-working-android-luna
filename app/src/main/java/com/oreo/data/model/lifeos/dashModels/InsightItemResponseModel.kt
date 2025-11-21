package com.oreo.data.model.lifeos.dashModels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InsightItemResponseModel(
    val description: String ?= null,
    val graph: List<Graph> ?= null,
    val graphs: String ?= null,
    val related_suggested_questions: List<String> ?= null,
    val relevancy: Double ?= null,
    val suggestions: String ?= null,
    val title: String ?= null
) : Parcelable