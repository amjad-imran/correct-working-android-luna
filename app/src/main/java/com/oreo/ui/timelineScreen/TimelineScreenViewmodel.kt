package com.oreo.ui.timelineScreen

import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import com.oreo.data.model.timeline.habits.HabitsByDateResponse
import com.oreo.data.model.timeline.habits.HabitsByDateResponse.Options
import com.oreo.data.usecases.CancelHabitByIdAndDateUseCase
import com.oreo.data.usecases.GetAllHabitsUseCase
import com.oreo.data.usecases.GetHabitsByDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class TimelineScreenViewmodel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val dataStore: DataStoredInterface,
    private val habitsByDateUC: dagger.Lazy<GetHabitsByDateUseCase>,
    private val cancelHabitByIdAndDateUseCase: dagger.Lazy<CancelHabitByIdAndDateUseCase>,
    private val getAllHabitUC: dagger.Lazy<GetAllHabitsUseCase>
) : BaseViewModel() {

    var habitsResponseData: HabitsByDateResponse ?= null
    private val _allHabits = MutableStateFlow<List<Options>>(emptyList())
    val allHabits: StateFlow<List<Options>> = _allHabits.asStateFlow()

    // expose ONLY 3 visible items
    val visibleHabits: StateFlow<List<Options>> =
        allHabits.map { it.take(3) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // for "+N more"
    val moreCount: StateFlow<Int> =
        allHabits.map { (it.size - 3).coerceAtLeast(0) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun getPrefixAndSuffixList(dataList: List<String>): Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.date = it
            var currentDayText = ""
            if (it == DateFormats.getCurrentDate(DateFormats.dateFormat3())) {
                currentDayText = resourcesProvider.getString(R.string.text_today_comma)
            }
            val formattedDate = if (currentDayText.isEmpty()) {
                DateFormats.getOrdinalDate(
                    it,
                    DateFormats.dateFormat3(),
                    NoiseFitApplicationMain.appLanguage.languageCode
                )
            } else {
                DateFormats.getOrdinalDateToday(
                    it,
                    DateFormats.dateFormat3(),
                    NoiseFitApplicationMain.appLanguage.languageCode
                )
            }
            chartModel.formattedDate = "$currentDayText$formattedDate"
            chartModel.index = ""
            chartModel.value = 0
            list.add(chartModel)
        }

        list.reverse()
        val lastDateFromList = dataList.first()
        val lastDate = DateFormats.subtractDateFormat3(lastDateFromList, 1)!!
        val suffixDatesList = DateFormats.getWeekDaysBetweenDates(
            DateFormats.subtractDateFormat3(lastDate, 14)!!, lastDate,
            DateFormats.dateFormat3(), DateFormats.singleWeekDay()
        )
        val currentDateFromList = dataList.last()
        val currentDate = DateFormats.addDateFormat3(currentDateFromList, 1)!!
        val prefixDatesList = DateFormats.getWeekDaysBetweenDates(
            currentDate,
            DateFormats.addDateFormat3(currentDate, 14)!!,
            DateFormats.dateFormat3(), DateFormats.singleWeekDay()
        )

        val suffix = java.util.ArrayList<ChartModel>()
        suffixDatesList.forEach {
            val chartModel = ChartModel()
            chartModel.index = it
            chartModel.date = ""
            chartModel.value = 0
            suffix.add(chartModel)
        }

        suffix.reverse()


        val prefix = java.util.ArrayList<ChartModel>()
        prefixDatesList.forEach {
            val chartModel = ChartModel()
            chartModel.index = it
            chartModel.date = ""
            chartModel.value = 0
            prefix.add(chartModel)
        }
        prefix.reverse()
        return Triple(list, suffix, prefix)
    }

    fun checkUserFirstTimeForAddHabits() = dataStore.getUserFirstTimeForAddHabits()
    fun setAddHabitFirstTimeVisibility() = dataStore.setUserFirstTimeForAddHabits(false)

    fun getUserSavedHabits(date:String?){
        viewModelScope.launch {
            date?.let {
                habitsByDateUC.get()
                    .invoke(it).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                setLoading(resource.loading)
                            }

                            is Resource.GenericError -> {
                                sendMessage(resource.message)
                            }

                            is Resource.NetworkError -> {
                                setApiErrors(resource.response.apply {
                                    (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                        object : BinaryActionCallback {
                                            override fun yes() {
                                                getUserSavedHabits(date)
                                            }

                                            override fun no() {}
                                        }
                                })
                            }

                            is Resource.Success -> {
                                resource.data?.data?.let { resp ->
                                    habitsResponseData = resp
                                    _allHabits.value = resp.options.filter { it.isCancelled!=true }
                                }
                            }
                        }
                    }
            }
        }
    }

    fun onCrossClicked(id: Int?, selectedDate: String?) {
        if(id == null) return

        viewModelScope.launch {

            // 1) mark as skipping (UI turns red + shows "Skipped")
            _allHabits.value = _allHabits.value.map {
                if (it.timeTrackerOptionId == id) it.copy(state = Options.State.Skipping) else it
            }

            // 2) after 1 sec remove it (DiffUtil animates removal & next item appears)
            delay(1000)
            _allHabits.value = _allHabits.value.filterNot { it.timeTrackerOptionId == id }

            val reqArray = JsonArray()
            reqArray.add(
                JsonObject().apply {
                    addProperty("habit_option_id", id)
                    addProperty("date", selectedDate)
                }
            )

            val reqObj = JsonObject().apply {
                add("cancelled_habits", reqArray)
            }

            cancelHabitByIdAndDateUseCase.get().invoke(reqObj).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        /*setLoading(resource.loading)*/
                    }

                    is Resource.GenericError -> {
                        /*sendMessage(resource.message)*/
                    }

                    is Resource.NetworkError -> {
                        /*setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        onCrossClicked(id, selectedDate)
                                    }

                                    override fun no() {}
                                }
                        })*/
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                        }
                    }
                }
            }

        }

    }


    /*fun getAllUserHabits() {
        viewModelScope.launch {
            getAllHabitUC.get()
                .invoke().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Do Nothing on this
                    }
                    is Resource.Success -> {
                        resource.data?.data.let {
                            _allHabitsState.postValue(it)
                        }
                    }
                    else -> {
                        // TODO: Need to add analytics
                    }
                }
            }
        }
    }*/
}