package com.oreo.ui.timelineScreen.addActivity.activities

import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddLightExposureViewModel @Inject constructor() : BaseViewModel() {

    val defaultMinutes = 10L

    val lightTime = MutableLiveData<LocalTime>(LocalTime.now().minusMinutes(defaultMinutes))
    val lightDuration = MutableLiveData<Long>(defaultMinutes)
    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    fun logLightExposure() {
        //TODO api here

        onAddSuccess.postValue(Event(true))
    }

}