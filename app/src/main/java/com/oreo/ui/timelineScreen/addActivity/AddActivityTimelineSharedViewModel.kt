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
) : BaseViewModel() {

    val loadFragment = MutableLiveData<Event<AddActivityItemsEnum>>()
    val navigateUp = MutableLiveData<Event<Boolean>>()

    fun loadFragmentByType(type: AddActivityItemsEnum) {
        loadFragment.postValue(Event(type))
    }

    fun navigateUp() {
        navigateUp.postValue(Event(true))
    }



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