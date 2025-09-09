package com.oreo.ui.customHomeScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.data.model.customHomeScreen.CustomHomeScreenModel
import com.noisefit_commans.data.model.customHomeScreen.CustomHomeScreenNetworkItem
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CustomHomescreenViewModel @Inject constructor(
    private val resourceProvider: ResourcesProvider,
    private val userRepository: UserRepository,
    private val localDataSource: DataStoredInterface,
    private val sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
) : BaseViewModel() {

    // Switch state
    val lunaManagedState = MutableLiveData<Boolean?>(false)

    // List of items
    private val _items = MutableLiveData<List<CustomHomeScreenItem>>()
    val items: LiveData<List<CustomHomeScreenItem>> get() = _items

    val dataUpdated = MutableLiveData<Event<Boolean>>()

    init {
        loadInitialItems()
    }

    private fun loadInitialItems() {
        viewModelScope.launch {

            val itemsMap = getItemsMap()
            val localData = localDataSource.getCustomHomeScreenItemsPriorityList()
            if (localData != null) {
                lunaManagedState.postValue(localData.manage)
            } else {
                lunaManagedState.postValue(true)
            }
            val sortedList = localData?.cards
            if (sortedList != null) {
                sortedList.sortedBy { it.priority }
                val tempList = ArrayList<CustomHomeScreenItem>()
                sortedList.forEach { item ->

                    val card = itemsMap[item.type]
                    card?.let {
                        it.priority = item.priority
                        it.switchState = item.switchState
                        tempList.add(card)
                    }


                }
                val newList = addOtherCards(tempList)
                _items.postValue(newList)
            } else {
                val initialItems = itemsMap.values.toList().sortedBy { it.priority }
                _items.postValue(initialItems)
            }
        }
    }

    fun addOtherCards(card: List<CustomHomeScreenItem>): List<CustomHomeScreenItem> {
        val itemsMap = getItemsMap()
        val existingIds = card.map { it.key }.toSet()

        val remainingCards = itemsMap.values.filter { it.key !in existingIds }

        val cardsToAdd = ArrayList<CustomHomeScreenItem>()
        cardsToAdd.addAll(card)
        remainingCards.forEachIndexed { index,item ->
            cardsToAdd.add(item.apply {
                this.priority = card.size + (index + 1)
            })
        }

        //handle new cards
        // caffeine_intake
        val caffeineCard = card.find { it.key.equals("caffeine_intake",true) }
        if(caffeineCard==null){
            val index = cardsToAdd.indexOfFirst { it.key.equals("caffeine_intake",true) }
            if(index!=-1){
                cardsToAdd[index].switchState = true
            }
        }

        val circadianCard = card.find { it.key.equals("circadian_alignment",true) }
        if(circadianCard==null){
            val index = cardsToAdd.indexOfFirst { it.key.equals("circadian_alignment",true) }
            if(index!=-1){
                cardsToAdd[index].switchState = true
            }
        }


        return cardsToAdd
    }

    fun updateData(isToggleOn: Boolean, updatedList: List<CustomHomeScreenItem>) {
        viewModelScope.launch {

            if (!canMakeApiCall()) {
                lunaManagedState.postValue(lunaManagedState.value)
                return@launch
            }

            var count = 0
            var sleepItemIndex = -1
            updatedList.forEachIndexed { index, cItem ->
                cItem.priority = index + 1
                if (cItem.switchState.not()) count++
                if (cItem.key == "sleep") sleepItemIndex = index
            }

            if (count == updatedList.size) {
                updatedList.get(sleepItemIndex).switchState = true
            }

            val customHomeScreenData = CustomHomeScreenModel(
                manage = isToggleOn,
                type = "home-dash",
                cards = getNetworkList(updatedList)
            )

            userRepository.submitCustomHomeScreenPriority(customHomeScreenData)
                .collect { resource ->
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
                                localDataSource.setCustomHomeScreenApiCallTimeStamps()

                                localDataSource.setCustomHomeScreenItemsPriorityList(
                                    customHomeScreenData
                                )
                                lunaManagedState.postValue(isToggleOn)
                                dataUpdated.postValue(Event(true))
                            }
                        }
                    }
                }
        }
    }

    private fun canMakeApiCall(): Boolean {
        val (lastCallTime, callCount) = localDataSource.getCustomHomeScreenApiCallTimeStamps()
            ?: return true

        if (callCount < 5) return true

        val now = ZonedDateTime.now().toEpochSecond()
        val elapsed = now - lastCallTime

        return if (elapsed > 3600) {
            localDataSource.clearCustomHomeScreenApiCallTimeStamps()
            true
        } else {
            val remaining = 3600 - elapsed
            val minutes = (remaining % 3600) / 60
            val seconds = remaining % 60

            sendMessage(
                resourceProvider.getString(
                    R.string.text_please_try_again_after_mins_seconds,
                    minutes,
                    seconds
                )
            )

            sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.customize_homescreen_warning)

            false
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

    private fun getItemsMap(): Map<String, CustomHomeScreenItem> =
        HashMap<String, CustomHomeScreenItem>().apply {

            this["circadian_alignment"] = CustomHomeScreenItem(
                R.drawable.icon_circadian_alignment,
                "circadian_alignment",
                resourceProvider.getString(R.string.text_circadian_alignment),
                true,
                this.size+1
            )

            this["caffeine_intake"] = CustomHomeScreenItem(
                R.drawable.icon_caffeine_intake,
                "caffeine_intake",
                resourceProvider.getString(R.string.text_caffeine_window),
                true,
                this.size+1
            )

            this["sleep"] = CustomHomeScreenItem(
                R.drawable.icon_sleep,
                "sleep",
                resourceProvider.getString(R.string.text_sleep),
                true,
                this.size+1
            )

            this["activity"] = CustomHomeScreenItem(
                R.drawable.icon_activity,
                "activity",
                resourceProvider.getString(R.string.text_activity_o),
                true,
                this.size+1
            )
            this["readiness"] = CustomHomeScreenItem(
                R.drawable.icon_readiness,
                "readiness",
                resourceProvider.getString(R.string.text_readiness),
                true,
                this.size+1
            )
            this["sleep_planner"] = CustomHomeScreenItem(
                R.drawable.icon_sleep_planner,
                "sleep_planner",
                resourceProvider.getString(R.string.text_sleep_planner),
                true,
                this.size+1
            )

            if(getGeneration(ringDataStore.getRingDevice()?.ringInfo?.serialNoRaw)==1) {
                this["heart_rate"] = CustomHomeScreenItem(
                    R.drawable.icon_heart_rate,
                    "heart_rate",
                    resourceProvider.getString(R.string.text_heart_rate),
                    true,
                    this.size + 1
                )
            }

//        this["health_monitor"] = CustomHomeScreenItem(
//            R.drawable.icon_heart_monitor,
//            "health_monitor",
//            resourceProvider.getString(R.string.text_heart_monitor),
//            true,
//            7
//        )
            this["daily_goals"] = CustomHomeScreenItem(
                R.drawable.icon_daily_goals,
                "daily_goals",
                resourceProvider.getString(R.string.text_daily_goals),
                true,
                this.size+1
            )
            this["luna_ai"] = CustomHomeScreenItem(
                R.drawable.icon_luna_ai,
                "luna_ai",
                resourceProvider.getString(R.string.text_luna_ai),
                true,
                this.size+1
            )

            if(getGeneration(ringDataStore.getRingDevice()?.ringInfo?.serialNoRaw)==1) {
                this["stress"] = CustomHomeScreenItem(
                    R.drawable.icon_stress,
                    "stress",
                    resourceProvider.getString(R.string.text_stress),
                    true,
                    this.size + 1
                )
            }

            if (shouldShowFemaleHealth()) {
                this["cycle_tracker"] = CustomHomeScreenItem(
                    R.drawable.icon_cycle_tracker,
                    "cycle_tracker",
                    resourceProvider.getString(R.string.text_cycle_tracker),
                    true,
                    this.size+1
                )
            }
            this["7_day_trends_card"] = CustomHomeScreenItem(
                R.drawable.icon_7_day_trends_card,
                "7_day_trends_card",
                resourceProvider.getString(R.string.text_7_day_trends_cards),
                true,
                this.size+1
            )
            /*this["workout_history"] = CustomHomeScreenItem(
                R.drawable.icon_flexibility_training,
                "workout_history",
                resourceProvider.getString(R.string.text_workout_history),
                true,
                this.size+1
            )*/

        }

    fun shouldShowFemaleHealth(): Boolean {
        val user = localDataSource.getUser()
        return user?.userInfo?.gender.equals("female", true)
    }

    private fun getGeneration(serialNoRaw: String?): Int {
        if (serialNoRaw == null) return 1

        return try {
            serialNoRaw.substring(1, 2).toInt()
        } catch (exp: Exception) {
            exp.printStackTrace()
            1
        }
    }

}
