package com.oreo.ui.timelineScreen.addActivity.activities

import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddMealViewModel @Inject constructor() : BaseViewModel() {


    val mealTime = MutableLiveData<LocalTime>(LocalTime.now())
    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    fun logMeal() {
        //TODO api here

        onAddSuccess.postValue(Event(true))
    }

}