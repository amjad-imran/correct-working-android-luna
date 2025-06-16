package com.oreo.ui.chatGpt.functions

import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.AiMeal
import com.noisefit.data.model.AiMealResponse
import com.noisefit.data.model.AiMeals
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository,
    val localDataStore: DataStoredInterface,
) : BaseViewModel() {

    val workoutData: String? = null

    val dayNutrients = MutableLiveData<List<String>>()
    val selectedPosition = MutableLiveData<Int>()
    val currentSelectedWeekDayPosition = MutableLiveData<Int>()

    private val mealResponse = ArrayList<AiMealResponse>()
    val dayMealList = MutableLiveData<Pair<List<AiMeals>?,DietState>>()

    val currentDayBoosterMeals = MutableLiveData<AiMealResponse>()
//    val currentDayRegularMeals = MutableLiveData<AiMealResponse>()

    val dietState = MutableLiveData<DietState>()

//    var rememberCurDayDietState: DietState?

    /*val isRegularDiet = MutableLiveData<Boolean>()

    val boosterFood = MutableLiveData<AiMeal?>()
    val isMale = MutableLiveData<Boolean>()*/

    init {
        currentSelectedWeekDayPosition.postValue(LocalDate.now().dayOfWeek.value)
    }

    /*private fun initData(){
        val user = localDataStore.getUser()
        if(user?.userInfo?.gender.equals("male", true)) {
            isMale.postValue(true)
        } else {
            isMale.postValue(false)
        }
        //
        if(!localDataStore.getLdwReadinessData() && !localDataStore.getLdwReadinessData()){
            dietState.postValue(DietState.REGULAR)
        }else{
            dietState.postValue(DietState.COMFORT)
        }
    }*/

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

                            if (
                                !localDataStore.getLdwReadinessData() &&
                                !localDataStore.getLdwCycleTrackerData()
                            )
                            {
                                LOGS.d("yashhhhhhhhdkkfdkjhdsfk  : normal")

                                dietState.postValue(DietState.NORMAL)
//                                rememberCurDayDietState = DietState.NORMAL
                            }else{
                                LOGS.d("yashhhhhhhhdkkfdkjhdsfk  : regular")
                                dietState.postValue(DietState.REGULAR)
//                                rememberCurDayDietState = DietState.REGULAR
                            }

                            setSelectedPosition(LocalDate.now().dayOfWeek.value)
                        }
                    }
                }
            }
        }
    }

    fun getComfortMealPlans() {
        viewModelScope.launch {
            if(currentDayBoosterMeals.value==null || currentDayBoosterMeals.value?.meals==null) {
                oreoDeviceRepository.getAiComfortMealPlans().collect { resource ->
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

                                /*mealResponse.clear()
                            mealResponse.addAll(it)*/
                                dietState.value = (DietState.COMFORT)
                                currentDayBoosterMeals.value = (it)
                                LOGS.d("lkncascan : ${it.meals}")
                                LOGS.d("lkncascanaa : ${currentDayBoosterMeals.value}")
                                /*dayMealList.postValue(it.meals)*/

                                setSelectedPosition(LocalDate.now().dayOfWeek.value)

                            }
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

        LOGS.d("yashhhhhhhh post : ${meals?.meals}")

        if (meals == null) {
            dayMealList.postValue(Pair(null, DietState.NORMAL))
        } else {
            /*if(currentDayRegularMeals.value==null && position == LocalDate.now().dayOfWeek.value){
                meals?.let { cm -> currentDayRegularMeals.postValue(cm) }
            }*/
            if(position==LocalDate.now().dayOfWeek.value){
                when(dietState.value){
                    DietState.COMFORT -> {
                        dayMealList.postValue(Pair(currentDayBoosterMeals.value?.meals,DietState.COMFORT))
                    }
                    DietState.REGULAR, DietState.NORMAL, null -> {
                        dayMealList.postValue(Pair(meals.meals,DietState.REGULAR))
                    }
                }
            }else {
                dayMealList.postValue(Pair(meals.meals,DietState.NORMAL))
            }
        }

        /*if(meals != null && isMale.value == false){
            *//*boosterFood.postValue(meals.booster)*//*
        }*/

        //dayNutrients.postValue(arrayListOf("", "", "", ""))
    }

    private fun getDayName(position: Int): String {
        return "day_$position"
    }

    fun getTextShaderForGradient(
        width: Float,
        startGradColor: Int,
        endGradColor: Int
    ): LinearGradient {
        return LinearGradient(
            0f, 0f, width, 0f,  // Left to right gradient
            intArrayOf(
                startGradColor,  // start color
                endGradColor   // end color
            ),
            null,
            Shader.TileMode.CLAMP
        )
    }

    enum class DietState {
        REGULAR, COMFORT, NORMAL
    }

}