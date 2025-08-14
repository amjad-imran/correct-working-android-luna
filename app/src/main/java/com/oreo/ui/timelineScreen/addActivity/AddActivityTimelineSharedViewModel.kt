package com.oreo.ui.timelineScreen.addActivity

import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.addLogBottomSheetModels.AddLogBottomSheetDataModels
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddActivityTimelineSharedViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource,
    val localDataStore: DataStoredInterface,
) : BaseViewModel() {

    val loadFragment = MutableLiveData<Event<AddActivityItemsEnum>>()
    val navigateUp = MutableLiveData<Event<Boolean>>()

    fun loadFragmentByType(type: AddActivityItemsEnum) {
        loadFragment.postValue(Event(type))
    }

    fun clearTodayData(){
        GlobalScope.launch(Dispatchers.IO) {
            userHealthDataDataSource.clearDataByDates(listOf(DateFormats.getTodaysDateString(10)))
        }
    }
    fun navigateUp() {
        navigateUp.postValue(Event(true))
    }

    fun getAllActivityListMap() = arrayListOf(
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_caffeine_intake),
            type = AddActivityItemsEnum.CAFFEINE,
            titleColor = "#EEB69F".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_meal_intake),
            type = AddActivityItemsEnum.MEAL,
            titleColor = "#D8D3A3".toColorInt()
        ), AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_light_exposure),
            type = AddActivityItemsEnum.LIGHT_EXPOSURE,
            titleColor = "#F1C48E".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_workout),
            type = AddActivityItemsEnum.WORKOUT,
            titleColor = "#8ED3F1".toColorInt()
        ),
        /*AddActivityListTimelineModel(
            name = getString(R.string.text_water_consumption),
            type = AddActivityItemsEnum.WATER,
            titleColor = "#8EF1C3".toColorInt()
        ),*/
        AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_period).capitalizeWords(),
            type = AddActivityItemsEnum.CYCLE_LOG,
            titleColor = "#F18EBD".toColorInt()
        ),
       /* AddActivityListTimelineModel(
            name = resourcesProvider.getString(R.string.text_nap),
            type = AddActivityItemsEnum.NAP,
            titleColor = "#A8A8ED".toColorInt()
        ),*/
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