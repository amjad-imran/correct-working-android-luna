package com.oreo.ui.customHomeScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.data.model.customHomeScreen.CustomHomeScreenModel
import com.noisefit_commans.data.model.customHomeScreen.CustomHomeScreenNetworkItem
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomHomescreenViewModel  @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val userRepository: UserRepository,
    private val localDatSource: DataStoredInterface
): BaseViewModel() {

    // Switch state
    val lunaManagedState = MutableLiveData<Boolean?>(false)

    // List of items
    //
    private val _items = MutableLiveData<List<CustomHomeScreenItem>>()
    val items: LiveData<List<CustomHomeScreenItem>> get() = _items

    val dataUpdated= MutableLiveData<Event<Boolean>>()


    init {
        loadInitialItems()
    }

    private fun loadInitialItems() {
        viewModelScope.launch {
            val itemsMap = getItemsMap()
            val localData = localDatSource.getCustomHomeScreenItemsPriorityList()
            if (localData != null){
                lunaManagedState.postValue(localData.manage)
            }else{
                lunaManagedState.postValue(true)
            }
            val sortedList = localData?.cards
            if (sortedList != null){
                sortedList.sortedBy { it.priority }
                val tempList= ArrayList<CustomHomeScreenItem>()
                sortedList.forEach { item ->

                    val card = itemsMap[item.type]
                    card?.let {
                        it.priority = item.priority
                        it.switchState = item.switchState
                        tempList.add(card)
                    }
                    _items.postValue(tempList)
                }
            }else{
                val initialItems = itemsMap.values.toList().sortedBy { it.priority }
                _items.postValue(initialItems)
            }
        }
    }

     fun updateData(isToggleOn: Boolean, updatedList: List<CustomHomeScreenItem>) {
         viewModelScope.launch {
             updatedList.forEachIndexed{ index, cItem ->
                 cItem.priority = index+1
             }

             val customHomeScreendata = CustomHomeScreenModel(
                 manage = isToggleOn,
                 type = "home-dash",
                 cards = getNetworkList(updatedList)
             )

             userRepository.submitCustomHomeScreenPriority(customHomeScreendata).collect { resource ->
                 when (resource) {
                     is Resource.GenericError -> {
                         sendMessage(resource.message)
                     }

                     is Resource.Loading -> {
                         setLoading(resource.loading)
                     }

                     is Resource.NetworkError -> {
                         setApiErrors(resource.response.apply {
                             (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                 object : BinaryActionCallback {
                                     override fun yes() {
                                         updateData(isToggleOn, updatedList)
                                     }

                                     override fun no() {}
                                 }
                         })
                     }

                     is Resource.Success -> {
                         resource.data?.data?.let {
                             localDatSource.setCustomHomeScreenItemsPriorityList(customHomeScreendata)
                             dataUpdated.postValue(Event(true))
                         }
                     }
                 }
             }
         }
    }

    private fun getNetworkList(updatedList: List<CustomHomeScreenItem>): List<CustomHomeScreenNetworkItem> {
        return updatedList.map {
            CustomHomeScreenNetworkItem(
                type = it.key,
                switchState = it.switchState,
                priority = it.priority
            )
        }
    }

    private fun getItemsMap(): Map<String, CustomHomeScreenItem> = HashMap<String, CustomHomeScreenItem>().apply {
            this["sleep"] = CustomHomeScreenItem(
                R.drawable.icon_sleep,
                "sleep",
                resourcesProvider.getString(R.string.text_sleep),
                true,
                1
            )

            this["activity"] = CustomHomeScreenItem(
                R.drawable.icon_activity,
                "activity",
                resourcesProvider.getString(R.string.text_activity_o),
                true,
                2
            )
            this["readiness"] = CustomHomeScreenItem(
                R.drawable.icon_readiness,
                "readiness",
                resourcesProvider.getString(R.string.text_readiness),
                true,
                3
            )
            this["sleep_planner"] = CustomHomeScreenItem(
                R.drawable.icon_sleep_planner,
                "sleep_planner",
                resourcesProvider.getString(R.string.text_sleep_planner),
                true,
                4
            )
            this["heart_rate"] = CustomHomeScreenItem(
                R.drawable.icon_heart_rate,
                "heart_rate",
                resourcesProvider.getString(R.string.text_heart_rate),
                true,
                5
            )
            this["health_monitor"] = CustomHomeScreenItem(
                R.drawable.icon_heart_monitor,
                "health_monitor",
                resourcesProvider.getString(R.string.text_heart_monitor),
                true,
                6
            )
            this["daily_goals"] = CustomHomeScreenItem(
                R.drawable.icon_daily_goals,
                "daily_goals",
                resourcesProvider.getString(R.string.text_daily_goals),
                true,
                7
            )
            this["luna_ai"] = CustomHomeScreenItem(
                R.drawable.icon_luna_ai,
                "luna_ai",
                resourcesProvider.getString(R.string.text_luna_ai),
                true,
                8
            )
            this["cycle_tracker"] = CustomHomeScreenItem(
                R.drawable.icon_cycle_tracker,
                "cycle_tracker",
                resourcesProvider.getString(R.string.text_cycle_tracker),
                true,
                9
            )
            this["7_day_trends_card"] = CustomHomeScreenItem(
                R.drawable.icon_7_day_trends_card,
                "7_day_trends_card",
                resourcesProvider.getString(R.string.text_7_day_trends_cards),
                true,
                10
            )
            this["workout_history"] = CustomHomeScreenItem(
                R.drawable.icon_flexibility_training,
                "workout_history",
                resourcesProvider.getString(R.string.text_workout_history),
                true,
                11
            )

            this["stress"] = CustomHomeScreenItem(
                R.drawable.icon_flexibility_training,
                "stress",
                resourcesProvider.getString(R.string.text_stress),
                true,
                12
            )

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
