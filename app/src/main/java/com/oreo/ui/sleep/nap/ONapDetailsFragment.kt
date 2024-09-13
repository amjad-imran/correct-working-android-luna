package com.oreo.ui.sleep.nap

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentONapDetailsBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.custom.SleepProgressbarView
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.OreoNapDetailsDataModel
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.health.Nudges
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ONapDetailsFragment :
    BaseFragment<FragmentONapDetailsBinding>(FragmentONapDetailsBinding::inflate) {
    private val mViewModel: ONapDetailsViewModel by viewModels()
    private val navArgs: ONapDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.getUserNapData(navArgs.napId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initListener() {
        binding.lytNapTopView.lytToolbar.tvTitle.text = getString(R.string.text_nap)
        binding.lytNapTopView.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }


    private fun setNapBannerViewPager(data: List<Nudges>?) {

        if (data.isNullOrEmpty()) {
            binding.lytNudge.root.gone()
            return
        } else {
            binding.lytNudge.root.visible()
        }
        val fragments = ArrayList<ONapBannerFragment>()
        data.forEach {
            fragments.add(ONapBannerFragment.newInstance(it))
        }
        val sleepBannerAdapter =
            OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytNudge.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = sleepBannerAdapter
        }

        TabLayoutMediator(
            binding.lytNudge.tabLayout,
            binding.lytNudge.vpBannerSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 1) {
            binding.lytNudge.tabLayout.visible()
        } else {
            binding.lytNudge.tabLayout.invisible()
        }
    }

    private fun updateUi(it: OreoNapDetailsDataModel) {
        //nap sleep score
        if (it.sleepScore == 0) {
            binding.lytNapTopView.lytImpact.root.gone()

            binding.lytNapTopView.lytImpactNoData.apply {
                tvNotAvail.text = it.na?.title ?: getString(R.string.text_not_available)
                tvNapScoreMsg.setVisibilityByCondition(it.na?.text.isNullOrEmpty().not())
                tvNapScoreMsg.text = it.na?.text
                root.visible()
            }

        } else {
            binding.lytNapTopView.lytImpact.root.visible()
            binding.lytNapTopView.lytImpactNoData.root.gone()

            handleSleepScoreUi(it)
            handleReadinessScoreUi(it)
        }


        //nap details
        val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(
            it.duration?.toInt() ?: 0
        )
        if (hour > 0) {
            binding.lytNapDetails.tvHour.text = "$hour"
            binding.lytNapDetails.tvMinute.text = "$minute"
        } else {
            binding.lytNapDetails.tvHour.gone()
            binding.lytNapDetails.tvHourUnit.gone()
            binding.lytNapDetails.tvMinute.text = "$minute"
        }

        //set nap progress
        val sleepDayGraphView = SleepProgressbarView(binding.lytNapDetails.napPrg.context)
        binding.lytNapDetails.napPrg.removeAllViews()
        binding.lytNapDetails.napPrg.addView(sleepDayGraphView)
        sleepDayGraphView.setData(mViewModel.getNapArrayData((it.duration?.toInt() ?: 0) * 60))

        binding.lytNapDetails.tvNapStart.text = DateFormats.parseDate(
            it.startTime,
            DateFormats.dateTimeFormat5(),
            DateFormats.timeFormat12()
        )?.lowercase()
        binding.lytNapDetails.tvNapEnd.text = DateFormats.parseDate(
            it.endTime,
            DateFormats.dateTimeFormat5(),
            DateFormats.timeFormat12()
        )?.lowercase()

        if ((it.sleepScore ?: 0) == 0 && (it.readinessScore ?: 0) == 0 && (it.prevSleepScore
                ?: 0) == 0 && (it.prevReadinessScore ?: 0) == 0
        ) {
            binding.lytNapTopView.lytImpact.root.gone()
            binding.lytNapTopView.rootView.setBackgroundResource(0)
        } else {
            binding.lytNapTopView.lytImpact.root.visible()
        }

        //nap nudges
        setNapBannerViewPager(it.nudges)
        //set data on heart rate
        binding.lytHeartRate.bInfo.invisible()
        binding.lytHeartRate.tvTitle.text = getString(R.string.text_heart_rate)
        binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_lowest_hr)
        binding.lytHeartRate.tvSubtitle2.text = getString(R.string.text_average_hr)
        val isHrDataNull = (it.hrBreakup?.avg ?: 0) == 0

        if (!isHrDataNull) {
            if (!it.hrBreakup?.value.isNullOrEmpty()) {
                if (it.hrBreakup?.low == 0 || it.hrBreakup?.low == 255) {
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                } else {
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "${it.hrBreakup?.low}"
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
                }
                if (it.hrBreakup?.avg == 0 || it.hrBreakup?.avg == 255) {
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
                } else {
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "${it.hrBreakup?.avg}"
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.text = "bpm"
                }
            } else {
                heartRateDefaultView()
            }
        } else {
            heartRateDefaultView()
        }
        val sleepStartTime = it.startTime
        val sleepEndTime = it.endTime

        showHeartRateGraph(
            if (isHrDataNull) null else it.hrBreakup?.value,
            it.hrBreakup?.avg ?: 0,
            sleepStartTime, sleepEndTime
        )

        //set data on heart rate variability
        binding.lytHRVariability.bInfo.invisible()
        binding.lytHRVariability.tvTitle.text = getString(R.string.text_heart_rate_variability)
        binding.lytHRVariability.tvSubtitle1.text = getString(R.string.text_average_hrv)
        binding.lytHRVariability.tvSubtitle2.text = getString(R.string.text_max)

        val isHrvDataNull = (it.hrvBreakUp?.avg ?: 0) == 0

        if (!isHrvDataNull) {
            if (!it.hrvBreakUp?.value.isNullOrEmpty()) {
                if (it.hrvBreakUp?.avg == 0 || it.hrvBreakUp?.avg == 255) {
                    binding.lytHRVariability.lytSubtitleValue1.tvUnit.gone()
                    binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "-"
                } else {
                    binding.lytHRVariability.lytSubtitleValue1.tvValue.text =
                        "${it.hrvBreakUp?.avg}"
                    binding.lytHRVariability.lytSubtitleValue1.tvUnit.visible()
                    binding.lytHRVariability.lytSubtitleValue1.tvUnit.text = "ms"
                }
                if (it.hrvBreakUp?.max == 0 || it.hrvBreakUp?.max == 255) {
                    binding.lytHRVariability.lytSubtitleValue2.tvUnit.gone()
                    binding.lytHRVariability.lytSubtitleValue2.tvValue.text = "-"
                } else {
                    binding.lytHRVariability.lytSubtitleValue2.tvValue.text =
                        "${it.hrvBreakUp?.max}"
                    binding.lytHRVariability.lytSubtitleValue2.tvUnit.visible()
                    binding.lytHRVariability.lytSubtitleValue2.tvUnit.text = "ms"
                }
            } else {
                hrvDefaultView()
            }
        } else {
            hrvDefaultView()
        }
        showHeartRateVariabilityGraph(
            if (isHrvDataNull) null else it.hrvBreakUp?.value,
            it.hrvBreakUp?.avg ?: 0,
            sleepStartTime, sleepEndTime
        )

        //set data on temperature
        binding.lytTemperature.bInfo.invisible()
        binding.lytTemperature.tvTitle.text = getString(R.string.text_temperature)
        binding.lytTemperature.tvSubtitle1.text = getString(R.string.text_average)
        binding.lytTemperature.tvSubtitle2.gone()
        binding.lytTemperature.divider1.root.invisible()

        val isTempDataNull = (it.temperatureBreakup?.avg ?: 0) == 0.0f


        if (!isTempDataNull) {
            binding.lytTemperature.lytSubtitleValue1.tvValue.text = "${it.temperatureBreakup?.avg}"
            binding.lytTemperature.lytSubtitleValue1.tvUnit.visible()
            binding.lytTemperature.lytSubtitleValue1.tvUnit.text = "°F"
        } else {
            temperatureGraphDefaultView()
        }
        showTemperatureGraph(
            if (isTempDataNull) null else it.temperatureBreakup?.value,
            sleepStartTime,
            sleepEndTime
        )
    }

    private fun handleReadinessScoreUi(it: OreoNapDetailsDataModel) {

        if (it.prevReadinessScore == 0) {//only nap case
            binding.lytNapTopView.lytImpact.tvOldRScore.text = "${it.readinessScore ?: 0}"
            binding.lytNapTopView.lytImpact.tvNewRScore.invisible()
            binding.lytNapTopView.lytImpact.ivArrow2.invisible()
            binding.lytNapTopView.lytImpact.tvDiffRScore.invisible()

            mViewModel.setTextGradient(
                binding.lytNapTopView.lytImpact.tvOldRScore,
                requireActivity().getColor(R.color.white_12_70),
                requireActivity().getColor(R.color.nap_readiness_grad_end),
                requireActivity().getColor(R.color.nap_readiness_grad_start)
            )
        } else {
            //nap readiness score
            binding.lytNapTopView.lytImpact.tvOldRScore.text = "${it.prevReadinessScore ?: 0}"
            binding.lytNapTopView.lytImpact.tvNewRScore.text = "${it.readinessScore ?: 0}"
            mViewModel.setTextGradient(
                binding.lytNapTopView.lytImpact.tvNewRScore,
                requireActivity().getColor(R.color.white_12_70),
                requireActivity().getColor(R.color.nap_readiness_grad_end),
                requireActivity().getColor(R.color.nap_readiness_grad_start)
            )

            val diffRScore: String
            val preFix2: String
            val newRScore = it.readinessScore
            val oldRScore = it.prevReadinessScore
            if (oldRScore != null && newRScore != null) {
                if (newRScore > oldRScore) {
                    diffRScore = (newRScore - oldRScore).toString()
                    preFix2 = "+"
                    binding.lytNapTopView.lytImpact.tvDiffRScore.setTextColor(
                        binding.lytNapTopView.lytImpact.tvDiffSScore.context.getColor(
                            R.color.steps_arc
                        )
                    )
                } else {
                    diffRScore = (oldRScore - newRScore).toString()
                    preFix2 = "-"
                    binding.lytNapTopView.lytImpact.tvDiffRScore.setTextColor(
                        binding.lytNapTopView.lytImpact.tvDiffSScore.context.getColor(
                            R.color.calories_arc
                        )
                    )
                }
                binding.lytNapTopView.lytImpact.tvDiffRScore.visible()
                binding.lytNapTopView.lytImpact.tvDiffRScore.text = "$preFix2$diffRScore"
            } else {
                binding.lytNapTopView.lytImpact.tvDiffRScore.invisible()
            }
        }
    }

    private fun handleSleepScoreUi(it: OreoNapDetailsDataModel) {

        if (it.prevSleepScore == 0) {//only nap case
            binding.lytNapTopView.lytImpact.tvOldSScore.text = "${it.sleepScore ?: 0}"
            binding.lytNapTopView.lytImpact.tvNewSScore.invisible()
            binding.lytNapTopView.lytImpact.ivArrow1.invisible()
            binding.lytNapTopView.lytImpact.tvDiffSScore.invisible()

            mViewModel.setTextGradient(
                binding.lytNapTopView.lytImpact.tvOldSScore,
                requireActivity().getColor(R.color.white_12_70),
                requireActivity().getColor(R.color.nap_sleep_grad_end),
                requireActivity().getColor(R.color.nap_sleep_grad_start)
            )
        } else {
            binding.lytNapTopView.lytImpact.tvOldSScore.text = "${it.prevSleepScore ?: 0}"
            binding.lytNapTopView.lytImpact.tvNewSScore.text = "${it.sleepScore ?: 0}"
            mViewModel.setTextGradient(
                binding.lytNapTopView.lytImpact.tvNewSScore,
                requireActivity().getColor(R.color.white_12_70),
                requireActivity().getColor(R.color.nap_sleep_grad_end),
                requireActivity().getColor(R.color.nap_sleep_grad_start)
            )
            val diffScore: String
            val preFix: String
            val newSScore = it.sleepScore
            val oldSScore = it.prevSleepScore
            var isScoreGreater = false
            if (oldSScore != null && newSScore != null) {
                if (newSScore > oldSScore) {
                    diffScore = (newSScore - oldSScore).toString()
                    preFix = "+"
                    binding.lytNapTopView.lytImpact.tvDiffSScore.setTextColor(
                        binding.lytNapTopView.lytImpact.tvDiffSScore.context.getColor(
                            R.color.steps_arc
                        )
                    )
                    isScoreGreater = true
                } else {
                    diffScore = (oldSScore - newSScore).toString()
                    preFix = "-"
                    binding.lytNapTopView.lytImpact.tvDiffSScore.setTextColor(
                        binding.lytNapTopView.lytImpact.tvDiffSScore.context.getColor(
                            R.color.nap_down
                        )
                    )
                    isScoreGreater = false
                }
                binding.lytNapTopView.lytImpact.tvDiffSScore.visible()
                binding.lytNapTopView.lytImpact.tvDiffSScore.text = "$preFix$diffScore"
            } else {
                binding.lytNapTopView.lytImpact.tvDiffSScore.invisible()
            }
            if (isScoreGreater) {
                binding.lytNapTopView.rootView.setBackgroundResource(R.drawable.ic_nap_top_bg_positive)
            } else {
                binding.lytNapTopView.rootView.setBackgroundResource(R.drawable.ic_nap_top_bg_negative)
            }
        }

    }

    private fun showHeartRateGraph(
        heartRateData: List<Int>?,
        avg: Int,
        startTime: String?, endTime: String?
    ) {
        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Int>()
        var hasDummyData = true
        if (heartRateData.isNullOrEmpty()) {
            breakUpData = mViewModel.getDummyBreakUpDataForTimeDisplay()
            ssTime = null
            seTime = null
        } else {
            hasDummyData = false
            seTime = endTime
            ssTime = startTime
            breakUpData.addAll(heartRateData)
        }


        /*  val baseTimeList = UtilClass.graphTwoHoursInterval(
              ssTime,
              seTime,
              breakUpData.size ?: 288
          )*/

        val baseTimeListNew =
            UtilClass.getXAxisPoints(ssTime, seTime, breakUpData.size ?: 288)

        binding.lytHeartRate.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()
        breakUpData.forEachIndexed { index, it ->
            val chartModel = ChartModel()

            var value = it
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
            sleepChart, 5,
            false, true, GraphDummyModel(
                hasDummyData, 40, 100
            ),
            avg
        )


    }

    private fun showHeartRateVariabilityGraph(
        hrvBreakUpData: List<Int>?,
        avg: Int,
        startTime: String,
        endTime: String
    ) {


        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Int>()
        var hasDummyData = false
        if (hrvBreakUpData.isNullOrEmpty()) {
            breakUpData = mViewModel.getDummyBreakUpDataForTimeDisplay()
            ssTime = null
            seTime = null
            hasDummyData = true
        } else {
            hasDummyData = false
            seTime = endTime
            ssTime = startTime
            breakUpData.addAll(hrvBreakUpData)
        }

        val baseTimeListNew =
            UtilClass.getXAxisPoints(ssTime, seTime, breakUpData.size ?: 288)

        binding.lytHRVariability.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()

        breakUpData.forEachIndexed { index, it ->
            val chartModel = ChartModel()

            var value = it
            if (value == 255) {
                value = 0
            }

            chartModel.value = value
            chartModel.index = baseTimeListNew[index]
            chartList.add(chartModel)
        }

        sleepChart.list = chartList

        binding.lytHRVariability.lineChart.updateGraphColor(
            Color.parseColor("#ff80e3"),
            Color.parseColor("#CCff59da"),
            Color.parseColor("#0Dff59da")
        )

        binding.lytHRVariability.lineChart.updateDataWithMax(
            sleepChart, 5,
            true, false, GraphDummyModel(
                hasDummyData, 0, 200
            ),
            avg
        )

    }

    private fun showTemperatureGraph(
        temperatureBreakUpData: List<Float>?,
        startTime: String?,
        endTime: String?
    ) {
        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Float>()
        var hasDummyData = true
        if (temperatureBreakUpData.isNullOrEmpty()) {
            breakUpData = mViewModel.getDummyBreakUpDataForTimeDisplayFloat()
            ssTime = null
            seTime = null
        } else {
            hasDummyData = false
            seTime = endTime
            ssTime = startTime
            breakUpData.addAll(temperatureBreakUpData)
        }

        val baseTimeListNew =
            UtilClass.getXAxisPoints(ssTime, seTime, breakUpData.size ?: 288)

        binding.lytTemperature.lineChart.visible()
        val chartList = ArrayList<ChartModel>()
        val sleepChart = SleepChartModel()
        breakUpData.forEachIndexed { index, it ->
            val chartModel = ChartModel()

            var value = it
            if (value.toInt() == 255) {
                value = 0F
            }

            chartModel.value = value.toInt()
            chartModel.index = baseTimeListNew[index]
            chartList.add(chartModel)
        }

        sleepChart.list = chartList

        binding.lytTemperature.lineChart.updateGraphColor(
            Color.parseColor("#ff9659"),
            Color.parseColor("#4cff6624"),
            Color.parseColor("#00ff6624")
        )


        binding.lytTemperature.lineChart.updateDataWithMax(
            sleepChart, 5,
            true, false, GraphDummyModel(
                hasDummyData, 80, 110
            ),
            null
        )

    }

    private fun heartRateDefaultView() {
        binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
        binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
        binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
        binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
    }

    private fun hrvDefaultView() {
        binding.lytHRVariability.lytSubtitleValue1.tvUnit.gone()
        binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "-"
        binding.lytHRVariability.lytSubtitleValue2.tvUnit.gone()
        binding.lytHRVariability.lytSubtitleValue2.tvValue.text = "-"
    }

    private fun temperatureGraphDefaultView() {
        binding.lytTemperature.lytSubtitleValue1.tvUnit.gone()
        binding.lytTemperature.lytSubtitleValue1.tvValue.text = "-"
    }

    override fun subscribeObservers() {
        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }


        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        mViewModel.napDetailsResponse.observe(this) {
            if (it != null) {
                binding.viewMain.visible()
                updateUi(it)
            }
        }
    }

}