package com.oreo.ui.timelineScreen.habits.yourHabits

import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.timeline.habits.HabitsByDateResponse
import com.oreo.data.model.timeline.habits.HabitsByDateResponse.Options
import com.oreo.data.usecases.CancelHabitByIdAndDateUseCase
import com.oreo.data.usecases.GetHabitsByDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class YourHabitsTimelineViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
    private val habitsByDateUC: dagger.Lazy<GetHabitsByDateUseCase>,
    private val cancelHabitByIdAndDateUseCase: dagger.Lazy<CancelHabitByIdAndDateUseCase>,
): BaseViewModel() {

    var mDate: String ?= null

    var habitsResponseData: HabitsByDateResponse ?= null
    private val _allHabits = MutableStateFlow<List<Options>>(emptyList())
    val allHabits: StateFlow<List<Options>> = _allHabits.asStateFlow()

    fun onCrossClicked(id: Int?) {
        if(id == null) return

        viewModelScope.launch {

            _allHabits.value = _allHabits.value.map {
                if (it.timeTrackerOptionId == id) it.copy(isCancelled = true) else it
            }

            val reqArray = JsonArray()
            reqArray.add(
                JsonObject().apply {
                    addProperty("habit_option_id", id)
                    addProperty("date", mDate)
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

    fun getUserSavedHabits(date:String?) {
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
                                    _allHabits.value =
                                        resp.options
                                }
                            }
                        }
                    }
            }
        }
    }

    fun getIsButtonsDisabled(date: String?): Boolean{
        return try {
            val today = LocalDate.now()
            today.toString() != date && today.minusDays(1L).toString() != date
        }catch (_: Exception){
            true
        }
    }

}