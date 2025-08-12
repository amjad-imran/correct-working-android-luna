package com.oreo.data.model.timeline.addActivityTimelineModels

import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum

data class AddActivityListTimelineModel(
    val name: String,
    val key: String,
    val type: AddActivityItemsEnum,
    val titleColor: Int,
)