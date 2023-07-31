package com.noisefit.ui.dashboard.summary


import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.data.model.*
import com.noisefit_commans.utils.Event
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Units

data class Summary(
    val healthOverviewData: MutableLiveData<HealthOverviewData> = MutableLiveData<HealthOverviewData>(),
    val recentActivities: MutableLiveData<RecentActivities> = MutableLiveData<RecentActivities>(),
    var unit: Units = Units.METRIC,
    var bodyTemp: Units = Units.METRIC,
    var user: User? = null,
    var loggedIn: Boolean = false,
    var connectedDevice: ColorFitDevice? = null,
    var imageCounter: Int = 0,
    var refreshPosition: Int? = null,
    var bleCallingStatus: Triple<Boolean, String, Boolean>? = null,
    var deviceFeatures: DeviceFeatures? = null,
    val showPromotionalBanner: MutableLiveData<Event<Boolean>> = MutableLiveData<Event<Boolean>>(),
//    var editHealthOverView: EditHealthOverView = EditHealthOverView(),
    var caloriesProgressCompleted: Float = -1f,
    var stepsProgressCompleted: Float = -1f,
    var distanceProgressCompleted: Float = -1f,
)