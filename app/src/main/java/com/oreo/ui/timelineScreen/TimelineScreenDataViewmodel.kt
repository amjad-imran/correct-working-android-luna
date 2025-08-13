package com.oreo.ui.timelineScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.timeline.ItemTimelineModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineScreenDataViewmodel @Inject constructor(
    private val userRepository: UserRepository,
): BaseViewModel() {

    var date: String? = null

    val activityListData = MutableLiveData<ArrayList<ItemTimelineModel>>()

    fun getCurrDayActivities(date: String){
        viewModelScope.launch {
            userRepository.getCurrDayTimelineActivitiesData(date).collect{ resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {

                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.timeTracker?.let { dataList ->
//                                activityListData.postValue(ArrayList(dataList))
                            }
                        }
                    }
                }
            }
        }
    }

}