package com.oreo.ui.timelineScreen.addActivity.activities

import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddCaffeineViewModel @Inject constructor() : BaseViewModel() {

    val caffeineTime = MutableLiveData<LocalTime>(LocalTime.now())
    val caffeineValue = MutableLiveData<Int>(30)
    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    fun logCaffeineValue() {
        //TODO api here

        onAddSuccess.postValue(Event(true))
    }

}