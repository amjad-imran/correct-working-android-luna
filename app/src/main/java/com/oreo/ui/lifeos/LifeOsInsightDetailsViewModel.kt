package com.oreo.ui.lifeos

import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LifeOsInsightDetailsViewModel @Inject constructor(

): BaseViewModel() {


    var insightData: InsightItemResponseModel ?= null
}