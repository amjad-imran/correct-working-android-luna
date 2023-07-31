package com.noisefit.ui.dashboard.healthOverview.edit

import com.noisefit.R
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.data.model.EditHealthOverView
import com.noisefit_commans.data.model.HealthOverViewList
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


const val MINIMUM_DASHBOARD_ITEMS = 3

@HiltViewModel
class EditDashboardViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {

    private var editHealthOverView = EditHealthOverView()
    private var deviceFeatures: DeviceFeatures? = null

    init {
        deviceFeatures = localDataStore.getDeviceFeatures()
        deviceFeatures?.calorieData = 0
        if(watchesSDK.isCaloriesSupported()){
            deviceFeatures?.calorieData = 1
        }
        editHealthOverView = localDataStore.getEditHealthOverView()

    }

    fun getDashBoardList(): ArrayList<HealthOverViewList> {
        return getDummyDashboardList(
            deviceFeatures!!,
            localDataStore.getEditHealthOverView()
        )
    }

    fun isMinItemsSelected(): Boolean {
        if (deviceFeatures == null) return true

        var selectedCount = 0
        if (editHealthOverView.bloodOxygen == 1 && deviceFeatures!!.bloodOxygen == 1) {
            selectedCount++
        }
        if (editHealthOverView.heartRate == 1 && deviceFeatures!!.heartRate == 1) {
            selectedCount++
        }
        if (editHealthOverView.steps == 1 && deviceFeatures!!.stepsData == 1) {
            selectedCount++
        }
        if (editHealthOverView.distance == 1 && deviceFeatures!!.stepsData == 1) {
            selectedCount++
        }
        if (editHealthOverView.calories == 1 && deviceFeatures!!.stepsData == 1) {
            selectedCount++
        }
        if (editHealthOverView.sleep == 1 && deviceFeatures!!.sleepData == 1) {
            selectedCount++
        }
        if (editHealthOverView.bodyTemp == 1 && deviceFeatures!!.bodyTemperature == 1) {
            selectedCount++
        }
        if (editHealthOverView.stress == 1 && deviceFeatures!!.stressCount == 1) {
            selectedCount++
        }
        return selectedCount >= MINIMUM_DASHBOARD_ITEMS
    }

    fun updateDashboardList() {
        localDataStore.setEditHealthOverView(editHealthOverView)
    }

    fun updateEditHealthOverview(id: Int, isChecked: Boolean) {
        when (id) {
            1 -> {
                if (isChecked) {
                    editHealthOverView.steps = 1
                } else {
                    editHealthOverView.steps = 0
                }
            }
            2 -> {
                if (isChecked) {
                    editHealthOverView.distance = 1
                } else {
                    editHealthOverView.distance = 0
                }
            }
            3 -> {
                if (isChecked) {
                    editHealthOverView.heartRate = 1
                } else {
                    editHealthOverView.heartRate = 0
                }
            }
            4 -> {
                if (isChecked) {
                    editHealthOverView.sleep = 1
                } else {
                    editHealthOverView.sleep = 0
                }
            }
            5 -> {
                if (isChecked) {
                    editHealthOverView.bloodOxygen = 1
                } else {
                    editHealthOverView.bloodOxygen = 0
                }
            }
            6 -> {
                if (isChecked) {
                    editHealthOverView.stress = 1
                } else {
                    editHealthOverView.stress = 0
                }
            }
            7 -> {
                if (isChecked) {
                    editHealthOverView.bodyTemp = 1
                } else {
                    editHealthOverView.bodyTemp = 0
                }
            }
            8 -> {
                if (isChecked) {
                    editHealthOverView.calories = 1
                } else {
                    editHealthOverView.calories = 0
                }
            }
        }
    }

    private fun getDummyDashboardList(
        deviceFeatures: DeviceFeatures,
        editHealthOverView: EditHealthOverView
    ): ArrayList<HealthOverViewList> {
        val editHealthOverViewList = ArrayList<HealthOverViewList>()
        if (deviceFeatures.stepsData == 1) {

            editHealthOverViewList.add(
                HealthOverViewList(
                    1,
                    R.drawable.ic_steps,
                    "Step Count",
                    editHealthOverView.steps == 1
                )
            )
        }

        if (deviceFeatures.stepsData == 1) {
            editHealthOverViewList.add(
                HealthOverViewList(
                    2,
                    R.drawable.ic_distance,
                    "Distance",
                    editHealthOverView.distance == 1
                )
            )
        }

        if (deviceFeatures.calorieData == 1) {
            editHealthOverViewList.add(
                HealthOverViewList(
                    8,
                    R.drawable.ic_calories,
                    "Calories",
                    editHealthOverView.calories == 1
                )
            )
        }

        if (deviceFeatures.heartRate == 1) {
            editHealthOverViewList.add(
                HealthOverViewList(
                    3,
                    R.drawable.ic_heart_rate,
                    "Heart Rate",
                    editHealthOverView.heartRate == 1
                )
            )
        }

        if (deviceFeatures.sleepData == 1) {
            editHealthOverViewList.add(
                HealthOverViewList(
                    4,
                    R.drawable.ic_sleep,
                    "Sleep",
                    editHealthOverView.sleep == 1
                )
            )
        }

        if (deviceFeatures.bloodOxygen == 1) {
            editHealthOverViewList.add(
                HealthOverViewList(
                    5,
                    R.drawable.ic_sp_o_2,
                    "Blood Oxygen",
                    editHealthOverView.bloodOxygen == 1
                )
            )
        }

        if (deviceFeatures.stressCount == 1) {
            editHealthOverViewList.add(
                HealthOverViewList(
                    6,
                    R.drawable.ic_stress,
                    "Stress",
                    editHealthOverView.stress == 1
                )
            )
        }

        if (deviceFeatures.bodyTemperature == 1) {
            editHealthOverViewList.add(
                HealthOverViewList(
                    7,
                    R.drawable.ic_body_temp,
                    "Temperature",
                    editHealthOverView.bodyTemp == 1
                )
            )
        }



        return editHealthOverViewList
    }
}