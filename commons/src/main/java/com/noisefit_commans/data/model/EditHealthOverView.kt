package com.noisefit_commans.data.model

data class EditHealthOverView(
    var bloodOxygen: Int = 1,
    var heartRate: Int = 1,
    var steps: Int = 0,
    var distance: Int = 0,
    var sleep: Int = 1,
    var bodyTemp: Int = 1,
    var stress: Int = 1,
    var calories: Int =0,
)

data class HealthOverViewList(val id: Int, val icon: Int, val title: String, var visible: Boolean)