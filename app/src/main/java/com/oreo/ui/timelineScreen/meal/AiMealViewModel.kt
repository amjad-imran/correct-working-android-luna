package com.oreo.ui.timelineScreen.meal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.model.timeline.MealAiFoods
import com.noisefit.data.model.timeline.MealAiResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                        setLoading(resource.loading)
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

    }


}