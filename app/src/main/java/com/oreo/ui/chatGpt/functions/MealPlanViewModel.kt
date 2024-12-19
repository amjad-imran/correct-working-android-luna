package com.oreo.ui.chatGpt.functions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.AiMealResponse
import com.noisefit.data.model.AiMeals
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository
) : BaseViewModel() {

    val workoutData: String? = null

    val dayNutrients = MutableLiveData<List<String>>()
    val selectedPosition = MutableLiveData<Int>()
    val currentSelectedWeekDayPosition = MutableLiveData<Int>()

    private val mealResponse = ArrayList<AiMealResponse>()
    val dayMealList = MutableLiveData<List<AiMeals>?>()


    init {
        currentSelectedWeekDayPosition.postValue(LocalDate.now().dayOfWeek.value)
    }

    fun getMealPlans() {
        viewModelScope.launch {
            oreoDeviceRepository.getAiMealPlans().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getMealPlans()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            mealResponse.clear()
                            mealResponse.addAll(it)

                            setSelectedPosition(LocalDate.now().dayOfWeek.value)//todo based on current day
                        }
                    }
                }
            }
        }
    }

    /**
     *@param position-> 1..7 (Mon - Sun)
     */
    fun setSelectedPosition(position: Int) {
        selectedPosition.postValue(position)

        val meals = mealResponse.find {
            it.day_name.equals(getDayName(position), true)
        }

        if (meals == null) {
            dayMealList.postValue(null)
        } else {
            dayMealList.postValue(meals.meals)
        }

        //dayNutrients.postValue(arrayListOf("", "", "", ""))
    }

    private fun getDayName(position: Int): String {
        return "day_$position"
    }
}