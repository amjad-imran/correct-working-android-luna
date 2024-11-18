package com.oreo.ui.sleep

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentBodyTempScoreDetailBinding
import com.noisefit_commans.common.setCompoundDrawable
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChartModel
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.ResultData
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.sleep.scoredetails.OSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class BodyTempScoreDetailFragment :
    BaseFragment<FragmentBodyTempScoreDetailBinding>(FragmentBodyTempScoreDetailBinding::inflate),
    ScrollListener {

    private val mViewModel: OSCDViewModel by viewModels()
    private val args: BodyTempScoreDetailFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_body_temp_page_visit)

        binding.toolbar.tvTitle.text = binding.root.context.getString(R.string.text_skin_temperature)
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.toolbar.view1.setOnClickListener {
            mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_body_temp_info_click)
            args.infoData?.let { data ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", data)
                })
            }
        }
        binding.toolbar.view1.visible()
        binding.toolbar.ivAddFriend.invisible()
        binding.toolbar.view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)


        mViewModel.dayType = "Day"
        mViewModel.selectedDate = args.date
        mViewModel.itemClickType = ViewItemClickType.AVG_TEMP.name

        mViewModel.getReadinessInternalDetailsData()
    }

    private fun initUi(it: OInternalPageResponseModal) {

        val firstData = it.result?.firstOrNull()

        firstData?.let {
            binding.tvAvgOn.text = getString(
                R.string.text_deviation_on_value, DateFormats.parseDate(
                    it.date,
                    DateFormats.dateFormat3(),
                    DateFormats.dateFormat7()
                )
            )



            binding.tvDeviationValue.text = if (it.data == 0.0f) {
                "-"
            } else {
                val deviationString = StringBuilder().apply {
                    if ((it.deviation ?: 0f) > 0) {
                        this.append("+")
                    }
                    this.append(
                        String.format(
                            locale = Locale.US, "%.2f", if (mViewModel.sessionManager.isMetric()) {
                                AppConversionUtils.fahrenheitToCelsius(32 + (it.deviation ?: 0f))
                            } else {
                                it.deviation
                            }
                        )
                    )
                }

                if (mViewModel.sessionManager.isMetric()) {
                    String.format(
                        locale = Locale.US,
                        "%.1f°C (%s)",
                        AppConversionUtils.fahrenheitToCelsius(it.data),
                        deviationString
                    )
                } else {
                    String.format(locale = Locale.US, "%.1f°F (%s)", it.data, deviationString)
                }

            }
            //"${it.deviation}°"
        }
//        binding.tvBaseline.text = "${if (it.trendData?.base == null) "-" else it.trendData.base}°"


        val topGraphData = mViewModel.getPrefixAndSuffixListTemp(
            it.result as ArrayList<ResultData>,
        )
        /*val topGraphData = mViewModel.getPrefixAndSuffixListTempDummy(
        )*/
        binding.rvTopBarGraph.updateDataWithMax(
            topGraphData.first.first,
            topGraphData.third,
            topGraphData.second,
            mViewModel.sessionManager.isMetric()
        )


//        var trendTodayTitle = ""
//        var trendYesterdayTitle = ""
//        var trendScoreMsg = ""
//
//        trendTodayTitle = getString(R.string.text_today)
//        trendYesterdayTitle = getString(R.string.text_yesterday)

//        binding.lytScoreOverview.tvTitle.text = "Body temperature trend"
        //  handleShowTrendCompareProgress(it)

//        trendScoreMsg = if (mViewModel.isProgressEqual)
//            "same as yesterday"
//        else {
//            if (mViewModel.isTodayGreater)
//                "more than yesterday"
//            else
//                "less than yesterday"
//        }


//        binding.lytScoreOverview.lytToday.tvToday.text = trendTodayTitle
//        binding.lytScoreOverview.lytYesterday.tvToday.text = trendYesterdayTitle
//        binding.lytScoreOverview.tvScoreMsg.text = trendScoreMsg

//        bindDataOnUi(it)
        binding.groupMain.visible()

    }

    private fun returnColor(): Pair<Int, Int> {

        val todayColor: Int = ContextCompat.getColor(
            requireContext(),
            R.color.readiness_progress_color
        )
        val yesterdayColor: Int = ContextCompat.getColor(
            requireContext(),
            R.color.readiness_progress_color_50
        )
        return Pair(todayColor, yesterdayColor)


    }


    override fun initListener() {
        binding.rvTopBarGraph.setOnChartScrollChangedListener(this)

    }

    override fun subscribeObservers() {

        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        mViewModel.internalDetailsData.observe(viewLifecycleOwner) {
            if (it != null) {
                initUi(it)
            }
        }
    }

    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {
        chartModel?.date?.let {
            if (it.isNotEmpty()) {
                binding.tvAvgOn.text = "Deviation on ${
                    DateFormats.parseDate(
                        it,
                        DateFormats.dateFormat3(),
                        DateFormats.dateFormat7()
                    )
                }"

                binding.tvDeviationValue.text = if (chartModel.valueFloat2 == 0.0f) {
                    "-"
                } else {
                    val deviationString = StringBuilder().apply {
                        if ((chartModel.valueFloat) > 0) {
                            this.append("+")
                        }
                        this.append(
                            String.format(
                                locale = Locale.US,
                                "%.2f",
                                chartModel.valueFloat
                            )
                        )
                    }

                    if(mViewModel.sessionManager.isMetric()){
                        String.format(
                            locale = Locale.US,
                            "%.1f°C (%s)",
                            chartModel.valueFloat2,
                            deviationString
                        )
                    }else{
                        String.format(
                            locale = Locale.US,
                            "%.1f°F (%s)",
                            chartModel.valueFloat2,
                            deviationString
                        )
                    }
                }
            }
        }
    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }

    private fun handleShowTrendCompareProgress(it: OInternalPageResponseModal) {
        val trendData = it.trendData
        var todayProgress: Float
        var yesterdayProgress: Float
        if (trendData != null) {
            if (trendData.today?.value == null || trendData.today.value.toInt() == 0 ||
                trendData.yesterday?.value == null || trendData.yesterday.value.toInt() == 0
            ) {
                binding.lytScoreOverview.tvTrendProg.gone()
                binding.lytScoreOverview.tvScoreMsg.gone()
            } else {
                var difference = 0f
                todayProgress =
                    trendData.today.value
                yesterdayProgress =
                    trendData.yesterday.value
                if (todayProgress > yesterdayProgress) {
                    binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_up)
                    binding.lytScoreOverview.tvTrendProg.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.steps_arc
                        )
                    )
                    binding.lytScoreOverview.tvTrendProg.visible()
                    binding.lytScoreOverview.tvScoreMsg.visible()
                    mViewModel.isProgressEqual = false
                    difference = trendData.today.value - trendData.yesterday.value
                    mViewModel.isTodayGreater = true
                } else if (yesterdayProgress > todayProgress) {
                    binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_down)
                    binding.lytScoreOverview.tvTrendProg.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_error
                        )
                    )
                    binding.lytScoreOverview.tvTrendProg.visible()
                    binding.lytScoreOverview.tvScoreMsg.visible()
                    mViewModel.isProgressEqual = false
                    difference = trendData.yesterday.value - trendData.today.value
                    mViewModel.isTodayGreater = false
                } else {
                    binding.lytScoreOverview.tvTrendProg.gone()
                    binding.lytScoreOverview.tvScoreMsg.visible()
                    mViewModel.isProgressEqual = true
                }
                val compPro = "${String.format(locale = Locale.US, "%.1f", difference)} °F"
                binding.lytScoreOverview.tvTrendProg.text = compPro
            }
        }
    }
}