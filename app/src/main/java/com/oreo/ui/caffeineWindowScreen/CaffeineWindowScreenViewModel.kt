package com.oreo.ui.caffeineWindowScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CaffeineWindowData
import com.oreo.ui.custom.HighlightState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CaffeineWindowScreenViewModel @Inject constructor(
    private val resourceProvider: ResourcesProvider,
    private val userRepository: UserRepository,
    private val localDataSource: DataStoredInterface
): BaseViewModel(){

    val rvDisplayAllItemsToggleState: MutableLiveData<Boolean> = MutableLiveData(true)

    val _myItemsList = MutableLiveData<ArrayList<CaffeineFoodItem>>()

    val _allItemsList = MutableLiveData<ArrayList<CaffeineFoodItem>>()

    val dataUpdated= MutableLiveData<Event<Boolean>>()

    fun loadItems() {
        viewModelScope.launch {

            userRepository.getCaffeineWindowItemsList().collect{ resource ->
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

                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            justLoadIt(it)
                            dataUpdated.postValue(Event(true))
                        }
                    }
                }
            }

        }
    }

    fun justLoadIt(mainList: List<CaffeineFoodItem>){
        val allItems = ArrayList<CaffeineFoodItem>()
        val myItems = ArrayList<CaffeineFoodItem>()
        LOGS.d("caffeineData: $mainList")
        mainList.forEach {
            if(it.is_favorite == true){
                myItems.add(it)
            }else{
                allItems.add(it)
            }
        }

        _myItemsList.postValue(myItems)
        _allItemsList.postValue(allItems)
    }

    fun hitPostApiToUpdateItems(postData: CaffeinePostApiModel){
        viewModelScope.launch {
            userRepository.updateCaffeineItemsList(postData).collect{ resource ->

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

                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            /*localDataSource.setCustomHomeScreenApiCallTimeStamps()

                            localDataSource.setCustomHomeScreenItemsPriorityList(customHomeScreenData)
                            dataUpdated.postValue(Event(true))*/
                        }
                    }
                }

            }
        }
    }

    fun getMaxQuantity(data: CaffeineWindowData): Int? {

        val now = LocalTime.now()

        val graphStart = LocalTime.parse(data.wakeUpTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        val  graphEnd = LocalTime.parse(data.bedTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        val caffeineStart =
            LocalTime.parse(data.caffeineStartTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        val caffeineEnd = LocalTime.parse(data.caffeineEndTime, DateTimeFormatter.ofPattern("HH:mm:ss"))


        var highlightState:HighlightState = HighlightState.START
        when {
            now in graphStart..caffeineStart -> {

                highlightState = HighlightState.START
            }

            now in caffeineStart..caffeineEnd -> {

                highlightState = HighlightState.CAFFEINE
            }

            now in caffeineEnd..graphEnd -> {

                highlightState = HighlightState.END
            }
        }
        //todo check highlight state


        //caffine start - caffine end - local time  //10/
        data.caffeineValues

        if(highlightState == HighlightState.START || highlightState == HighlightState.END){
            return null
        }

        val highlightedIndex = getTimeIndex(
            caffeineStart!!,
            caffeineEnd!!,
            now,
            data.caffeineValues.size
        )

        return if (highlightedIndex != -1){
            data.caffeineValues[highlightedIndex]
        }else{
            null
        }

    }

    fun getTimeIndex(
        startTime: LocalTime,
        endTime: LocalTime,
        currentTime: LocalTime,
        parts: Int
    ): Int {
        if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) return -1

        val totalDuration = Duration.between(startTime, endTime).toMinutes()
        val interval = totalDuration / parts

        val minutesSinceStart = Duration.between(startTime, currentTime).toMinutes()

        return (minutesSinceStart / interval).toInt().coerceAtMost(parts - 1)
    }

    /*    private fun getItemsFromAPI(): List<CaffeineFoodItem> {
            return listOf(
                CaffeineFoodItem(
                    id = "askcc",
                    name = "Diet coke can",
                    quantity = 30.0,
                    unit = "mg",
                    is_favorite = false
                ),
                CaffeineFoodItem(
                    id = "askcc",
                    name = "Diet coke can",
                    quantity = 32.0,
                    unit = "mg",
                    is_favorite = false
                ),
                CaffeineFoodItem(
                    id = "askcc",
                    name = "Diet coke can",
                    quantity = 34.0,
                    unit = "mg",
                    is_favorite = true
                ),
                CaffeineFoodItem(
                    id = "askcc",
                    name = "Diet coke can",
                    quantity = 50.0,
                    unit = "mg",
                    is_favorite = true
                ),

            )
        }*/

}