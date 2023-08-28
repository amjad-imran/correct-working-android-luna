package com.oreo.ui.readiness

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoReadinessBinding
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.health.CommonDataModel
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.sleep.OreoSleepContributorAdapter
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OreoReadinessFragment :
    BaseFragment<FragmentOreoReadinessBinding>(FragmentOreoReadinessBinding::inflate),
    ScrollListener {
    private val mViewModel: OreoReadinessViewModel by viewModels()
    private val mSharedViewModel: SharedOSCDViewModel by activityViewModels()


    private val mReadinessConAdapter: OreoSleepContributorAdapter by lazy {
        OreoSleepContributorAdapter(object :
            OreoSleepContributorAdapter.ContributorItemClickListener {
            override fun onItemClick(resultData: ArrayList<Contributors>, position: Int) {
//                if (resultData[position].barPercent > 0) {
                openContributorBottomSheet(resultData, position)
//                }
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


        if (mViewModel.ringDataStore.isReadinessWalkAroundShown()) {
            mViewModel.getReadinessDetailsData()
        } else {
            showWalkAround(true)
        }

    }

    private fun showWalkAround(show: Boolean) {
        if (show) {
            binding.lytEmptyView.root.visible()
            binding.svMain.gone()
        } else {
            binding.lytEmptyView.root.gone()
        }
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


    private fun showHeartRateGraph(heartRateData: List<Int>,
                                   startTime: String, endTime: String) {
        /*LOGS.d("showHeartRateGraph $startTime $endTime")
        var hasDummyData = true
        val breakUpData = if (heartRateData.isNullOrEmpty()) {
            mViewModel.getDummyBreakUpDataForTimeDisplay()
        } else {
            hasDummyData = false
            heartRateData
        }


        val baseHrList = UtilClass.graphBaseInterval(null, null, breakUpData.size)*/


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
            breakUpData = heartRateData as ArrayList<Int>
        }


        val baseTimeList = UtilClass.graphTwoHoursInterval(
            ssTime,
            seTime,
            breakUpData.size ?: 288
        )

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
            chartModel.index = baseTimeList[index]
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
            false, true,  GraphDummyModel(
                hasDummyData,40,100
            )
        )


    }

    private fun showHeartRateVariabilityGraph(
        hrvBreakUp: List<Int>,
        startTime: String,
        endTime: String
    ) {

       /* var hasDummyData = true

        val breakUpData = if (hrvBreakUp.isNullOrEmpty()) {
            mViewModel.getDummyBreakUpDataForTimeDisplay()
        } else{
            hasDummyData = false
            hrvBreakUp
        }

        val baseHrList = UtilClass.graphBaseInterval(null, null, breakUpData.size)
*/


        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Int>()
        var hasDummyData = true
        if (hrvBreakUp.isNullOrEmpty()) {
            breakUpData = mViewModel.getDummyBreakUpDataForTimeDisplay()
            ssTime = null
            seTime = null
        } else {
            hasDummyData = false
            seTime = endTime
            ssTime = startTime
            breakUpData = hrvBreakUp as ArrayList<Int>
        }


        val baseTimeList = UtilClass.graphTwoHoursInterval(
            ssTime,
            seTime,
            breakUpData.size ?: 288
        )

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
            chartModel.index = baseTimeList[index]
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
            true, false,  GraphDummyModel(
                hasDummyData,0,200
            )
        )

    }

    private fun showTemperatureGraph(
        temperatureBreakUp: List<Float>?,
        startTime: String,
        endTime: String
    ) {
     /*   var hasDummyData = true
        val breakUpData = if (temperatureBreakUp.isNullOrEmpty()) {
            mViewModel.getDummyBreakUpDataForTimeDisplay()
        } else{
            hasDummyData = false
            temperatureBreakUp

        }

        val baseHrList = UtilClass.graphBaseInterval(null, null, breakUpData.size ?: 0)
*/

        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Float>()
        var hasDummyData = true
        if (temperatureBreakUp.isNullOrEmpty()) {
            breakUpData = mViewModel.getDummyBreakUpDataForTimeDisplayFloat()
            ssTime = null
            seTime = null
        } else {
            hasDummyData = false
            seTime = endTime
            ssTime = startTime
            breakUpData = temperatureBreakUp as ArrayList<Float>
        }

        val baseTimeList = UtilClass.graphTwoHoursInterval(
            ssTime,
            seTime,
            breakUpData.size ?: 288
        )

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
            chartModel.index = baseTimeList[index]
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
            true, false,  GraphDummyModel(
                hasDummyData,80,110
            )
        )

    }

    private fun setRecycler() {
        binding.rvTopGraph.setOnChartScrollChangedListener(this)

        //  mAdapter.setData(mViewModel.getDummyData())
        with(binding.lytRContributor.rvContributor) {
            adapter = mReadinessConAdapter
        }


    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val selectedDate = data?.getStringExtra("selected_date")
                LOGS.d("Selected Date  :${selectedDate}")
                mViewModel.getReadinessDetailsData(selectedDate)
                if (selectedDate != null) {
                    mViewModel.updateSelectedDate(selectedDate)
                }

            }
        }

    override fun initListener() {
        binding.lytEmptyView.bGoToSettings.setOnClickListener {
            showWalkAround(false)
            mViewModel.ringDataStore.setReadinessWalkAroundShown(true)
            mViewModel.getReadinessDetailsData()
        }
        binding.lytToolbar.tvTitle.text = getString(R.string.text_readiness)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_calenders)
        binding.lytToolbar.backBtn.invisible()

        binding.lytToolbar.view1.setOnClickListener {
            resultLauncher.launch(
                HistoryCalendarActivity.getStartIntent(
                    requireContext(),
                    mViewModel.readinessHistoryResponse.value?.lastOrNull()?.date
                        ?: mSharedViewModel.selectedDate,
                    "ring"
                )
            )
        }

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

    private fun setReadinessScore(readinessData: CommonDataModel) {
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
            binding.lytRScoreData.lytScore.emptyText.gone()
            binding.lytRScoreData.lytScore.tvValue.visible()
            val readinessData = it.readinessScore
            if (readinessData.value != null) {
                if (readinessData.value == 0) {
                    if (it.totalSleep?.value != 0) {
                        setReadinessScore(readinessData)
                    } else {
                        binding.lytRScoreData.lytScore.tvValue.text = "-"
                        binding.lytRScoreData.lytScore.tvQuality.gone()
                    }
                } else {
                    setReadinessScore(readinessData)
                }
            } else {
                binding.lytRScoreData.lytScore.tvValue.text = "-"
                binding.lytRScoreData.lytScore.tvQuality.gone()

            }

        } else {
            if (mViewModel.ringDataStore.getRegisterDay() == 0) {
                binding.lytRScoreData.lytScore.emptyText.text =
                    getString(R.string.text_you_will_see_your_readiness_score_after_first_sleep_analysis)
                binding.lytRScoreData.lytScore.emptyText.visible()
                binding.lytRScoreData.lytScore.tvValue.gone()
                binding.lytRScoreData.lytScore.tvQuality.gone()
            } else {
                binding.lytRScoreData.lytScore.emptyText.gone()
                binding.lytRScoreData.lytScore.tvValue.text = "-"
                binding.lytRScoreData.lytScore.tvQuality.gone()
            }
        }
        //resting HR
        if (it.restingHr != null) {
            val rHrData = it.restingHr
            if (rHrData.value == 0 || rHrData.value == 255) {
                restHrDefaultView()
            } else {
                binding.lytRScoreData.lytSec1.lytBpmView.tvValue.text =
                    rHrData.value.toString()
                binding.lytRScoreData.lytSec1.lytHrMn.root.gone()
                binding.lytRScoreData.lytSec1.tvPercentValue.gone()
                binding.lytRScoreData.lytSec1.lytBpmView.root.visible()
                binding.lytRScoreData.lytSec1.lytBpmView.tvUnit.text = "bpm"
                binding.lytRScoreData.lytSec1.lytBpmView.tvUnit.visible()
            }

        } else {
            restHrDefaultView()
        }
        //hrv
        if (it.hrv != null) {
            val hrvData = it.hrv
            if (hrvData.value == 0 || hrvData.value == 255) {
                hrVariabilityDefaultView()
            } else {
                binding.lytRScoreData.lytSec2.lytHrMn.root.gone()
                binding.lytRScoreData.lytSec2.tvPercentValue.gone()
                binding.lytRScoreData.lytSec2.lytBpmView.root.visible()

                binding.lytRScoreData.lytSec2.lytBpmView.tvValue.text =
                    hrvData.value.toString()
                binding.lytRScoreData.lytSec2.lytBpmView.tvUnit.text = "ms"
                binding.lytRScoreData.lytSec2.lytBpmView.tvUnit.visible()
            }
        } else {
            hrVariabilityDefaultView()
        }
        //temperature
        if (it.temperature != null) {
            binding.lytRScoreData.lytSec3.lytHrMn.root.gone()
            binding.lytRScoreData.lytSec3.tvPercentValue.visible()
            binding.lytRScoreData.lytSec3.lytBpmView.root.gone()
            binding.lytRScoreData.lytSec3.tvPercentValue.text =
                "${it.temperature?.value} °F"
        } else {
            temperatureDefaultView()
        }

        //respiration
        if (it.respiration != null) {
            val resData = it.respiration
            if (resData.value == 0 || resData.value == 255) {
                respiratoryRateDefaultView()
            } else {
                binding.lytRScoreData.lytSec4.lytHrMn.root.gone()
                binding.lytRScoreData.lytSec4.tvPercentValue.gone()
                binding.lytRScoreData.lytSec4.lytBpmView.root.visible()

                binding.lytRScoreData.lytSec4.lytBpmView.tvValue.text =
                    resData.value.toString()
                binding.lytRScoreData.lytSec4.lytBpmView.tvUnit.text = "bpm"
                binding.lytRScoreData.lytSec4.lytBpmView.tvUnit.visible()
            }
        } else {
            respiratoryRateDefaultView()
        }

        //readiness contributor
        binding.lytRContributor.tvTitle.text = getString(R.string.text_readiness_contributor)
        mReadinessConAdapter.setData(mViewModel.getContributorsData(it))

        //set data on heart rate
        binding.lytHeartRate.tvTitle.text = getString(R.string.text_heart_rate)
        binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_lowest_hr)
        binding.lytHeartRate.tvSubtitle2.text = getString(R.string.text_average_hr)
        if (it.hrBreakUp != null) {
            if (!it.hrBreakUp.value.isNullOrEmpty()) {
                if (it.hrBreakUp.low == 0 || it.hrBreakUp.low == 255) {
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                } else {
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "${it.hrBreakUp.low}"
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
                }
                if (it.hrBreakUp.avg == 0 || it.hrBreakUp.avg == 255) {
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
                } else {
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "${it.hrBreakUp.avg}"
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.text = "bpm"
                }
            } else {
                heartRateDefaultView()
            }
        } else {
            heartRateDefaultView()
        }
        //todo will change startTime, endTime
        val sleepStartTime = DateFormats.formatDate(
            it.start_time,
            DateFormats.dateTimeFormat5,
            DateFormats.time12Meridian
        )
        val sleepEndTime = DateFormats.formatDate(
            it.end_time,
            DateFormats.dateTimeFormat5,
            DateFormats.time12Meridian
        )

        showHeartRateGraph(it.hrBreakUp?.value ?: ArrayList(),
            sleepStartTime, sleepEndTime)


        //set data on heart rate variability
        binding.lytHRVariability.tvTitle.text = getString(R.string.text_heart_rate_variability)
        binding.lytHRVariability.tvSubtitle1.text = getString(R.string.text_average_hrv)
        binding.lytHRVariability.tvSubtitle2.text = getString(R.string.text_max)
        if (it.hrvBreakUp != null) {
            if (!it.hrvBreakUp.value.isNullOrEmpty()) {
                if (it.hrvBreakUp.avg == 0 || it.hrvBreakUp.avg == 255) {
                    binding.lytHRVariability.lytSubtitleValue1.tvUnit.gone()
                    binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "-"
                } else {
                    binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "${it.hrvBreakUp.avg}"
                    binding.lytHRVariability.lytSubtitleValue1.tvUnit.visible()
                    binding.lytHRVariability.lytSubtitleValue1.tvUnit.text = "ms"
                }
                if (it.hrvBreakUp.max == 0 || it.hrvBreakUp.max == 255) {
                    binding.lytHRVariability.lytSubtitleValue2.tvUnit.gone()
                    binding.lytHRVariability.lytSubtitleValue2.tvValue.text = "-"
                } else {
                    binding.lytHRVariability.lytSubtitleValue2.tvValue.text = "${it.hrvBreakUp.max}"
                    binding.lytHRVariability.lytSubtitleValue2.tvUnit.visible()
                    binding.lytHRVariability.lytSubtitleValue2.tvUnit.text = "ms"
                }
            } else {
                hrvDefaultView()
            }
        } else {
            hrvDefaultView()
        }
        //todo will change startTime, endTime
        showHeartRateVariabilityGraph(
            it.hrvBreakUp?.value ?: ArrayList(),
            sleepStartTime, sleepEndTime
        )

        //set data on temperature
        binding.lytTemperature.tvTitle.text = getString(R.string.text_temperature)
        binding.lytTemperature.tvSubtitle1.text = getString(R.string.text_average)
        binding.lytTemperature.tvSubtitle2.gone()
        binding.lytTemperature.divider1.root.invisible()
        if (!it.temperatureBreakUp?.value.isNullOrEmpty()) {
            binding.lytTemperature.lytSubtitleValue1.tvValue.text = "${it.temperatureBreakUp?.avg}"
            binding.lytTemperature.lytSubtitleValue1.tvUnit.visible()
            binding.lytTemperature.lytSubtitleValue1.tvUnit.text = "°F"
        } else {
            temperatureGraphDefaultView()
        }
        //todo will change startTime, endTime
        showTemperatureGraph(it.temperatureBreakUp?.value ?: ArrayList(), sleepStartTime, sleepEndTime)
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