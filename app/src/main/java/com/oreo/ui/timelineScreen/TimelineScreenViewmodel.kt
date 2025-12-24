package com.oreo.ui.timelineScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.model.HabitsByDateResponse
import com.noisefit.data.model.HabitsResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.implementation.UserRepositoryImpl
import com.noisefit.luna.R
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import com.oreo.data.usecases.GetAllHabitsUseCase
import com.oreo.data.usecases.GetHabitsByDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineScreenViewmodel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val dataStore: DataStoredInterface,
    private val habitsByDateUC: dagger.Lazy<GetHabitsByDateUseCase>,
    private val getAllHabitUC: dagger.Lazy<GetAllHabitsUseCase>
) : BaseViewModel() {

    private val _allHabitsState = MutableLiveData<HabitsResponse>()
    val allHabitsState: LiveData<HabitsResponse> get() = _allHabitsState
    private val _habitsByDateState = MutableLiveData<HabitsByDateResponse>()
    val habitsByDateState: LiveData<HabitsByDateResponse> get() = _habitsByDateState

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
                                // Do Nothing on this
                            }
                            is Resource.Success -> {
                                resource.data?.data.let {
                                    println("Sahil ******* Success")
                                    _habitsByDateState.postValue(it)
                                }
                            }
                            is Resource.GenericError ->{
                                println("Sahil ******* error: "+resource.errorBody)
                            }
                            is Resource.NetworkError -> {
                                println("Sahil ******* error: "+resource.response)
                            }
                        }
                    }
            }
        }
    }


    fun getAllUserHabits() {
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
    }
}