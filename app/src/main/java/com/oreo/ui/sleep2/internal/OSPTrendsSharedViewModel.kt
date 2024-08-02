package com.oreo.ui.sleep2.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.TrendsGraphData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class OSPTrendsSharedViewModel @Inject constructor() : BaseViewModel() {

    private val _interactGraphData =
        MutableLiveData<LocalDate?>()
    val interactGraphData: LiveData<LocalDate?> = _interactGraphData

    var calendarStartDate: LocalDate = LocalDate.now().minusMonths(1)

    fun sendInteractDay(day: LocalDate?) {
        _interactGraphData.postValue(day)
    }

    fun getYAxisRange(
        maxValue: Int,
        contributorType: SleepInternalLaunchState?
    ): List<Pair<Int, String>> {
        val default = arrayListOf(
            Pair(0, "0%"),
            Pair(25, "25%"),
            Pair(50, "50%"),
            Pair(75, "75%"),
            Pair(100, "100%")
        )
        return when (contributorType) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> default
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> default
            SleepInternalLaunchState.HOUR_VS_NEED -> {
                return when (maxValue) {
                    in 0..720 -> {
                        arrayListOf(
                            Pair(0, "0"),
                            Pair(180, "3"),
                            Pair(360, "6"),
                            Pair(540, "9"),
                            Pair(720, "12")
                        )
                    }

                    else -> {
                        arrayListOf(
                            Pair(0, "0"),
                            Pair(360, "6"),
                            Pair(720, "12"),
                            Pair(1080, "18"),
                            Pair(1440, "24")
                        )
                    }
                }
            }

            SleepInternalLaunchState.SLEEP_TIME -> default
            SleepInternalLaunchState.TIMING -> default
            SleepInternalLaunchState.EFFICIENCY -> {
                default
            }

            SleepInternalLaunchState.REM_SLEEP, SleepInternalLaunchState.DEEP_SLEEP -> {
                if (maxValue <= 60) {
                    return arrayListOf(
                        Pair(0, "0"),
                        Pair(15, "15"),
                        Pair(30, "30"),
                        Pair(45, "45"),
                        Pair(60, "60")
                    )
                } else {
                    return arrayListOf(
                        Pair(0, "0"),
                        Pair(60, "60"),
                        Pair(120, "120"),
                        Pair(180, "180"),
                        Pair(240, "240")
                    )
                }
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                if (maxValue <= 12 * 60) {
                    arrayListOf(
                        Pair(0, "0"),
                        Pair(3 * 60, "3"),
                        Pair(6 * 60, "6"),
                        Pair(9 * 60, "9"),
                        Pair(12 * 60, "12")
                    )
                } else {
                    arrayListOf(
                        Pair(0, "0"),
                        Pair(6 * 60, "6"),
                        Pair(12 * 60, "12"),
                        Pair(18 * 60, "18"),
                        Pair(24 * 60, "24")
                    )
                }
            }

            SleepInternalLaunchState.LATENCY -> {
                if (maxValue <= 40) {
                    return arrayListOf(
                        Pair(0, "0"),
                        Pair(10, "10"),
                        Pair(20, "20"),
                        Pair(30, "30"),
                        Pair(40, "40")
                    )
                } else {
                    return arrayListOf(
                        Pair(0, "0"),
                        Pair(25, "25"),
                        Pair(50, "50"),
                        Pair(75, "75"),
                        Pair(100, "100")
                    )
                }
            }

            SleepInternalLaunchState.RESTFULNESS -> {
                if (maxValue <= 4) {
                    return arrayListOf(
                        Pair(0, "0"),
                        Pair(1, "1"),
                        Pair(2, "2"),
                        Pair(3, "3"),
                        Pair(4, "4")
                    )
                } else {
                    return arrayListOf(
                        Pair(0, "0"),
                        Pair(3, "3"),
                        Pair(6, "6"),
                        Pair(9, "9"),
                        Pair(12, "12")
                    )
                }
            }

            else -> default
        }
    }

    fun getMaxValue(
        dataListType1: List<Int?>? = null,
        dataListType2: List<Pair<Int?, Int?>>? = null,
        contributorType: SleepInternalLaunchState?
    ): Int {
        val nonNullValues = dataListType1?.filterNotNull()
        return when (contributorType) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> 100
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> 100
            SleepInternalLaunchState.HOUR_VS_NEED -> {
                var mMax = 0
                dataListType2?.forEach {

                    var max = it.first ?: 0
                    if ((it.second ?: 0) > max) {
                        max = it.second ?: 0
                    }

                    if (max > mMax) {
                        mMax = max
                    }
                }

                mMax += ((0.2) * mMax).toInt()
                return mMax
            }

            SleepInternalLaunchState.SLEEP_TIME -> 100
            SleepInternalLaunchState.TIMING -> 100
            SleepInternalLaunchState.EFFICIENCY -> {
                100
            }

            SleepInternalLaunchState.DEEP_SLEEP, SleepInternalLaunchState.REM_SLEEP -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    60
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                if (nonNullValues.isNullOrEmpty()) {
                    12 * 60
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.LATENCY -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    25
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.RESTFULNESS -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    4
                } else {
                    nonNullValues.max()
                }
            }

            null -> 100
            else -> 100
        }
    }


    fun redirectToSkinTempInternal(launchMode: SleepInternalLaunchState): Boolean {
        return when (launchMode) {
            SleepInternalLaunchState.SKIN_TEMPERATURE,
            SleepInternalLaunchState.RESPIRATORY_RATE,
            SleepInternalLaunchState.RESTING_HEART_RATE,
            SleepInternalLaunchState.HRV,
            SleepInternalLaunchState.BLOOD_OXYGEN -> {
                return true
            }

            else -> false

        }
    }
    fun getAvgValue(
        dataListType1: List<Int?>? = null,
        dataListType2: List<Pair<Int?, Int?>>? = null,
        contributorType: SleepInternalLaunchState?
    ): Pair<Int, String>? {
        val filteredData = dataListType1?.filterNotNull()
        if (filteredData.isNullOrEmpty()) {
            return null
        }

        val avg = dataListType1.filterNotNull().average().roundToInt()
        return when (contributorType) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> Pair(avg, "$avg%")
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> Pair(avg, "$avg%")

            SleepInternalLaunchState.HOUR_VS_NEED -> Pair(avg, "$avg%")
            SleepInternalLaunchState.SLEEP_TIME -> Pair(avg, "$avg%")
            SleepInternalLaunchState.TIMING -> Pair(avg, "$avg%")
            SleepInternalLaunchState.EFFICIENCY -> Pair(avg, "$avg%")
            SleepInternalLaunchState.REM_SLEEP, SleepInternalLaunchState.DEEP_SLEEP -> {
                Pair(avg, "${avg}min")
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                Pair(avg, "${avg / 60}")
            }

            SleepInternalLaunchState.LATENCY -> Pair(avg, "${avg}min")
            SleepInternalLaunchState.RESTFULNESS -> Pair(avg, "$avg")
            else -> Pair(avg, "$avg%")
        }
    }

    fun getXAxisRange(pageData: TrendsGraphData?): List<String> {
        return when (pageData?.selectedPeriod) {
            InternalSelectedPeriod.MONTH -> {
                arrayListOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
            }

            InternalSelectedPeriod.WEEK -> {
                val weekList = HashSet<Int>()
                val weekListReturn = ArrayList<String>()

                pageData.data?.forEach {
                    val date = LocalDate.parse(it.date)

                    val weekFields = WeekFields.of(Locale.getDefault())
                    val weekNumber = date.get(weekFields.weekOfWeekBasedYear())
                    weekList.add(weekNumber)
                }
                weekList.sorted().forEach {
                    weekListReturn.add("W$it")
                }
                weekListReturn
            }

            else -> {
                arrayListOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            }
        }
    }

}