package com.oreo.ui.customHomeScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomHomescreenViewModel  @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
): BaseViewModel() {

    // Switch state
    val isListVisible = MutableLiveData(false)

    // List of items
    //
    private val _items = MutableLiveData<List<CustomHomeScreenItem>>()
    val items: LiveData<List<CustomHomeScreenItem>> get() = _items

    init {
        loadInitialItems()
    }

    private fun loadInitialItems() {
        viewModelScope.launch {
            // get switch state


            // Replace with your actual data loading logic
            val initialItems = listOf(
                CustomHomeScreenItem(
                    R.drawable.icon_sleep,
                    "sleep",
                    resourcesProvider.getString(R.string.text_sleep),
                    false,
                    1
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_activity,
                    "activity",
                    resourcesProvider.getString(R.string.text_activity_o),
                    false,
                    2
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_readiness,
                    "readiness",
                    resourcesProvider.getString(R.string.text_readiness),
                    false,
                    3
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_sleep_planner,
                    "sleep_planner",
                    resourcesProvider.getString(R.string.text_sleep_planner),
                    false,
                    4
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_heart_rate,
                    "heart_rate",
                    resourcesProvider.getString(R.string.text_heart_rate),
                    false,
                    5
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_heart_monitor,
                    "heart_monitor",
                    resourcesProvider.getString(R.string.text_heart_monitor),
                    false,
                    6
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_daily_goals,
                    "daily_goals",
                    resourcesProvider.getString(R.string.text_daily_goals),
                    false,
                    7
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_luna_ai,
                    "luna_ai",
                    resourcesProvider.getString(R.string.text_luna_ai),
                    false,
                    8
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_cycle_tracker,
                    "cycle_tracker",
                    resourcesProvider.getString(R.string.text_cycle_tracker),
                    false,
                    9
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_7_day_trends_card,
                    "7_day_trends_card",
                    resourcesProvider.getString(R.string.text_7_day_trends_cards),
                    false,
                    10
                ),
                CustomHomeScreenItem(
                    R.drawable.icon_flexibility_training,
                    "workout_history",
                    resourcesProvider.getString(R.string.text_workout_history),
                    false,
                    11
                ),
                // Add more items...
            )
            _items.postValue(initialItems.sortedBy { it.priority })
        }
    }

     fun updateData(updatedList: List<CustomHomeScreenItem>) {
         updatedList.forEachIndexed{ index, cItem ->
             cItem.priority = index+1
         }
        LOGS.d(" $updatedList")
    }
}

    // Swap items for drag-and-drop
//    fun moveItem(fromPosition: Int, toPosition: Int) {
//        _items.value?.let {
//            val temp = it[fromPosition]
//            it.removeAt(fromPosition)
//            it.add(toPosition, temp)
//            _items.value = it
//        }
//    }
