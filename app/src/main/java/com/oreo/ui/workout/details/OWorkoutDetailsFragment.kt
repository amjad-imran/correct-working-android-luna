package com.oreo.ui.workout.details

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOWorkoutDetailsBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.*
import com.noisefit_commans.ui.custom.WorkoutIntensityGraphOreo
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.data.model.ChartModel
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.OWDActivityData
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.health.CommonListDataModel
import com.oreo.ui.activity.all.DELETE_WORKOUT_REQUEST_KEY
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


@AndroidEntryPoint
class OWorkoutDetailsFragment :
    BaseFragment<FragmentOWorkoutDetailsBinding>(FragmentOWorkoutDetailsBinding::inflate) {

    private val mViewModel: OWorkoutDetailsViewModel by viewModels()
    private val args: OWorkoutDetailsFragmentArgs by navArgs()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    private val mAdapter: OWorkoutDetailslAdapter by lazy {
        OWorkoutDetailslAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultUiValue()
        setRecycler()
        mViewModel.getWorkoutDetails(args.workoutId)
        mViewModel.position = args.position
    }

    private fun setRecycler() {
        with(binding.rvActivityDetails) {
            adapter = mAdapter
        }
    }


    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvEdit.setOnClickListener {
            val id = mViewModel.workoutDetailsResponse.value?.id
            mViewModel.deleteWorkout(id!!)
        }

    }

    private fun setDefaultUiValue() {
        binding.lytIntensity.tvTitle.text = getString(R.string.text_intensity)
        binding.lytHeartRate.tvTitle.text = getString(R.string.text_heart_rate)
        binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_max_hr)
        binding.lytHeartRate.tvSubtitle2.text = getString(R.string.text_average_hr)

        binding.lytPace.tvTitle.text = getString(R.string.text_pace)
        binding.lytPace.tvSubtitle1.text = getString(R.string.text_average)
        binding.lytPace.tvSubtitle2.text = getString(R.string.maximum)
    }

    override fun subscribeObservers() {

        mViewModel.workoutDetailsResponse.observe(this) {
            if (it != null) {
                updateUi(it)
            }
        }

        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        mViewModel.workoutDeletedResponse.observe(this) {
            it?.getContent()?.let { response ->
                mainViewModel.reloadTodaysData()

                setFragmentResult(
                    DELETE_WORKOUT_REQUEST_KEY,
                    bundleOf("allow" to true, "position" to mViewModel.position)

                )
                navigateUpSafe()
            }
        }
        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(it: OWorkoutDetailsResponseModel) {
//        binding.lytIntensity.tvIntensityType.text = it.intensity


        if (it.date == DateFormats.getCurrentDate(DateFormats.dateFormat3()) && !it.type.equals(
                "auto",
                true
            ) && !it.type.equals(
                "apple",
                true
            )
            && !it.type.equals(
                "google",
                true
            )
        ) {
            binding.tvEdit.visible()
        }


        binding.rvActivityDetails.visible()
        binding.lytActivityItem.root.visible()

        binding.lytActivityItem.tvActivityDate.text = LocalDate.parse(it.date).format(
            DateTimeFormatter.ofPattern(
                "dd MMM, yyyy",
                Locale(NoiseFitApplicationMain.appLanguage.languageCode)
            )
        )

        binding.lytActivityItem.tvTime.text =
            DateFormats.getActivityDisplayDates(it.startTime, it.endTime)

        binding.lytActivityItem.ivWorkoutImage.loadImage(
            requireContext(), it.iconUrl
        )
        prepareDataForActivity(it)

        if (it.type.equals("apple", true)) {
            binding.lytHeartRate.root.gone()
            binding.divider1.root.visible()
            binding.lytIntensity.root.gone()
            binding.divider2.root.gone()
            binding.tvImportText.visible()
            binding.tvImportText.text = getString(R.string.text_imported_from_health)
        } else if (it.type.equals("google", true)) {
            binding.lytHeartRate.root.gone()
            binding.divider1.root.visible()
            binding.lytIntensity.root.gone()
            binding.divider2.root.gone()
            binding.tvImportText.visible()
            binding.tvImportText.text = getString(R.string.text_imported_from_google_fit)
        } else {


            binding.lytHeartRate.root.gone()

            var movement = it.movement
            if (it.type.equals("userworkout", true)) {
                val maxHr = it.hrArray?.maxOrNull()
                setHrGraph(
                    it.hrArray,
                    it.hrAvg,
                    maxHr,
                    it.hrLow,
                    "${it.date} ${it.startTime}",
                    "${it.date} ${it.endTime}"
                )
                movement = mViewModel.getCombinedMovement(it.movement ?: ArrayList())
            }


            /*val movement = ArrayList<Int>()
            for (i in 0..959){
                val random = arrayListOf<Int>(0,1,2,3).random()
                movement.add(random)
            }*/


            setMovementGraph(
                it.intensityText,
                movement, DateFormats.convert24HourTo12(
                    it.startTime, SimpleDateFormat("HH:mm:ss", DateFormats.defaultLocale)
                ), DateFormats.convert24HourTo12(
                    it.endTime, SimpleDateFormat(
                        "HH:mm:ss", DateFormats.defaultLocale
                    )
                ),
                it.duration
            )

        }

        binding.lytToolbar.tvTitle.text = it.workoutText


        /* setMovementGraph(
             arrayListOf(0, 1, 2, 3, 2, 2, 1, 2), "12:03 pm","12:06 pm"
         )*/
    }

    private fun setHrGraph(
        hrArray: List<Int>?,
        hrAvg: Int?,
        hrMax: Int?,
        hrLow: Int?,
        startTime: String?,
        endTime: String?
    ) {
        binding.lytHeartRate.root.visible()
        binding.lytHeartRate.bInfo.gone()

        binding.lytHeartRate.tvTitle.text = getString(R.string.text_heart_rate)
        binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_max_hr)
        binding.lytHeartRate.tvSubtitle2.text = getString(R.string.text_average_hr)
        val heartRateData = hrArray
        if (heartRateData != null) {
            if (hrAvg != null) {
                if (hrAvg == 0 || hrAvg == 255) {
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
                } else {
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text =
                        hrAvg.toString()
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.text =
                        getString(R.string.text_bpm_small)
                }
            } else {
                binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
            }
            if (hrMax != null) {
                if (hrMax == 0 || hrMax == 255) {
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
                } else {
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text =
                        hrMax.toString()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.text =
                        getString(R.string.text_bpm_small)
                }
            } else {
                binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
            }
        } else {
            binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
            binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
            binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
        }
        showHeartRateGraph(
            CommonListDataModel(
                value = hrArray ?: ArrayList(),
                avg = hrAvg,
                low = hrLow,
            ), startTime, endTime
        )

    }

    private fun showHeartRateGraph(
        heartRateList: CommonListDataModel?,
        sleepStartTime: String?,
        sleepEndTime: String?,
    ) {
//        if ((heartRateList?.value?.size ?: 0) <= 1) {
//            binding.lytHeartRate.lineChart.gone()
//            return
//        }
        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Int>()
        var hasDummyData = true

        if (heartRateList?.value.isNullOrEmpty()) {
            breakUpData = mViewModel.getDummyBreakUpDataForTimeDisplay()
            ssTime = null
            seTime = null
        } else {
            hasDummyData = false
            seTime = sleepEndTime
            ssTime = sleepStartTime
            breakUpData = heartRateList?.value as ArrayList<Int>

            val filteredSize = heartRateList.value.filter { it != 0 && it != 255 }
            if (filteredSize.isEmpty()) {
                hasDummyData = true
            }
        }


        /* val baseTimeList = UtilClass.graphTwoHoursInterval(
             ssTime,
             seTime,
             breakUpData.size ?: 288
         )*/
        val baseTimeListNew =
            UtilClass.getXAxisPoints(ssTime, seTime, breakUpData.size ?: 288)

        // LOGS.d("dsakjdsalkjsladjlksdajldsajldsajl ${heartRateList?.value?.size} ${Gson().toJson(baseTimeList)}")

        binding.lytHeartRate.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()
        breakUpData.forEachIndexed { index, data ->
            val chartModel = ChartModel()

            var value = data
            if (value == 255) {
                value = 0
            }


            chartModel.value = value
            chartModel.index = baseTimeListNew[index]
            chartList.add(chartModel)
        }


        sleepChart.list = chartList
        binding.lytHeartRate.lineChart.updateGraphColor(
            Color.parseColor("#ff6b86"),
            Color.parseColor("#CCff6581"),
            Color.parseColor("#0Dff6581")
        )

        binding.lytHeartRate.lineChart.updateDataWithMax(
            sleepChart, 5, true, false,
            GraphDummyModel(
                hasDummyData, 40, 100
            ),
            heartRateList?.avg
        )

    }

    private fun setMovementGraph(
        intensity: String?,
        movementList: List<Int>?,
        startTime: String,
        endTime: String,
        duration: Long?
    ) {

        var hasNoData = false
        if (movementList == null) {
            hasNoData = true
        } else {
            val filteredData = movementList.filter { it != 0 && it != 255 }
            if (filteredData.isEmpty()) {
                hasNoData = true
            }
        }

        if (hasNoData) {
            binding.lytIntensity.textNoData.visible()
            binding.lytIntensity.tvIntensityType.invisible()
            binding.lytIntensity.backIntensity.invisible()
        } else {
            binding.lytIntensity.textNoData.gone()
            binding.lytIntensity.backIntensity.visible()
            binding.lytIntensity.tvIntensityType.visible()
            binding.lytIntensity.tvIntensityType.text = intensity?.capitalizeWords()
        }

        binding.divider1.root.visible()
        binding.lytIntensity.root.visible()

        val sleepDayGraphView = WorkoutIntensityGraphOreo(requireContext())
        binding.lytIntensity.graphView.removeAllViews()
        binding.lytIntensity.graphView.addView(sleepDayGraphView)

        sleepDayGraphView.init(false)

        sleepDayGraphView.setData(
            if (hasNoData) {
                ArrayList()
            } else movementList ?: ArrayList(),
            mViewModel.getXAxisList(
                if (hasNoData) {
                    ArrayList()
                } else movementList, startTime, endTime, duration
            )
        )

        sleepDayGraphView.invalidate()
    }


    private fun prepareDataForActivity(it: OWorkoutDetailsResponseModel) {
        val activityList = ArrayList<OWDActivityData>()
        val duration = ApplicationUtils.getActivityDurationFormat2Seconds(it.duration)
        activityList.add(
            OWDActivityData(
                getString(R.string.text_duration), duration, ""
            )
        )
        if (it.calories != null && it.calories > 0) {
            activityList.add(
                OWDActivityData(
                    getString(R.string.text_calories_burned),
                    it.calories.toString(),
                    getString(R.string.text_kcal),
                )
            )
        }
        if (it.steps != null && it.steps > 0) {
            activityList.add(
                OWDActivityData(
                    getString(R.string.text_steps),
                    it.steps.toString(),
                    "",
                )
            )
        }

        mAdapter.setDataSet(activityList)
    }

}