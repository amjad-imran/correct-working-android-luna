package com.noisefit.ui.dashboard.graphs.hr

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.noisefit.luna.R
import com.noisefit_commans.data.model.history.HrBreakup
import com.noisefit.luna.databinding.FragmentHrGraphBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.util.graph.HeartChartUtils
import com.noisefit_commans.ui.*
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"
private const val ARG_PARAM5 = "param5"

@AndroidEntryPoint
class HrGraphFragment :
    BaseFragment<FragmentHrGraphBinding>(FragmentHrGraphBinding::inflate),
    OnChartValueSelectedListener {

    private var markerView: View? = null
    private val viewModel: HrGraphViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.hrDataList = it.getParcelableArrayList(ARG_PARAM1)!!
            viewModel.historyType = it.getString(ARG_PARAM2)!!
            viewModel.average = it.getInt(ARG_PARAM4, 0)
            viewModel.stats = it.getString(ARG_PARAM3)!!
            viewModel.date = it.getString(ARG_PARAM5)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val graphInterval = viewModel.getGraphInterval(viewModel.historyType)

        initUi(graphInterval)

        HeartChartUtils.setHrChart(
            binding.hrBarChart,
            graphInterval,
            viewModel.getXAxisMarker(graphInterval)
        )
        val dataList = viewModel.parseGraph(viewModel.hrDataList, graphInterval)

        binding.hrBarChart.post(Runnable() {

            if (dataList != null) {
                viewModel.markEntryList = dataList.second
                nullableBinding?.hrBarChart?.let {
                    HeartChartUtils.setHrChartData(
                        dataList.first,
                        it,
                        android.R.color.holo_red_dark,
                        viewModel.markEntryList,
                        it.measuredWidth.toFloat()
                    )
                }
            } else {
                HeartChartUtils.setInvalidate(binding.hrBarChart)
            }
        })

        binding.hrBarChart.setOnChartValueSelectedListener(this)

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

    }

    companion object {
        @JvmStatic
        fun newInstance(
            hrData: List<HrBreakup>,
            historyType: String,
            stats: String,
            average: Int,
            date: String
        ) =
            HrGraphFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, (hrData as ArrayList))
                    putString(ARG_PARAM2, historyType)
                    putString(ARG_PARAM3, stats)
                    putInt(ARG_PARAM4, average)
                    putString(ARG_PARAM5, date)
                }
            }
    }

    private fun initUi(graphInterval: GraphInterval) {


        when (graphInterval) {
            GraphInterval.DAY -> {
                binding.lytGraphDetailsHeader.tvDataEnd.gone()
                binding.lytGraphDetailsHeader.tvAverageSteps.gone()
                binding.lytGraphDetailsHeader.tvUnitEnd.gone()
            }
            GraphInterval.WEEK -> {
                binding.lytGraphDetailsHeader.tvDataEnd.visible()
                binding.lytGraphDetailsHeader.tvAverageSteps.visible()
                binding.lytGraphDetailsHeader.tvUnitEnd.visible()
            }
            GraphInterval.MONTH -> {
                binding.lytGraphDetailsHeader.tvDataEnd.visible()
                binding.lytGraphDetailsHeader.tvAverageSteps.visible()
                binding.lytGraphDetailsHeader.tvUnitEnd.visible()
            }
            GraphInterval.YEAR -> {
                binding.lytGraphDetailsHeader.tvDataEnd.visible()
                binding.lytGraphDetailsHeader.tvAverageSteps.visible()
                binding.lytGraphDetailsHeader.tvUnitEnd.visible()
            }
        }

        binding.lytGraphDetailsHeader.tvDataEnd.text = getString(R.string.text_average)

        binding.lytGraphDetailsHeader.apply {
            viewModel.average.toString().replaceUnderScore().apply {
                if (this.isUnderScore()) {
                    tvAverageSteps.text = this
                    tvTotalStepsCount.text = this
                    tvUnitEnd.text = ""
                    tvStatsUnit.text = ""
                } else {
                    tvStatsUnit.text = resources.getString(R.string.text_bpm)
                    tvUnitEnd.text = resources.getString(R.string.text_bpm)
                    tvAverageSteps.text = "${viewModel.average}"
                    tvTotalStepsCount.text = viewModel.stats
                }
            }

        }

        setDate(viewModel.date)

    }

    private fun setDate(date: String?) {
        if (date.isNullOrEmpty()) {
            binding.lytGraphDetailsHeader.tvDate.text = ""
            return
        }
        binding.lytGraphDetailsHeader.tvDate.text = date
    }

    override fun onValueSelected(e: Entry, h: Highlight) {

        handleMarkerUi(true)
        inflateView(h.xPx, e)

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

    override fun onNothingSelected() {
        handleMarkerUi(false)
    }

}