package com.oreo.ui.timelineScreen.addActivity

import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.addLogBottomSheetModels.AddLogBottomSheetDataModels
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddActivityTimelineSharedViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
) : BaseViewModel() {

    val allActivitiesList: ArrayList<AddActivityListTimelineModel> = ArrayList()

    val selectedActivity = MutableLiveData<AddActivityItemsEnum?>()
    val selectedActivityInitData = MutableLiveData<ArrayList<AddLogBottomSheetDataModels>?>()

    fun initActivityListData(){
        allActivitiesList.addAll(getAllActivityListMap().values.toList())
    }

    fun getAllActivityListMap() = hashMapOf(
        AddActivityItemsEnum.MEAL_INTAKE to AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_meal_intake),
            key = "meal",
            type = AddActivityItemsEnum.MEAL_INTAKE,
            titleColor = "#FFE3B2".toColorInt()
        ),
        AddActivityItemsEnum.LIGHT_EXPOSURE to AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_light_exposure),
            key = "light-exposure",
            type = AddActivityItemsEnum.LIGHT_EXPOSURE,
            titleColor = "#FFE1CF".toColorInt()
        ),
        AddActivityItemsEnum.CAFFEINE_INTAKE to AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_caffeine_intake),
            key = "caffeine",
            type = AddActivityItemsEnum.CAFFEINE_INTAKE,
            titleColor = "#DCA58E".toColorInt()
        ),
        AddActivityItemsEnum.WORKOUT to AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_workout),
            key = "workout",
            type = AddActivityItemsEnum.WORKOUT,
            titleColor = "#78C3F9".toColorInt()
        ),
        AddActivityItemsEnum.WATER_CONSUMPTION to AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_water_consumption),
            key = "hydration",
            type = AddActivityItemsEnum.WATER_CONSUMPTION,
            titleColor = "#8EF1C3".toColorInt()
        ),
        AddActivityItemsEnum.PERIOD_STARTED to AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_period_started),
            key = "period",
            type = AddActivityItemsEnum.PERIOD_STARTED,
            titleColor = "#F18EBD".toColorInt()
        ),
        AddActivityItemsEnum.NAP to AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_nap),
            key = "nap",
            type = AddActivityItemsEnum.NAP,
            titleColor = "#A8A8ED".toColorInt()
        )
    )

    fun initSpecificActivityData(type: AddActivityItemsEnum){
        val dataList: ArrayList<AddLogBottomSheetDataModels> = ArrayList()
        when(type){
            AddActivityItemsEnum.MEAL_INTAKE -> {
                dataList.add(AddLogBottomSheetDataModels.MealIntakeAddLogModel(
                    resourcesProvider.getString(
                        R.string.text_meal_val,
                        1
                    )
                ))
            }
            AddActivityItemsEnum.LIGHT_EXPOSURE -> {}
            AddActivityItemsEnum.CAFFEINE_INTAKE -> {}
            AddActivityItemsEnum.EXERCISE_DURATION -> {}
            AddActivityItemsEnum.WATER_CONSUMPTION -> {}
            AddActivityItemsEnum.PERIOD_STARTED -> {}
            AddActivityItemsEnum.NAP -> {}
            AddActivityItemsEnum.WORKOUT -> {}
        }

        selectedActivityInitData.postValue(dataList)
    }

    fun clearData() {
        allActivitiesList.clear()
        selectedActivity.value = null
        selectedActivityInitData.value = null
    }
}

enum class AddActivityItemsEnum{
    MEAL_INTAKE,
    LIGHT_EXPOSURE,
    CAFFEINE_INTAKE,
    EXERCISE_DURATION,
    WATER_CONSUMPTION,
    PERIOD_STARTED,
    NAP,
    WORKOUT,
}