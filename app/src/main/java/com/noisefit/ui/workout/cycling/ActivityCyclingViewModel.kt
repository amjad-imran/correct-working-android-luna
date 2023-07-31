package com.noisefit.ui.workout.cycling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.noisefit.luna.R
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.common.convertMinuteIntoSeconds
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.DetailData
import com.noisefit_commans.data.model.HeartRateZoneData
import com.noisefit_commans.data.model.MarkerEntry
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.getColor
import com.noisefit_commans.ui.getFormattedTimeInHourMinSec
import com.noisefit_commans.utils.ActivityConvertUtils
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ActivityCyclingViewModel
@Inject
constructor(
    val userRepository: UserRepository,
    val localDataStore: DataStoredInterface,
    val dataUnitConverter: DataUnitConverter,
    var sessionManager: SessionManager,
    var watchesSDK: WatchesSDK,
) : BaseViewModel() {

    var unit: Units = Units.METRIC
    var caloriesChartMarkerData: ArrayList<MarkerEntry> = ArrayList()

    //    var hrChartMarkerData: ArrayList<MarkerEntry> = ArrayList()
    private val _activities = MutableLiveData<SportsModeResponse>()
    val activities: LiveData<SportsModeResponse> = _activities

    init {
        unit = localDataStore.getUnit()
    }

    fun handleHrData(activity: SportsModeResponse): Triple<ArrayList<Entry>, ArrayList<Int>, ArrayList<CandleEntry>> {

        val lineChartList: ArrayList<Entry> = ArrayList()
        val candleChartList: ArrayList<CandleEntry> = ArrayList()
        val lineColorList: ArrayList<Int> = ArrayList()

        if (activity.heartRateData == null) {
            return Triple(ArrayList(), ArrayList(), ArrayList())
        }
        val heartRateChunk = activity.heartRateData?.asList()?.chunked(2)
        heartRateChunk?.forEachIndexed { index, heartRateList ->
            var max = heartRateList[0]
            if (heartRateList.size > 1) {
                max = (heartRateList[1] + 1)
            }
            val min = heartRateList[0]

            val avg = (min + max) / 2
            LOGS.d("handleHrData $min $max")
            candleChartList.add(
                CandleEntry(
                    index.toFloat(),
                    max.toFloat(),
                    min.toFloat(),
                    max.toFloat(),
                    min.toFloat()
                )
            )
            if (index % 2 == 0) {
                lineColorList.add(R.color.calories_arc.getColor())
            } else {
                lineColorList.add(R.color.white.getColor())
            }
            lineChartList.add(
                Entry(
                    index.toFloat(),
                    avg.toFloat()
                )
            )

        }


        return Triple(lineChartList, lineColorList, candleChartList)
    }

    fun handleCaloriesData(activity: SportsModeResponse): ArrayList<BarEntry> {
        val values: ArrayList<BarEntry> = ArrayList()
        activity.calorieData?.forEachIndexed { index, data ->
            values.add(BarEntry(index.toFloat(), data.toFloat()))
            val time = "$index-${index + 1} min"
            val cal = "$data kcal"
            caloriesChartMarkerData.add(MarkerEntry(cal, time))
        }
        return values
    }

    fun getDistance(act: SportsModeResponse): Pair<String, String> {
        val distance = dataUnitConverter.formatDistance(
            act.distance?.toInt() ?: 0,
            unit
        )
        val unit = dataUnitConverter.distanceUnit(unit)
        return Pair(distance, unit)
    }

    fun getBodyTempWithUnit(value: Float): String {
        return "${
            dataUnitConverter.formatBodyTemp(
                value,
                unit
            )
        } ${dataUnitConverter.bodyTempUnit(unit)}"
    }

    fun generateDetailsData(act: SportsModeResponse, unit: Units): List<DetailData> {
        val result = ArrayList<DetailData>()

        val isStandingActivity = act.isStandingActivity()


        result.add(
            DetailData(
                "Duration",
                act.getActivityDurationFormat2(),
                "",
                R.drawable.ic_steps_gradient,
                R.drawable.back_image_step
            )
        )
        if (act.distance != null && act.distance!! > 0L) {
            result.add(
                DetailData(
                    "Total distance",
                    dataUnitConverter.formatDistance(
                        act.distance?.toInt() ?: 0,
                        unit
                    ),
                    dataUnitConverter.distanceUnit(unit),
                    R.drawable.ic_steps_gradient,
                    R.drawable.back_image_step
                )
            )
        }


        var hasCalorieGraphData = false
        act.calorieData?.let {

            if (it.isNotEmpty()) {
                hasCalorieGraphData = true
            }
        }

        if (act.calories != null && act.calories!! > 0) {
            result.add(
                DetailData(
                    "Calories",
                    act.calories.toString(),
                    "Kcal",
                    R.drawable.ic_steps_gradient,
                    R.drawable.back_image_step
                )
            )
        }


        var hasHeartGraphData = false
        act.heartRateData?.let {
            if (it.isNotEmpty()) {
                hasHeartGraphData = true
            }
        }

        if (act.heartRateAvg != null && act.heartRateAvg!! > 0) {
            result.add(
                DetailData(
                    "Avg Heart Rate",
                    act.heartRateAvg.toString(),
                    "bpm",
                    R.drawable.ic_steps_gradient,
                    R.drawable.back_image_step
                )
            )
        }


        if (!isStandingActivity) {


            var avgPaceUnit = ""
            var avgPace = ""
            //To be optimized , send watch type in activity instead
            val watches = watchesSDK.getWatchType()
            if (watches == SDKWatchType.SDK_NAV_PLUS || watches == SDKWatchType.SDK_ZH) {
                avgPace = ActivityConvertUtils.avgPaceUltra(
                    unit,
                    act.distance,
                    act.duration
                )
                avgPaceUnit = if (unit == Units.METRIC) " /km" else " /miles"
            } else {
                if (act.distance != null && act.distance!! > 0) {
                    if (localDataStore.getConnectedDevice()?.deviceType?.equals(DeviceType.COLORFIT_PULSE_2.deviceType) == true) {
                        avgPace = ActivityConvertUtils.avgPacePulse2(
                            unit,
                            act.distance,
                            act.duration
                        )
                        avgPaceUnit = if (unit == Units.METRIC) " /km" else " /miles"
                    } else {
                        avgPace = ActivityConvertUtils.avgPace(
                            unit,
                            act.distance,
                            act.duration
                        )
                        avgPaceUnit = if (unit == Units.METRIC) " /km" else " /miles"
                    }

                } else {
                    avgPace = java.lang.String.format(
                        Locale.ENGLISH,
                        "%1$02d'%2$02d",
                        0,
                        0
                    )
                    avgPaceUnit = if (unit == Units.METRIC) " /km" else " /miles"
                }

            }

            val averageSpeed = dataUnitConverter.averageSpeed(act.distance, unit, act.duration)
            val avgSpeed = ActivityConvertUtils.averageSpeed(
                unit,
                act.distance,
                act.duration
            )

            val tempUnit = if (unit == Units.METRIC) " km/hr" else " miles/hr"
            val finalAverageSpeed = averageSpeed + tempUnit

            val avgSpeedUnit = if (unit == Units.METRIC) " km/hr" else " miles/hr"
//            val finalAverageSpeed=averageSpeed+avgSpeedUnit


            if (act.distance != null && act.distance!! > 0) {
                result.add(
                    DetailData(
                        "Avg Pace",
                        avgPace,
                        avgPaceUnit,
                        R.drawable.ic_steps_gradient,
                        R.drawable.back_image_step
                    )
                )
                result.add(
                    DetailData(
                        "Avg Speed",
                        avgSpeed,
                        avgSpeedUnit,
                        R.drawable.ic_steps_gradient,
                        R.drawable.back_image_step
                    )
                )
                if (act.cadence != null && act.cadence!! > 0) {
                    result.add(
                        DetailData(
                            "Avg Cadence",
                            "${act.cadence}",
                            "steps/min",
                            R.drawable.ic_steps_gradient,
                            R.drawable.back_image_step
                        )
                    )
                }
            }

            if (act.steps != null && act.steps!! > 0) {
                result.add(
                    DetailData(
                        "Steps",
                        "${act.steps}",
                        "",
                        R.drawable.ic_steps_gradient,
                        R.drawable.back_image_step
                    )
                )
            }
            if (act.avgStepStride != null && act.avgStepStride!! > 0) {
                val strideUnit: String = if (unit == Units.METRIC)
                    "cm"
                else
                    "in"
                result.add(
                    DetailData(
                        "Avg Stride",
                        dataUnitConverter.formatAvgStride(act.avgStepStride, unit),
                        strideUnit,
                        R.drawable.ic_steps_gradient,
                        R.drawable.back_image_step
                    )
                )
            }
        }

        return result

    }

    fun getHeartRateData(activity: SportsModeResponse): ArrayList<HeartRateZoneData>? {
        val heartRateProgressList = ArrayList<HeartRateZoneData>()
//        val duration = (activity.extremeMin ?: 0) + (activity.anaerobic ?: 0) +
//                (activity.aerobic ?: 0) + (activity.fatBurn ?: 0) + (activity.warmUp ?: 0)
        //val duration = activity.duration ?: 0
        var extremeSeconds = activity.extremeMin?.convertMinuteIntoSeconds()
        var anaerobicSeconds = activity.anaerobic?.convertMinuteIntoSeconds()
        var aerobicSeconds = activity.aerobic?.convertMinuteIntoSeconds()
        var fatBurnSeconds = activity.fatBurn?.convertMinuteIntoSeconds()
        var warmUpSeconds = activity.warmUp?.convertMinuteIntoSeconds()

        if (activity.hrZoneInSeconds == 1) {
            extremeSeconds = activity.extremeMin
            anaerobicSeconds = activity.anaerobic
            aerobicSeconds = activity.aerobic
            fatBurnSeconds = activity.fatBurn
            warmUpSeconds = activity.warmUp
        }

        val duration = (extremeSeconds ?: 0) + (anaerobicSeconds ?: 0) + (aerobicSeconds
            ?: 0) + (fatBurnSeconds ?: 0) + (warmUpSeconds ?: 0)

        if (duration == 0) {
            return null
        }

        heartRateProgressList.add(HeartRateZoneData().apply {
            progress = extremeSeconds?.toFloat()?.calculatePercentage(duration.toFloat())
                ?.roundToInt()
                ?: 0
            name = "Extreme"
            minutes = extremeSeconds?.toInt().getFormattedTimeInHourMinSec()
            color = R.color.extreme_start
        })

        val child2 = HeartRateZoneData()

        child2.progress =
            anaerobicSeconds?.toFloat()?.calculatePercentage(duration.toFloat())
                ?.roundToInt()
                ?: 0

        child2.name = "Anaerobic"
        child2.minutes = anaerobicSeconds?.toInt().getFormattedTimeInHourMinSec()
        child2.color = R.color.anaerobic_start
        heartRateProgressList.add(child2)

        val child3 = HeartRateZoneData()

        child3.progress =
            aerobicSeconds?.toFloat()?.calculatePercentage(duration.toFloat())
                ?.roundToInt()
                ?: 0

        child3.name = "Aerobic"
        child3.minutes = aerobicSeconds?.toInt().getFormattedTimeInHourMinSec()
        child3.color = R.color.aerobic_start
        heartRateProgressList.add(child3)

        val child4 = HeartRateZoneData()
        child4.progress =
            fatBurnSeconds?.toFloat()?.calculatePercentage(duration.toFloat())
                ?.roundToInt()
                ?: 0

        child4.name = "Fat Burning"
        child4.minutes = fatBurnSeconds?.toInt().getFormattedTimeInHourMinSec()
        child4.color = R.color.fat_burn_start
        heartRateProgressList.add(child4)

        val child5 = HeartRateZoneData()


        child5.progress =
            warmUpSeconds?.toFloat()?.calculatePercentage(duration.toFloat())
                ?.roundToInt()
                ?: 0

        child5.name = "Warm Up"
        child5.minutes = warmUpSeconds?.toInt().getFormattedTimeInHourMinSec()
        child5.color = R.color.warm_up_start
        heartRateProgressList.add(child5)

//        val child6 = HeartRateZoneData()

//        child6.progress =
//            activity.getHrZonesProgress(activity.duration?.toFloat(), activity.bel?.toFloat())
//        child6.name = "Below Zone"
//        child6.minutes = "0 min"
//        child6.color = R.color.below_zone_start
//        heartRateProgressList.add(child6)
        return heartRateProgressList
    }


    fun fetchDetailsDataFromServer(itemId: Int) {
        viewModelScope.launch {
            setLoading(true)
            userRepository.getActivitiesDetails(itemId).collect { resource ->
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    fetchDetailsDataFromServer(itemId)
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _activities.postValue(it)
                        }
                    }
                }
            }
        }
    }
}