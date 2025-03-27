package com.oreo.ui.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
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
            val minLiter = 1
            val maxLiter = 14

            for (i in minLiter..maxLiter) {
                hydrationList1.add("$i L")
            }
            for (i in 0..900 step 100) {
                hydrationList2.add("$i ml")
            }
        } else {
            val minOz = 30
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
                LOGS.d("sdkjfhlsjdfhksdf ${convertOuncesToRoundedMl(it)}")
                hydrationGoal = (convertOuncesToRoundedMl(it)).toInt()
            }
        }
    }

    fun convertOuncesToRoundedMl(ounces: Double): Int {
        val milliliters = ounces * 29.5735
        val roundedML = (milliliters / 100).roundToInt() * 100.0
        return roundedML.roundToInt()
    }

    fun convertMlToOuncesRounded(milliliters: Double): Int {
        val ounces = milliliters / 29.5735
        val roundedOunces = (ounces / 10).roundToInt() * 10
        return roundedOunces
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


    /**
     * value in ml
     */
    fun getHydrationMessage(ml: Int) :Pair<Int,Int>{
        return when(ml){
            in 0..2000 -> {
                 Pair(R.string.text_hydration_1,R.string.text_hydration_1_message)
            }
            in 2001..3000 -> {
                Pair(R.string.text_hydration_2,R.string.text_hydration_2_message)
            }
            in 3001..4000 -> {
                Pair(R.string.text_hydration_3,R.string.text_hydration_3_message)
            }
            in 4001..5000 -> {
                Pair(R.string.text_hydration_4,R.string.text_hydration_4_message)
            }
            in 5001..Int.MAX_VALUE -> {
                Pair(R.string.text_hydration_5,R.string.text_hydration_5_message)
            }
            else -> {
                Pair(R.string.text_hydration_1,R.string.text_hydration_1_message)
            }
        }
    }


    fun getStepsMessage(steps: Int) :Pair<Int,Int>{
        return when(steps){
            in 0..3000 -> {
                 Pair(R.string.text_steps_1,R.string.text_steps_1_message)
            }
            in 3001..5000 -> {
                Pair(R.string.text_steps_3,R.string.text_steps_3_message)
            }
            in 5001..7000 -> {
                Pair(R.string.text_steps_5,R.string.text_steps_5_message)
            }
            in 7001..8000 -> {
                Pair(R.string.text_steps_7,R.string.text_steps_7_message)
            }
            in 8001..10000 -> {
                Pair(R.string.text_steps_8,R.string.text_steps_8_message)
            }
            in 10001..12000 -> {
                Pair(R.string.text_steps_10,R.string.text_steps_10_message)
            }
            in 12001..15000 -> {
                Pair(R.string.text_steps_12,R.string.text_steps_12_message)
            }
            in 15001..Int.MAX_VALUE -> {
                Pair(R.string.text_steps_15,R.string.text_steps_15_message)
            }

            else -> {
                Pair(R.string.text_steps_1,R.string.text_steps_1_message)
            }
        }
    }

}