package com.noisefit.ui.dashboard.graphs.sleep

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.LineChart
import com.noisefit.R
import com.noisefit.databinding.FragmentSleepDayBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.enums.SleepExtraType
import com.noisefit_commans.response.SleepHeartRate
import com.noisefit_commans.response.SleepHourBreakup
import com.noisefit_commans.ui.custom.SleepGraphInteractionListener
import com.noisefit_commans.ui.custom.SleepGraphViewNew
import com.noisefit_commans.ui.custom.ToolTipEntry
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"
private const val ARG_PARAM5 = "param5"
private const val ARG_PARAM6 = "param6"
private const val ARG_PARAM7 = "ARG_PARAM7"
private const val ARG_PARAM8 = "ARG_PARAM8"
private const val ARG_PARAM9 = "ARG_PARAM9"

@AndroidEntryPoint
class SleepDayFragment : BaseFragment<FragmentSleepDayBinding>(FragmentSleepDayBinding::inflate) {

    private var markerView: View? = null

    private val viewModel: SleepGraphViewModel by viewModels()
    private val sharedViewModel: SleepSharedViewModel by activityViewModels()

    private var sleepDayGraphView: SleepGraphViewNew? = null

    private lateinit var dayHeartLineChartView: View
    private lateinit var dayStressScatteredChartView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            viewModel.sleepHourList = it.getParcelableArrayList(ARG_PARAM1)!!
            viewModel.sleepHeartRateList = it.getParcelableArrayList(ARG_PARAM8)
            viewModel.sleepStressList = it.getParcelableArrayList(ARG_PARAM9)
            viewModel.historyType = it.getString(ARG_PARAM2)!!
            viewModel.duration = it.getInt(ARG_PARAM3)
            viewModel.date = it.getString(ARG_PARAM4)!!
            viewModel.startTime = it.getString(ARG_PARAM5)!!
            viewModel.endTime = it.getString(ARG_PARAM6)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sleepDayGraphView = SleepGraphViewNew(requireContext())

        initUi()
        binding.flSleepGraph.addView(sleepDayGraphView)

        val sleepData = viewModel.getHourlySleepData()
        sleepDayGraphView?.init(false)

        sleepDayGraphView?.setData(sleepData.second)

        sleepDayGraphView?.setData(
            sleepData.first
        )
        sleepDayGraphView?.setInteraction(object : SleepGraphInteractionListener {
            override fun onSleepGraphSelected(toolTipEntry: ToolTipEntry?) {
                if (toolTipEntry == null) {
                    handleMarkerUi(false)
                } else {
                    handleMarkerUi(true)
                    val x = (toolTipEntry.x2 + toolTipEntry.x1) / 2
                    inflateView(x, toolTipEntry.type, toolTipEntry.value, toolTipEntry.range)
                }
            }

        })
        sleepDayGraphView?.invalidate()

        if (viewModel.overlayType != SleepExtraType.NONE) {
            setOverlayGraph(viewModel.overlayType)
        }

    }

    private fun removeMarkerView() {
        if (markerView != null) {
            binding.markerContainer.removeAllViews()
            binding.barContainer.removeAllViews()
            markerView = null
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initListener() {
        binding.container1.setOnTouchListener { _, _ ->
            removeMarkerView()
            handleMarkerUi(false)
            false
        }
    }
    override fun subscribeObservers() {

        sharedViewModel.isSelected.observe(this) {
            setOverlayGraph(it)
        }

    }

    fun setOverlayGraph(overlayType: SleepExtraType) {
        viewModel.overlayType = overlayType
        binding.flSleepGraphOverlay.removeAllViews()


        sleepDayGraphView?.toggleStatus(overlayType != SleepExtraType.NONE)

        if (overlayType == SleepExtraType.NONE) {
            return
        }

        if (overlayType == SleepExtraType.HeartRate) {
            if (!viewModel.sleepHeartRateList.isNullOrEmpty()) {
                dayHeartLineChartView =
                    layoutInflater.inflate(R.layout.layout_linechart_chart, null)
                val dayLineChart: LineChart = dayHeartLineChartView.findViewById(R.id.lineChart)

                SleepGraphUtil.setChartData(
                    viewModel.getTodayHeartRateData(),
                    dayLineChart,
                    requireContext()
                )
                binding.flSleepGraphOverlay.addView(dayHeartLineChartView)
            }

        } else if (overlayType == SleepExtraType.StressLevel) {
            if (!viewModel.sleepStressList.isNullOrEmpty()) {
                dayStressScatteredChartView =
                    layoutInflater.inflate(R.layout.layout_candle_chart_sleep_day, null)
                val dayStressChartData: CandleStickChart =
                    dayStressScatteredChartView.findViewById(R.id.candleChart)
                SleepGraphUtil.setDayStressChart(dayStressChartData, requireContext())
                SleepGraphUtil.setCandleStickChartData(
                    viewModel.getTodayStressData(),
                    dayStressChartData,
                    requireContext(),
                    R.color.stress_normal
                )
                binding.flSleepGraphOverlay.addView(dayStressScatteredChartView)
            }


        }


    }

    companion object {
        @JvmStatic
        fun newInstance(
            sleepData: List<SleepHourBreakup>,
            sleepHeartRate: List<SleepHeartRate>,
            stressRate: List<SleepHeartRate>,
            historyType: String,
            duration: Int,
            date: String,
            startTime: String,
            endTime: String
        ) =
            SleepDayFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, (sleepData as ArrayList))
                    putParcelableArrayList(ARG_PARAM8, (sleepHeartRate as ArrayList))
                    putParcelableArrayList(ARG_PARAM9, (stressRate as ArrayList))
                    putString(ARG_PARAM2, historyType)
                    putInt(ARG_PARAM3, duration)
                    putString(ARG_PARAM4, date)
                    putString(ARG_PARAM5, startTime)
                    putString(ARG_PARAM6, endTime)
                }
            }
    }

    private fun initUi() {

        binding.lytGraphDetailsHeader.apply {
            tvData2.gone()
            tvSleepHour2.gone()
            textHour2.gone()
        }

        val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(viewModel.duration)
        if(hour == 0 && minute == 0){
            binding.lytGraphDetailsHeader.tvSleepHour.text = "_"
            binding.lytGraphDetailsHeader.tvSleepMinute.text = "_"
        }else{
            binding.lytGraphDetailsHeader.tvSleepHour.text = "$hour"
            binding.lytGraphDetailsHeader.tvSleepMinute.text = "$minute"
        }
        setDate(viewModel.date)

    }

    private fun setDate(date: String?) {
        if (date.isNullOrEmpty()) {
            binding.lytGraphDetailsHeader.tvDate.text = ""
            return
        }
        binding.lytGraphDetailsHeader.tvDate.text = DateFormats.formatDateTime(
            date,
            DateFormats.dateFormat3,
            DateFormats.dateTimeFormatWithWeekDay
        )
    }

    private fun inflateView(marginStart: Float, type: String, min: Int, range: String) {

//        if (viewModel.markEntryList.isNullOrEmpty()) {
//            return
//        }
       removeMarkerView()

        markerView = LayoutInflater.from(context)
            .inflate(com.noisefit_commans.R.layout.custom_marker_view, binding.container1, false)

        val tvContent: TextView = markerView!!.findViewById(com.noisefit_commans.R.id.tvMarketData)
        val tvDate: TextView = markerView!!.findViewById(com.noisefit_commans.R.id.tvMarketDate)
        val tvType: TextView = markerView!!.findViewById(com.noisefit_commans.R.id.tvMarketType)
        val tvRange: TextView = markerView!!.findViewById(com.noisefit_commans.R.id.tvMarketRange)
        tvRange.visible()
        tvDate.text = type.uppercase()
        tvContent.text = "$min mins"
        tvRange.text = range

        markerView!!.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        markerView!!.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val markViewWidth = markerView!!.measuredWidth
        val params =
            RelativeLayout.LayoutParams(binding.markerContainer.width, markerView!!.measuredHeight)
        var totalMargin = marginStart - (markViewWidth / 3)
        val viewPlusTotalMargin = totalMargin + markViewWidth
        val barChartWidth = binding.flSleepGraph.width
        if (totalMargin < 0) {
            totalMargin = 0f
        } else if (viewPlusTotalMargin > barChartWidth) {
            totalMargin = barChartWidth - markViewWidth.toFloat()
        }
        params.marginStart = totalMargin.toInt()
        binding.markerContainer.addView(markerView)
        binding.markerContainer.layoutParams = params

//        LOGS.d("asdassaddsa ${totalMargin} ${markerView!!.measuredWidth} ${binding.barChart.width}")

        val view = View(requireContext())
        val lpView =
            LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT) // --> horizontal

        lpView.marginStart = marginStart.toInt()
        view.layoutParams = lpView
        view.setBackgroundColor(requireContext().resources.getColor(R.color.marker_border))

        binding.barContainer.addView(view)
        // binding.barContainer.layoutParams = view.layoutParams

    }

    private fun handleMarkerUi(showMarker: Boolean) {
        if (showMarker) {
            binding.markerContainer.visible()
            binding.lytGraphDetailsHeader.root.gone()

        } else {
            binding.lytGraphDetailsHeader.root.visible()
            binding.markerContainer.gone()
            binding.markerContainer.removeAllViews()
            binding.barContainer.removeAllViews()

        }
    }


}