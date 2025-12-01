package com.oreo.data.model.lifeos.dashModels

data class LifeOsWhatsNewResponse(
    val version: Float ?= null,
    val whatsNewList: List<String> ?= null,
)