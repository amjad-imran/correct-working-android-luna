package com.oreo.ui.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.NotificationGoals
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNotificationGoalViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    val notificationGoalReceived = MutableLiveData<Event<NotificationGoals>>()

    private val stepsGoalList = mutableListOf<String>()

    fun getHydrationGoalList(isMetric: Boolean): Pair<List<String>, List<String>> {

        val list1 = mutableListOf<String>()
        val list2 = mutableListOf<String>()
        if (isMetric) {
            val minLiter = 1
            val maxLiter = 15

            for (i in minLiter..maxLiter) {
                list1.add("$i L")
            }
            for (i in 100..1000) {
                list2.add("$i ml")
            }
        } else {
            val minOz = 40
            val maxOz = 500

            for (i in minOz..maxOz step 10) {
                list1.add("$i oz")
            }
        }
        return Pair(list1, list2)
    }


    fun getStepsGoalsList(): List<String> {
        val min = 1000
        val max = 100000

        stepsGoalList.clear()
        for (i in min..max step 1000) {
            stepsGoalList.add(i.toString())
        }
        return stepsGoalList
    }

    fun updateNotificationGoal(steps: Int, hydration: Int) {
        /*viewModelScope.launch {

            userActivityRepository.updateNotificationGoals().collect { resource ->
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
                                        updateNotificationGoal(steps, hydration)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {


                        }
                    }
                }
            }
        }*/
    }

    fun getNotificationGoals() {
        viewModelScope.launch {
            userActivityRepository.getNotificationGoals().collect { resource ->
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
                                        getNotificationGoals()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            notificationGoalReceived.postValue(Event(it))
                        }
                    }
                }
            }
        }
    }

    fun getSelectedStepsPosition(selectedValue: Int): Int {
        return stepsGoalList.indexOfFirst {
            it.toInt() == selectedValue
        }
    }
}