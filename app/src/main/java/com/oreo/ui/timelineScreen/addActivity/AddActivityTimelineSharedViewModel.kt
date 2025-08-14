package com.oreo.ui.timelineScreen.addActivity

import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.addLogBottomSheetModels.AddLogBottomSheetDataModels
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddActivityTimelineSharedViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    val loadFragment = MutableLiveData<Event<AddActivityItemsEnum>>()
    val navigateUp = MutableLiveData<Event<Boolean>>()

    fun loadFragmentByType(type: AddActivityItemsEnum) {
        loadFragment.postValue(Event(type))
    }

    fun navigateUp() {
        navigateUp.postValue(Event(true))
    }

    fun getAllActivityListMap() = arrayListOf(
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_meal_intake),
            type = AddActivityItemsEnum.MEAL,
            titleColor = "#FFE3B2".toColorInt()
        ), AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_light_exposure),
            type = AddActivityItemsEnum.LIGHT_EXPOSURE,
            titleColor = "#FFE1CF".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_caffeine_intake),
            type = AddActivityItemsEnum.CAFFEINE,
            titleColor = "#DCA58E".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_workout),
            type = AddActivityItemsEnum.WORKOUT,
            titleColor = "#78C3F9".toColorInt()
        ),
        /*AddActivityListTimelineModel(
            name = getString(R.string.text_water_consumption),
            type = AddActivityItemsEnum.WATER,
            titleColor = "#8EF1C3".toColorInt()
        ),*/
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_period_started),
            type = AddActivityItemsEnum.CYCLE_LOG,
            titleColor = "#F18EBD".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_nap),
            type = AddActivityItemsEnum.NAP,
            titleColor = "#A8A8ED".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_sleep),
            type = AddActivityItemsEnum.SLEEP,
            titleColor = "#C5A8ED".toColorInt()
        ),
    )



}

enum class AddActivityItemsEnum {
    ACTIVITIES_LISTING,
    MEAL,
    LIGHT_EXPOSURE,
    WORKOUT,
    CAFFEINE,
    WATER,
    CYCLE_LOG,
    NAP,
    SLEEP,
}