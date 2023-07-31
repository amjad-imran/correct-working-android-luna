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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSleepGraphBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.RoundedBarChart
import com.noisefit.util.graph.SleepChartUtils
import com.noisefit_commans.data.enums.SleepExtraType
import com.noisefit_commans.response.SleepBreakup
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"
private const val ARG_PARAM5 = "param5"

@AndroidEntryPoint
class SleepGraphFragment :
    BaseFragment<FragmentSleepGraphBinding>(FragmentSleepGraphBinding::inflate),
    OnChartValueSelectedListener {

    private val viewModel: SleepGraphViewModel by viewModels()
    private var markerView: View? = null
    private val sharedViewModel: SleepSharedViewModel by activityViewModels()


    private var overlayChartView: View? = null
    private var overlayCandleChart: CandleStickChart? = null

    companion object {
        @JvmStatic
        fun newInstance(
            hrData: List<SleepBreakup>,
            historyType: String,
            duration: Int,
            averageBedTime: String,
            date: String
        ) =
            SleepGraphFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, (hrData as ArrayList))
                    putString(ARG_PARAM2, historyType)
                    putInt(ARG_PARAM3, duration)
                    putString(ARG_PARAM4, averageBedTime)
                    putString(ARG_PARAM5, date)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            viewModel.sleepDataList = it.getParcelableArrayList(ARG_PARAM1)!!
            viewModel.historyType = it.getString(ARG_PARAM2) ?: ""
            viewModel.averageBedTime = it.getString(ARG_PARAM4) ?: ""
            viewModel.duration = it.getInt(ARG_PARAM3, 0)
            viewModel.date = it.getString(ARG_PARAM5)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initUi()

        setBaseChart(false)


    }

    private fun setBaseChart(overlayMode: Boolean) {
        val graphInterval = viewModel.getGraphInterval(viewModel.historyType)
        SleepChartUtils.setSleepChart(
            binding.hrBarChart,
            graphInterval,
            viewModel.getXAxisMarker(graphInterval),
            overlayMode
        )
        val dataList = viewModel.parseGraph(viewModel.sleepDataList, graphInterval)
        if (dataList != null) {
            viewModel.markEntryList = dataList.second
            SleepChartUtils.setSleepChartData(
                dataList.first,
                binding.hrBarChart,
                overlayMode,
                graphInterval
            )
        } else {
            SleepChartUtils.setInvalidate(binding.hrBarChart)
        }
        binding.hrBarChart.setOnChartValueSelectedListener(this)
    }

    private fun initUi() {

        val (bedTime, unit) = try {
            val array = viewModel.averageBedTime.split(" ")
            Pair(array[0], array[1])
        } catch (exp: Exception) {
            Pair("_", "")
        }

        binding.lytGraphDetailsHeader.tvSleepHour2.text = bedTime
        binding.lytGraphDetailsHeader.textHour2.text = unit

        setDate(viewModel.date)

        val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(viewModel.duration)
        if(hour == 0 && minute == 0){
            binding.lytGraphDetailsHeader.tvSleepHour.text = "_"
            binding.lytGraphDetailsHeader.tvSleepMinute.text = "_"
        }else{
            binding.lytGraphDetailsHeader.tvSleepHour.text = "$hour"
            binding.lytGraphDetailsHeader.tvSleepMinute.text = "$minute"
        }


        overlayChartView =
            layoutInflater.inflate(R.layout.layout_candle_chart, null)
        overlayCandleChart =
            overlayChartView?.findViewById(R.id.candleChart)
    }

    private fun setInvalidate(chart: RoundedBarChart) {
        chart.clear()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun setDate(date: String?) {
        if (date.isNullOrEmpty()) {
            binding.lytGraphDetailsHeader.tvDate.text = ""
            return
        }
        binding.lytGraphDetailsHeader.tvDate.text = date
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


    private fun setOverlayGraph(overlayType: SleepExtraType) {
        viewModel.overlayType = overlayType
        binding.flSleepGraphOverlay.removeAllViews()

        if (overlayCandleChart == null) return

        overlayCandleChart?.apply {
            clear()
            notifyDataSetChanged()
            invalidate()
        }


        if (overlayType == SleepExtraType.NONE) {
            setBaseChart(false)
            return
        }

        setBaseChart(true)


        //TOOD change on the bases of data
        if (overlayType == SleepExtraType.HeartRate) {
            val viewToAdd = when (viewModel.historyType) {
                "weekly" -> {
                    SleepGraphUtil.setCandleStickChartData(
                        viewModel.getOverlayHeartData(),
                        overlayCandleChart!!,
                        requireContext(),
                        R.color.heart_rate
                    )
                    overlayChartView
                }
                "monthly" -> {
                    SleepGraphUtil.setCandleStickChartData(
                        viewModel.getOverlayHeartData(),
                        overlayCandleChart!!,
                        requireContext(),
                        R.color.heart_rate
                    )
                    overlayChartView
                }
                "yearly" -> {
                    SleepGraphUtil.setCandleStickChartData(
                        viewModel.getOverlayHeartData(),
                        overlayCandleChart!!,
                        requireContext(),
                        R.color.heart_rate
                    )
                    overlayChartView
                }
                else -> null
            }

            viewToAdd?.let {
                binding.flSleepGraphOverlay.addView(it)
            }

        } else if (overlayType == SleepExtraType.StressLevel) {
            val viewToAdd = when (viewModel.historyType) {
                "weekly" -> {
                    SleepGraphUtil.setCandleStickChartData(
                        viewModel.getOverlayStressData(),
                        overlayCandleChart!!,
                        requireContext(),
                        R.color.stress_normal
                    )
                    overlayChartView
                }
                "monthly" -> {
                    SleepGraphUtil.setCandleStickChartData(
                        viewModel.getOverlayStressData(),
                        overlayCandleChart!!,
                        requireContext(),
                        R.color.stress_normal
                    )
                    overlayChartView
                }
                "yearly" -> {
                    SleepGraphUtil.setCandleStickChartData(
                        viewModel.getOverlayStressData(),
                        overlayCandleChart!!,
                        requireContext(),
                        R.color.stress_normal
                    )
                    overlayChartView
                }
                else -> null
            }

            viewToAdd?.let {
                binding.flSleepGraphOverlay.addView(it)
            }
        }


    }


    override fun onValueSelected(e: Entry, h: Highlight) {
        handleMarkerUi(true)
        inflateView(h.xPx, e)

    }

    override fun onNothingSelected() {
        handleMarkerUi(false)

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

    private fun inflateView(marginStart: Float, e: Entry) {

        if (viewModel.markEntryList.isNullOrEmpty()) {
            return
        }
       removeMarkerView()

        markerView = LayoutInflater.from(context)
            .inflate(com.noisefit_commans.R.layout.custom_marker_view, binding.container1, false)

        val tvContent: TextView = markerView!!.findViewById(com.noisefit_commans.R.id.tvMarketData)
        val tvDate: TextView = markerView!!.findViewById(com.noisefit_commans.R.id.tvMarketDate)
        val tvType: TextView = markerView!!.findViewById(com.noisefit_commans.R.id.tvMarketType)

        tvContent.text = viewModel.markEntryList[e.x.toInt()].value
        tvDate.text = viewModel.markEntryList[e.x.toInt()].date
        tvType.text = viewModel.markEntryList[e.x.toInt()].type

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
        val barChartWidth = binding.hrBarChart.width
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


}