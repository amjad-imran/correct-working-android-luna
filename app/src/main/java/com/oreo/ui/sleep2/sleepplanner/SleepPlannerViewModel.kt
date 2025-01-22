package com.oreo.ui.sleep2.sleepplanner

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.text.TextPaint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.SleepPlannerData
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SleepPlannerViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val resourcesProvider: ResourcesProvider,
) : BaseViewModel() {
    fun getAlarmDays(): ArrayList<String> {
        return arrayListOf("Mon", "Tue", "Wed", "Thur", "Fri", "Sat", "Sun")
    }

    val sleepPlannerCard = MutableLiveData<SleepPlannerData?>()


    fun getSleepPlanerDetails() {
        viewModelScope.launch {

            userActivityRepository.getSleepPlannerDetails().collect { resource ->
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
                                        getSleepPlanerDetails()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            sleepPlannerCard.postValue(it)
                        }
                    }
                }
            }
        }
    }

    fun getGoalTextByKey(goal: String?): String {
        if (goal.isNullOrEmpty()) {
            return ""
        }

        return if (goal.equals("sleep_debt", true)) {
            resourcesProvider.getString(R.string.text_recover_sleep_debt)
        } else {
            resourcesProvider.getString(R.string.text_establish_consistency)
        }
    }
}