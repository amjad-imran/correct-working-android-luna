package com.oreo.ui.timelineScreen.addActivity.activities

import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddWaterViewModel @Inject constructor() : BaseViewModel() {

    val waterIntakeTime = MutableLiveData<LocalTime>(LocalTime.now())
    val waterIntakeValue = MutableLiveData<Int>(100)
    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    fun logWaterIntakeValue() {
        //TODO api here

        onAddSuccess.postValue(Event(true))
    }

}