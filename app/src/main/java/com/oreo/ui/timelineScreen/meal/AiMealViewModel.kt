package com.oreo.ui.timelineScreen.meal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.moengage.core.internal.serializers.toJsonElement
import com.noisefit.data.model.timeline.MealAiFoods
import com.noisefit.data.model.timeline.MealAiResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class AiMealViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    val mealAiResponse = MutableLiveData<MealAiResponse?>()
    var mealTime = MutableLiveData<LocalTime>(LocalTime.now())
    var lastEnteredPrompt: String? = null

    var onAddSuccess = MutableLiveData<Event<Boolean>>()

    fun getNutritionFromText(text: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val req = JsonObject().apply {
                this.addProperty("prompt", text)
            }

            userRepository.getNutritionFromText(
                req
            ).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        //setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getNutritionFromText(text)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            mealAiResponse.postValue(it)


                            val time = try {
                                if (it.time.isNullOrEmpty()) {
                                    LocalTime.now()
                                } else {
                                    LocalTime.parse(it.time, DateTimeFormatter.ofPattern("HH:mm"))
                                }
                            } catch (exp: Exception) {
                                LocalTime.now()
                            }

                            mealTime.postValue(time)
                        }
                    }
                }
            }
        }

    }

    fun saveMeal(foods: List<MealAiFoods>) {
        viewModelScope.launch(Dispatchers.IO) {


            val req = createRequestObject(foods, mealTime.value!!,mealAiResponse)

            userRepository.saveAiMeal(
                req
            ).collect { resource ->
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
                                        saveMeal(foods)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            onAddSuccess.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }

    private fun createRequestObject(
        foods: List<MealAiFoods>,
        value: LocalTime,
        mealAiResponse: MutableLiveData<MealAiResponse?>
    ) : JsonObject{

        val mealObject = JsonObject()
        val foodsArray = JsonArray()

        foods.forEach { food ->
            foodsArray.add(JsonObject().apply {
                addProperty("name", food.name)
                addProperty("calories", food.calories)
            })
        }
        mealObject.add("foods", foodsArray)
        mealObject.addProperty("time", value.format(DateTimeFormatter.ofPattern("HH:mm")))

        val macrosArray = JsonArray()

        mealAiResponse.value?.macros?.forEach { macro ->
            macrosArray.add(JsonObject().apply {
                addProperty("name", macro.name)
                addProperty("value", macro.value)
            })
        }
        mealObject.add("macros", macrosArray)
        mealObject.addProperty("prompt", lastEnteredPrompt)
        mealObject.addProperty("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

        val request = JsonObject()
        request.add("meal", mealObject)
        return request
    }


}