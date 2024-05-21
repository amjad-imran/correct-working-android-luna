package com.oreo.ui.femalehealth.cycletracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CycleTrackerViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
) : BaseViewModel() {

    var selectedDate: LocalDate? = null
    var selectedPos: DayOfWeek? = null

    private val _femaleHealthData = MutableLiveData<FemaleHealthUserInfoModel>()
    val femaleHealthData: LiveData<FemaleHealthUserInfoModel> get() = _femaleHealthData

    private val _cycleHistoryData = MutableLiveData<List<FMHCycleHistoryDataModel>?>()
    val cycleHistoryData: LiveData<List<FMHCycleHistoryDataModel>?> get() = _cycleHistoryData


    fun getCycleHistoryData(date: String) {
        viewModelScope.launch {
            val todayDate = DateFormats.getTodaysDateString(10)
            if (todayDate.equals(date)) {
                getCycleHistoryData()
            } else {
                _cycleHistoryData.postValue(null)
            }

            userActivityRepository.getFemaleHealthUserInfo(date).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        //setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getCycleHistoryData(date)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            _femaleHealthData.postValue(it)
                        }
                    }
                }
            }

        }
    }

    fun getCycleHistoryData() {
        viewModelScope.launch {
            userActivityRepository.getPeriodCycleHistory().collect { resource ->
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
                                object :
                                    BinaryActionCallback {
                                    override fun yes() {
                                        getCycleHistoryData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _cycleHistoryData.postValue(it.take(3))
                        }
                    }
                }
            }
        }
    }


    fun isCycleLengthNormal(cycleLength: Int): Boolean {
        return cycleLength in 21..35
    }

    fun isPeriodLengthNormal(cycleLength: Int): Boolean {
        return cycleLength in 2..7
    }

    fun getPregnancyText(text: String?): String {
        return if (text.equals("high", true)) {
            "High chance of pregnancy"
        } else if (text.equals("low", true)) {
            "Low chance of pregnancy"
        } else if (text.equals("fertile", true)) {
            "Your body is at it’s most fertile today"
        } else {
            ""
        }
    }

    fun getCurrentPhaseText(
        fertileWindowList: List<String>?,
        periodDate: String?,
        currentDate: String
    ): Pair<String, Int>? {
        if (fertileWindowList == null) return null
        if (fertileWindowList.size != 2) return null
        if (periodDate.isNullOrEmpty()) return null

        val localCurrentDate = LocalDate.parse(currentDate)
        val fertileStart = LocalDate.parse(fertileWindowList.first())

        return if (localCurrentDate.isBefore(fertileStart)) {
            Pair("Follicular phase", R.color.color_follicular)
        } else {
            Pair("Luteal phase", R.color.color_luteal)
        }
    }

    fun calculateDaysLeft(dateString: String): Long {
        val targetDate = LocalDate.parse(dateString)
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(today, targetDate)
    }

    fun updateSelectedDate(date: LocalDate) {
        selectedDate = date
        selectedPos = date.dayOfWeek
    }


}