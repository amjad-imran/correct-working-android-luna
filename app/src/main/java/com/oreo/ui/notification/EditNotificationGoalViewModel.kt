package com.oreo.ui.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.NotificationGoals
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class EditNotificationGoalViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    var sessionManager: SessionManager
) : BaseViewModel() {

    var hydrationGoal: Int? = null
    var stepsGoal: Int? = null

    val notificationGoalReceived = MutableLiveData<Event<NotificationGoals>>()

    private val stepsGoalList = mutableListOf<String>()
    private val hydrationList1 = mutableListOf<String>()
    private val hydrationList2 = mutableListOf<String>()


    fun getHydrationGoalList(isMetric: Boolean): Pair<List<String>, List<String>> {
        hydrationList1.clear()
        hydrationList2.clear()

        if (isMetric) {
            val minLiter = 0
            val maxLiter = 14

            for (i in minLiter..maxLiter) {
                hydrationList1.add("$i L")
            }
            for (i in 0..900 step 100) {
                hydrationList2.add("$i ml")
            }
        } else {
            val minOz = 40
            val maxOz = 500

            for (i in minOz..maxOz step 10) {
                hydrationList1.add("$i oz")
            }
        }
        return Pair(hydrationList1, hydrationList2)
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

    fun getSelectedHydrationImperialPosition(selectedValue: Int):Int{
        return hydrationList1.indexOfFirst {
            it.split(" ").get(0).toInt()==selectedValue
        }
    }

    fun updateHydration(value1: Int, metric: Boolean) {
        if (metric) {
            hydrationGoal = value1
        } else {
            value1.toDouble().let {
                LOGS.d("sdkjfhlsjdfhksdf ${convertOuncesToRoundedLiters(it)}")
                hydrationGoal = (convertOuncesToRoundedLiters(it) * 1000).toInt()
            }
        }
    }

    private fun convertOuncesToRoundedLiters(ounces: Double): Int {
        val milliliters = ounces * 29.5735
        val roundedML = (milliliters / 100).roundToInt() * 100.0
        return roundedML.roundToInt()
    }

    fun convertMlToOuncesRounded(milliliters: Double): Int {
        val ounces = milliliters / 29.5735
        return (ounces / 10).roundToInt() * 10
    }

    fun getSelectedHydrationMetricPositionL(hydrationValue: Int): Int {
        val literValue = hydrationValue/1000
        return hydrationList1.indexOfFirst {
            it.split(" ").get(0).toInt()==literValue
        }
    }

    fun getSelectedHydrationMetricPositionMl(hydrationValue: Int): Int {
        val mlvalue = hydrationValue%1000
        return hydrationList2.indexOfFirst {
            it.split(" ").get(0).toInt()==mlvalue
        }
    }

}