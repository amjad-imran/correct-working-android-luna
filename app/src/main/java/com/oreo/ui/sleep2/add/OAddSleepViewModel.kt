package com.oreo.ui.sleep2.add

import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.OAddSleep
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OAddSleepViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val resourcesProvider: ResourcesProvider
) :
    BaseViewModel() {

    var isStartTimeSelected = false
    var isEndTimeSelected = false
    var addSleep = OAddSleep()
    var dayPos:Int=0
    var dayName:String="Yesterday"
    var isEdit: Boolean = false
    private val _addSleepResponse =
        MutableLiveData<Event<Boolean>>()//todo return type will change once finalized
    val addSleepResponse = _addSleepResponse

    fun getSleepDuration(): Int {
        if (!isStartTimeSelected || !isEndTimeSelected) {
            return 0
        }

        val diffIn =
            ((addSleep.endHour * 60) + addSleep.endMinute) - ((addSleep.startHour * 60) + addSleep.startMinute)


        return (diffIn * 60)
    }

    fun callApiToAddSleep() {
        /*viewModelScope.launch(Dispatchers.IO) {
            val requestObject = JsonObject().apply {
            }
            userActivityRepository.addWorkout(
                requestObject
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
                                        callApiToAddSleep()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
//post response
                        }
                    }
                }
            }
        }*/
        _addSleepResponse.postValue(Event(true))
    }

    fun getPageTitle(launchMode: OAddSleepLaunchState):String {
        return  if (launchMode == OAddSleepLaunchState.ADD) resourcesProvider.getString(R.string.text_add_sleep) else resourcesProvider.getString(
            R.string.text_edit_sleep
        )
    }

}