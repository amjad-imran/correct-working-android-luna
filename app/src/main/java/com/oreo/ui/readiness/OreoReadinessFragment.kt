package com.oreo.ui.readiness

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoReadinessBinding
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.common.averageWithoutZeroFloat
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.sleep.OreoSleepContributorAdapter
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import dagger.hilt.android.AndroidEntryPoint
import java.util.Collections


@AndroidEntryPoint
class OreoReadinessFragment :
    BaseFragment<FragmentOreoReadinessBinding>(FragmentOreoReadinessBinding::inflate),
    ScrollListener {
    private val mViewModel: OreoReadinessViewModel by viewModels()
    private val mSharedViewModel: SharedOSCDViewModel by activityViewModels()
    var currentItem: Int = 0

    private val mReadinessConAdapter: OreoSleepContributorAdapter by lazy {
        OreoSleepContributorAdapter(object :
            OreoSleepContributorAdapter.ContributorItemClickListener {
            override fun onItemClick(resultData: ArrayList<Contributors>, position: Int) {
                if (resultData[position].barPercent > 0) {
                    openContributorBottomSheet(resultData, position)
                }
            }

        })
    }

    private fun openContributorBottomSheet(resultData: ArrayList<Contributors>, position: Int) {
        val descList = mViewModel.prepareDataForDescriptionArray(resultData)
        navigate(
            OreoReadinessFragmentDirections.actionNavigationReadinessDetailsFragToDescriptionPopUpBottomDialogFragment(
                position, descList.toTypedArray(), resultData[position].title
            )
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        mViewModel.getReadinessDetailsData()
//        mViewModel.getReadinessTestData()

    }

    private fun setReadinessBannerViewPager(data: List<Nudges>?) {
        if (data.isNullOrEmpty()) {
            binding.lytRScoreData.lytAScoreBanner.root.gone()
            return
        } else {
            binding.lytRScoreData.lytAScoreBanner.root.visible()
        }
        val fragments = ArrayList<OreoReadinessBannerFragment>()
        data.forEach {
            fragments.add(OreoReadinessBannerFragment.newInstance(it))
        }
        val sleepBannerAdapter =
            OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytRScoreData.lytAScoreBanner.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = sleepBannerAdapter
        }

        TabLayoutMediator(
            binding.lytRScoreData.lytAScoreBanner.tabLayout,
            binding.lytRScoreData.lytAScoreBanner.vpBannerSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 0) {
            binding.lytRScoreData.lytAScoreBanner.tabLayout.visible()
        } else {
            binding.lytRScoreData.lytAScoreBanner.tabLayout.invisible()
        }
    }


    private fun showHeartRateGraph(heartRateData: List<Int>, startTime: String, endTime: String) {


        LOGS.d("showHeartRateGraph $startTime $endTime")
        if (heartRateData.isNullOrEmpty()) {
            binding.lytHeartRate.lineChart.gone()
            return
        }
        binding.lytHeartRate.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()
        heartRateData.forEach {
            val chartModel = ChartModel()

            var value = it
            if (value == 255) {
                value = 0
            }

            chartModel.value = value
            chartModel.index = ""
            chartList.add(chartModel)
        }
        sleepChart.startTime = startTime ?: ""
        sleepChart.endTime = endTime ?: ""
        sleepChart.list = chartList

        binding.lytHeartRate.lineChart.updateGraphColor(
            Color.parseColor("#ff3358"),
            Color.parseColor("#4cff3358"),
            Color.parseColor("#00ff3358")
        )

        binding.lytHeartRate.lineChart.updateDataWithMax(sleepChart, 5, false, true)


    }

    private fun showHeartRateVariabilityGraph(
        hrvBreakUp: List<Int>,
        startTime: String,
        endTime: String
    ) {


        if (hrvBreakUp.isNullOrEmpty()) {
            binding.lytHRVariability.lineChart.gone()
            return
        }
        binding.lytHRVariability.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()
        hrvBreakUp.forEach {
            val chartModel = ChartModel()

            var value = it
            if (value == 255) {
                value = 0
            }

            chartModel.value = value
            chartModel.index = ""
            chartList.add(chartModel)
        }
        sleepChart.startTime = startTime ?: ""
        sleepChart.endTime = endTime ?: ""
        sleepChart.list = chartList

        binding.lytHRVariability.lineChart.updateGraphColor(
            Color.parseColor("#ff59da"),
            Color.parseColor("#4cff59da"),
            Color.parseColor("#00ff59da")
        )

        binding.lytHRVariability.lineChart.updateDataWithMax(sleepChart, 5, true, false)

    }

    private fun showTemperatureGraph(
        temperatureBreakUp: List<Float>,
        startTime: String,
        endTime: String
    ) {


        if (temperatureBreakUp.isNullOrEmpty()) {
            binding.lytTemperature.lineChart.gone()
            return
        }
        binding.lytTemperature.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()
        temperatureBreakUp.forEach {
            val chartModel = ChartModel()

            var value = it
            if (value.toInt() == 255) {
                value = 0F
            }

            chartModel.value = value.toInt()
            chartModel.index = ""
            chartList.add(chartModel)
        }
        sleepChart.startTime = startTime ?: ""
        sleepChart.endTime = endTime ?: ""
        sleepChart.list = chartList

        LOGS.d("sdasdaasdadsdas ${ sleepChart.list.size}")
        binding.lytTemperature.lineChart.updateGraphColor(
            Color.parseColor("#ff7525"),
            Color.parseColor("#4cff7525"),
            Color.parseColor("#00ff7525")
        )


        binding.lytTemperature.lineChart.updateDataWithMax(sleepChart, 5, true, false)

    }

    private fun setRecycler() {
        binding.rvTopGraph.setOnChartScrollChangedListener(this)

        //  mAdapter.setData(mViewModel.getDummyData())
        with(binding.lytRContributor.rvContributor) {
            adapter = mReadinessConAdapter
        }


    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_readiness)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_calenders)
        binding.lytToolbar.backBtn.invisible()

        binding.lytRScoreData.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.READINESS.name
            mSharedViewModel.itemClickType = ViewItemClickType.READINESS_SCORE.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "readiness")
            })
        }
        binding.lytRScoreData.lytSec1.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.READINESS.name
            mSharedViewModel.itemClickType = ViewItemClickType.RESTING_HR.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "readiness")
            })
        }
        binding.lytRScoreData.lytSec2.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.READINESS.name
            mSharedViewModel.itemClickType = ViewItemClickType.HR_VARIABILITY.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "readiness")
            })
        }
        binding.lytRScoreData.lytSec3.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.READINESS.name
            mSharedViewModel.itemClickType = ViewItemClickType.BODY_TEMPERATURE.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "readiness")
            })
        }
        binding.lytRScoreData.lytSec4.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.READINESS.name
            mSharedViewModel.itemClickType = ViewItemClickType.RESPIRATORY_RATE.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "readiness")
            })
        }


    }

    override fun subscribeObservers() {
        mViewModel.readinessHistoryResponse.observe(this) {
            binding.svMain.visible()
            val topGraphData = mViewModel.getPrefixAndSuffixList(it)
            mSharedViewModel.selectedDate = mViewModel.dateList[mViewModel.dateList.size - 1]
            binding.rvTopGraph.updateDataWithMax(
                topGraphData.first,
                topGraphData.third,
                topGraphData.second
            )
            setScrollDate()
            mViewModel.getContributorInfo()

        }
        mViewModel.dayReadinessData.observe(this) {
            updateUiRead(it)
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
        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

    }

    private fun updateUiRead(it: OreoReadinessModel) {
        binding.lytRScoreData.lytScore.tvTitle.text = getString(R.string.text_readiness_score)
        binding.lytRScoreData.lytSec1.tvTitle.text = getString(R.string.text_resting_hr)
        binding.lytRScoreData.lytSec2.tvTitle.text = getString(R.string.text_hr_variability)
        binding.lytRScoreData.lytSec3.tvTitle.text = getString(R.string.text_body_tempreature)
        binding.lytRScoreData.lytSec4.tvTitle.text = getString(R.string.text_respiratory_rate)

        setReadinessBannerViewPager(it.nudges)

        //readiness score
        if (it.readinessScore != null) {
            val readinessData = it.readinessScore
            if (readinessData.value != null) {
                binding.lytRScoreData.lytScore.tvValue.text =
                    readinessData.value.toString()
                if (readinessData.text != null) {
                    val statusColor = ContextCompat.getColor(
                        binding.lytRScoreData.lytScore.tvValue.context,
                        mViewModel.getStatusColors(readinessData.status)
                    )
                    binding.lytRScoreData.lytScore.tvQuality.setTextColor(statusColor)
                    binding.lytRScoreData.lytScore.tvQuality.text = readinessData.text
                    binding.lytRScoreData.lytScore.tvQuality.visible()
                } else {
                    binding.lytRScoreData.lytScore.tvQuality.gone()
                }
            } else {
                binding.lytRScoreData.lytScore.tvValue.text = "-"
                binding.lytRScoreData.lytScore.tvQuality.gone()
            }

        } else {
            binding.lytRScoreData.lytScore.tvValue.text = "-"
            binding.lytRScoreData.lytScore.tvQuality.gone()
        }
        //resting HR
        if (it.restingHr != null) {
            val rHrData = it.restingHr
            binding.lytRScoreData.lytSec1.lytBpmView.tvValue.text =
                rHrData.value.toString()
            binding.lytRScoreData.lytSec1.lytHrMn.root.gone()
            binding.lytRScoreData.lytSec1.tvPercentValue.gone()
            binding.lytRScoreData.lytSec1.lytBpmView.root.visible()
            binding.lytRScoreData.lytSec1.lytBpmView.tvUnit.text = "bpm"
            binding.lytRScoreData.lytSec1.lytBpmView.tvUnit.visible()

        } else {
            restHrDefaultView()
        }
        //hrv
        if (it.hrv != null) {
            val hrvData = it.hrv
            binding.lytRScoreData.lytSec2.lytHrMn.root.gone()
            binding.lytRScoreData.lytSec2.tvPercentValue.gone()
            binding.lytRScoreData.lytSec2.lytBpmView.root.visible()

            binding.lytRScoreData.lytSec2.lytBpmView.tvValue.text =
                hrvData.value.toString()
            binding.lytRScoreData.lytSec2.lytBpmView.tvUnit.text = "ms"
            binding.lytRScoreData.lytSec2.lytBpmView.tvUnit.visible()
        } else {
            hrVariabilityDefaultView()
        }
        //temperature
        if (it.temperature != null) {
//            val tempData = it.temperature
            binding.lytRScoreData.lytSec3.lytHrMn.root.gone()
            binding.lytRScoreData.lytSec3.tvPercentValue.visible()
            binding.lytRScoreData.lytSec3.lytBpmView.root.gone()

//            val temperatureData =
//                if (tempData.unit == "°C") MiscUtil.getCelsius(tempData.value.toString())
//                else MiscUtil.getFahrenheit(
//                    tempData.value.toString()
//                )
            binding.lytRScoreData.lytSec3.tvPercentValue.text =
                "${it.temperature?.value} °F"
        } else {
            temperatureDefaultView()
        }

        //respiration
        if (it.respiration != null) {
            val resData = it.respiration
            binding.lytRScoreData.lytSec4.lytHrMn.root.gone()
            binding.lytRScoreData.lytSec4.tvPercentValue.gone()
            binding.lytRScoreData.lytSec4.lytBpmView.root.visible()

            binding.lytRScoreData.lytSec4.lytBpmView.tvValue.text =
                resData.value.toString()
            binding.lytRScoreData.lytSec4.lytBpmView.tvUnit.text = "bpm"
            binding.lytRScoreData.lytSec4.lytBpmView.tvUnit.visible()
        } else {
            respiratoryRateDefaultView()
        }

        //readiness contributor
        binding.lytRContributor.tvTitle.text = getString(R.string.text_readiness_contributor)
        mReadinessConAdapter.setData(mViewModel.getContributorsData(it))

        //set data on heart rate
        binding.lytHeartRate.tvTitle.text = getString(R.string.text_heart_rate)
        binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_lowest_heart_rate)
        binding.lytHeartRate.tvSubtitle2.text = getString(R.string.text_average)

        if (it.hrBreakUp != null) {
            if (it.hrBreakUp.isNotEmpty()) {
                val minHrIndex: Int = it.hrBreakUp.indexOf(Collections.min(it.hrBreakUp))
                val minHrValue: Int = it.hrBreakUp[minHrIndex]
                val avgHrValue = it.hrBreakUp.averageWithoutZero()
                binding.lytHeartRate.lytSubtitleValue1.tvValue.text = minHrValue.toString()
                binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
                binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
                binding.lytHeartRate.lytSubtitleValue2.tvValue.text = avgHrValue.toString()
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.visible()
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.text = "bpm"
                //todo will change startTime, endTime
                showHeartRateGraph(it.hrBreakUp, it.date, it.date)
            } else {
                binding.lytHeartRate.lineChart.gone()
                heartRateDefaultView()
            }
        } else {
            binding.lytHeartRate.lineChart.gone()
            heartRateDefaultView()
        }

        //set data on heart rate variability
        binding.lytHRVariability.tvTitle.text = getString(R.string.text_heart_rate_variability)
        binding.lytHRVariability.tvSubtitle1.text = getString(R.string.text_average_hrv)
        binding.lytHRVariability.tvSubtitle2.text = getString(R.string.text_max)

        if (it.hrvBreakUp != null) {
            if (it.hrvBreakUp.isNotEmpty()) {
                val maxHrvIndex: Int = it.hrvBreakUp.indexOf(Collections.max(it.hrvBreakUp))
                val maxHrvValue: Int = it.hrvBreakUp[maxHrvIndex]
                val avgHrvValue = it.hrvBreakUp.averageWithoutZero()
                binding.lytHRVariability.lytSubtitleValue1.tvValue.text = avgHrvValue.toString()
                binding.lytHRVariability.lytSubtitleValue1.tvUnit.visible()
                binding.lytHRVariability.lytSubtitleValue1.tvUnit.text = "ms"
                binding.lytHRVariability.lytSubtitleValue2.tvValue.text = maxHrvValue.toString()
                binding.lytHRVariability.lytSubtitleValue2.tvUnit.visible()
                binding.lytHRVariability.lytSubtitleValue2.tvUnit.text = "ms"
                //todo will change startTime, endTime
                showHeartRateVariabilityGraph(it.hrvBreakUp, it.date, it.date)

            } else {
                binding.lytHRVariability.lineChart.gone()
                hrvDefaultView()
            }
        } else {
            binding.lytHRVariability.lineChart.gone()
            hrvDefaultView()
        }

        //set data on temperature
        binding.lytTemperature.tvTitle.text = getString(R.string.text_temperature)
        binding.lytTemperature.tvSubtitle1.text = getString(R.string.text_average)
        binding.lytTemperature.tvSubtitle2.gone()
        binding.lytTemperature.divider1.root.invisible()


        if (!it.temperatureBreakUp.isNullOrEmpty()) {
            val avgHrvValue = it.temperatureBreakUp.averageWithoutZeroFloat()
            binding.lytTemperature.lytSubtitleValue1.tvValue.text = avgHrvValue.toString()
            binding.lytTemperature.lytSubtitleValue1.tvUnit.visible()
            binding.lytTemperature.lytSubtitleValue1.tvUnit.text = "°F"
            //todo will change startTime, endTime
            showTemperatureGraph(it.temperatureBreakUp, it.date, it.date)

        } else {
            binding.lytTemperature.lineChart.gone()
            temperatureGraphDefaultView()
        }


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


    private fun setScrollDate() {
    }

    private fun respiratoryRateDefaultView() {
        binding.lytRScoreData.lytSec4.lytHrMn.root.gone()
        binding.lytRScoreData.lytSec4.tvPercentValue.gone()
        binding.lytRScoreData.lytSec4.lytBpmView.root.visible()

        binding.lytRScoreData.lytSec4.lytBpmView.tvValue.text = "-"
        binding.lytRScoreData.lytSec4.lytBpmView.tvUnit.gone()
    }

    private fun temperatureDefaultView() {
        binding.lytRScoreData.lytSec3.lytHrMn.root.gone()
        binding.lytRScoreData.lytSec3.tvPercentValue.visible()
        binding.lytRScoreData.lytSec3.lytBpmView.root.gone()

        binding.lytRScoreData.lytSec3.tvPercentValue.text = "-"
    }

    private fun hrVariabilityDefaultView() {
        binding.lytRScoreData.lytSec2.lytHrMn.root.gone()
        binding.lytRScoreData.lytSec2.tvPercentValue.gone()
        binding.lytRScoreData.lytSec2.lytBpmView.root.visible()

        binding.lytRScoreData.lytSec2.lytBpmView.tvValue.text = "-"
        binding.lytRScoreData.lytSec2.lytBpmView.tvUnit.gone()
    }

    private fun restHrDefaultView() {
        binding.lytRScoreData.lytSec1.lytHrMn.root.gone()
        binding.lytRScoreData.lytSec1.tvPercentValue.gone()
        binding.lytRScoreData.lytSec1.lytBpmView.root.visible()
        binding.lytRScoreData.lytSec1.lytBpmView.tvValue.text = "-"
        binding.lytRScoreData.lytSec1.lytBpmView.tvUnit.gone()
    }


    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {
        mSharedViewModel.selectedDate = chartModel?.date!!
        chartModel.date?.let { mViewModel.updateSelectedDate(it) }


    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }
}