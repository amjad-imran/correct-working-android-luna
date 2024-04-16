package com.oreo.ui.heartrate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.health.Nudges
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OHeartRateDataViewModel @Inject constructor(
    val userRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var date: String? = null
    var isToday = false
    val heartRateData = MutableLiveData<OHealthOverview.HeartRate?>()

    fun getNudges(): ArrayList<Nudges> {
        val dataList = ArrayList<Nudges>()
        dataList.add(
            Nudges(
                label = "Rest is productive",
                message = "Your resting hr is good"
            )
        )
        dataList.add(
            Nudges(
                label = "Rest is not productive",
                message = "Your resting hr is good"
            )
        )
        return dataList
    }

    fun getLearnMoreData(): ArrayList<LearnMoreDataModel> {
        val dataList = ArrayList<LearnMoreDataModel>()
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        return dataList
    }

    fun checkIsToday(date: String?) {
        if (date == null) {
            isToday = false
        }
        val todayDate = DateFormats.getCurrentDate(DateFormats.dateFormat3)
        isToday = todayDate.equals(date, true)
    }

    fun getTodayHeartRate() {
        viewModelScope.launch(Dispatchers.IO) {
            heartRateData.postValue(userRepository.getSummaryHRHealthOverview().apply {
            })
        }
    }

}