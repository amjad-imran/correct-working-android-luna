package com.noisefit.ui.feeds.create.styles.workout

import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.noisefit.R
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.databinding.FragmentWorkoutCyclingStyle1Binding
import com.noisefit.util.ImageUtil
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.displayToast
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.ActivityConvertUtils
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutCyclingStyle1Fragment :
    BaseFragment<FragmentWorkoutCyclingStyle1Binding>(FragmentWorkoutCyclingStyle1Binding::inflate) {

    @Inject
    lateinit var dataUnitConverter: DataUnitConverter

    @Inject
    lateinit var watchesSDK: WatchesSDK

    @Inject
    lateinit var localDataStore: DataStoredInterface

    companion object {
        val SELECTED_WORKOUT = "SELECTED_WORKOUT"

        @JvmStatic
        fun newInstance(selectedWorkout: SportsModeResponse) =
            WorkoutCyclingStyle1Fragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(SELECTED_WORKOUT, selectedWorkout)
                }
            }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val workout = arguments?.getParcelable(SELECTED_WORKOUT) as? SportsModeResponse


        workout?.let {

            if ((it.steps?:0) == 0 && (it.calories?.toInt()?:0 )== 0 && (it.distance?.toInt()?:0) == 0) {
                requireActivity().displayToast(R.string.text_you_cannot_share_without_any_activity_progress)
                navigateUpSafe()
            }

            binding.tvWorkoutName.text = it.getFormattedActivityName()
            binding.tvCalorie.text = "${it.calories ?: 0}"

            val distance = dataUnitConverter.formatDistance(
                it.distance?.toInt() ?: 0,
                Units.METRIC
            )
            binding.tvDistance.text = "$distance"
            val activityName = it.type ?: it.activityType
            binding.imageView8.setImageResource(ImageUtil().getImageFromActivity(activityName))

            setAveragePace(it)
            setAverageSpeed(it)
            binding.tvWorkoutTime.text = it.getActivityDurationFormat3()//"2h 30m 10s"


            binding.tvTemperature.text = if (it.temp != null) "${it.temp}° C" else "_"
            binding.tvHumidity.text = if (it.humidity != null) "${it.humidity}%" else "_"
            binding.tvUvIndex.text = if (it.uvi != null) "${it.uvi}" else ""


            if (!it.end.isNullOrEmpty() && it.end != it.start) {
                binding.tvStartLoc2.invisible()
                binding.ivStartLocAsset.invisible()
                binding.groupStartAndEnd.visible()
                binding.tvStartLoc1.text = if (it.start.isNullOrEmpty()) "" else it.start
                binding.tvEndLoc.text = it.end
            } else {
                binding.tvStartLoc2.visible()
                if (it.start.isNullOrEmpty()) {
                    binding.ivStartLocAsset.invisible()
                } else {
                    binding.ivStartLocAsset.visible()
                }
                binding.groupStartAndEnd.invisible()
                binding.tvStartLoc2.text = if (it.start.isNullOrEmpty()) "" else it.start
            }
        }
    }


    private fun setAveragePace(act: SportsModeResponse) {
        var avgPaceUnit = ""
        var avgPace = ""
        val watches = watchesSDK.getWatchType()
        if (watches == SDKWatchType.SDK_NAV_PLUS || watches == SDKWatchType.SDK_ZH) {
            avgPace = ActivityConvertUtils.avgPaceUltra(
                Units.METRIC,
                act.distance,
                act.duration
            )
            avgPaceUnit = if (Units.METRIC == Units.METRIC) " /km" else " /miles"
        } else {
            if (act.distance != null && act.distance!! > 0) {
                if (localDataStore.getConnectedDevice()?.deviceType?.equals(DeviceType.COLORFIT_PULSE_2.deviceType) == true) {
                    avgPace = ActivityConvertUtils.avgPacePulse2(
                        Units.METRIC,
                        act.distance,
                        act.duration
                    )
                    avgPaceUnit = "/km"
                } else {
                    avgPace = ActivityConvertUtils.avgPace(
                        Units.METRIC,
                        act.distance,
                        act.duration
                    )
                    avgPaceUnit = "/km"
                }

            } else {
                avgPace = java.lang.String.format(
                    Locale.ENGLISH,
                    "%1$02d'%2$02d",
                    0,
                    0
                )
                avgPaceUnit = "/km"
            }

        }


        LOGS.d("PACEEEE $avgPace")
        binding.tvPace.text = avgPace
        binding.textView72.text = avgPaceUnit

    }

    private fun setAverageSpeed(act: SportsModeResponse) {
        val avgSpeed = ActivityConvertUtils.averageSpeed(
            Units.METRIC,
            act.distance,
            act.duration
        )
        binding.tvAverageSpeed.text = avgSpeed
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}