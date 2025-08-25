package com.oreo.ui.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.yearMonth
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.HealthCalendar
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject


@HiltViewModel
class HealthCalendarViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var registerDate: Int = -1

    var launchedFrom: String? = null

    val healthDataScore = HashMap<LocalDate, HealthCalendar>()

    val datesToUpdate = MutableLiveData<Event<List<LocalDate>>>()

    var startDate: LocalDate ?= null
    var endDate = LocalDate.now()


    init {
        /*startDate = endDate.minusMonths(2).with(TemporalAdjusters.firstDayOfMonth())

        getCalendarData(startDate.toString(), endDate.toString())*/
    }


    fun getCalendarData(startDate: String, endDate: String) {
        viewModelScope.launch {
            userActivityRepository.getCalendarData(
                startDate,
                endDate
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
                                        getCalendarData(startDate, endDate)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {


                            val dates = ArrayList<LocalDate>()
                            it.forEach { cal ->
                                val parsedDate = LocalDate.parse(cal.date)
                                healthDataScore[parsedDate] = cal
                                dates.add(parsedDate)
                            }

                            datesToUpdate.postValue(Event(dates))

                        }
                    }
                }
            }
        }


    }

    fun getStatusByDate(date: LocalDate): String? {
        return if (launchedFrom.equals("readiness")) {
            healthDataScore[date]?.readiness_status
        } else if (launchedFrom.equals("activity")) {
            healthDataScore[date]?.activity_status
        } else if (launchedFrom.equals("sleep")) {
            healthDataScore[date]?.sleep_status
        } else {
            null
        }
    }

    fun checkAndLoadMoreData(month: CalendarMonth) {
        val date = LocalDate.of(month.yearMonth.year, month.yearMonth.monthValue, 1)
        if (date < startDate) {
            endDate = startDate?.minusMonths(1)?.with(TemporalAdjusters.lastDayOfMonth())
            startDate = endDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
            getCalendarData(startDate.toString(), endDate.toString())
        }
    }

    fun getCalendarStartDate(): YearMonth {
        if (registerDate == -1) {
            return YearMonth.now().minusMonths(11)
        } else {
            val startDate = LocalDate.now().minusDays(registerDate.toLong())
            val max = YearMonth.now().minusMonths(11)
            return if (startDate.yearMonth < max) {
                max
            } else {
                startDate.yearMonth
            }
        }
    }

    fun getUserStartDate(): LocalDate {
        return LocalDate.now().minusDays(registerDate.toLong()).minusDays(1)
    }
}