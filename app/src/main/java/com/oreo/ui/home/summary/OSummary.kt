package com.oreo.ui.home.summary

import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.data.model.User
import com.noisefit_commans.models.Units
import com.oreo.data.model.OHealthOverview

data class OSummary(
    val healthOverviewData: MutableLiveData<ArrayList<OHealthOverview>> = MutableLiveData<ArrayList<OHealthOverview>>(),
    var unit: Units = Units.METRIC,
    var bodyTemp: Units = Units.METRIC,
    var user: User? = null,
    var refreshPosition: Int? = -1

)