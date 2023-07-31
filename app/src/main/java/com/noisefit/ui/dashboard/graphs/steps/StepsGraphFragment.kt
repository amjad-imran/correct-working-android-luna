package com.noisefit.ui.dashboard.graphs.steps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.noisefit.luna.R
import com.noisefit_commans.data.model.history.StepsHistoryData
import com.noisefit.luna.databinding.FragmentStepsGraphBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.graph.StepsChartUtils
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"
private const val ARG_PARAM5 = "param5"
private const val ARG_PARAM6 = "param6"

@AndroidEntryPoint
class StepsGraphFragment :
    BaseFragment<FragmentStepsGraphBinding>(FragmentStepsGraphBinding::inflate),
    OnChartValueSelectedListener {

    private val viewModel: StepsGraphViewModel by viewModels()

    private var markerView: View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.stepsDataList = it.getParcelableArrayList(ARG_PARAM1)!!
            viewModel.historyType = it.getString(ARG_PARAM2)!!
            viewModel.steps = it.getLong(ARG_PARAM5, 0)
            viewModel.date = it.getString(ARG_PARAM4)!!
            viewModel.average = it.getDouble(ARG_PARAM3, 0.0)
            viewModel.healthOverViewHistoryType = getType(it.getString(ARG_PARAM6)!!)
        }
    }

    private fun getType(linkType: String): HealthOverViewHistoryType {
        when (linkType.lowercase()) {
            HealthOverViewHistoryType.Calories.name.lowercase() -> {
                return HealthOverViewHistoryType.Calories
            }
            HealthOverViewHistoryType.Distance.name.lowercase() -> {
                return HealthOverViewHistoryType.Distance
            }
        }
        return HealthOverViewHistoryType.Steps
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO move calculation to bg thread

        val graphInterval = viewModel.getGraphInterval(viewModel.historyType)
        initUi(graphInterval)

        val color = when (viewModel.healthOverViewHistoryType) {
            HealthOverViewHistoryType.Calories -> {
                resources.getColor(R.color.calories_color)
            }
            HealthOverViewHistoryType.Distance -> {
                resources.getColor(R.color.distance_color)
            }
            else -> {
                resources.getColor(R.color.steps_color)
            }
        }
        StepsChartUtils.setChart(
            color,
            binding.barChart,
            graphInterval,
            viewModel.average.roundToInt(),
            viewModel.getXAxisMarker(graphInterval)
        )
        val dataList =
            viewModel.parseGraph(viewModel.stepsDataList, graphInterval)

        nullableBinding?.barChart?.post(Runnable() {
            nullableBinding?.let {
                if (dataList != null) {
                    viewModel.markEntryList = dataList.third

                    StepsChartUtils.setChartData(
                        dataList.first,
                        it.barChart,
                        dataList.second,
                        dataList.third,
                        it.barChart.measuredWidth.toFloat()
                    )
                } else {
                    StepsChartUtils.setInvalidate(it.barChart)
                }
            }
        })
        binding.barChart.setOnChartValueSelectedListener(this)
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

        if (graphInterval == GraphInterval.DAY) {
            setStepsCount(viewModel.date)
        } else {
            setDate(viewModel.date)
        }


        when (viewModel.healthOverViewHistoryType) {
            HealthOverViewHistoryType.Calories -> {
                binding.lytGraphDetailsHeader.tvStatsUnit.text = getString(R.string.text_kcal)
                binding.lytGraphDetailsHeader.tvUnitEnd.text = getString(R.string.text_kcal)
                binding.lytGraphDetailsHeader.tvTotalStepsCount.text = "${viewModel.steps}"
                binding.lytGraphDetailsHeader.tvAverageSteps.text =  "${viewModel.average.roundToInt()}"
            }
            HealthOverViewHistoryType.Distance -> {
                binding.lytGraphDetailsHeader.tvStatsUnit.text = viewModel.distanceUnit
                binding.lytGraphDetailsHeader.tvUnitEnd.text = viewModel.distanceUnit
                binding.lytGraphDetailsHeader.tvTotalStepsCount.text =
                    viewModel.getDataUnitConverter().formatDistance(viewModel.steps, viewModel.unit)
                binding.lytGraphDetailsHeader.tvAverageSteps.text =
                    viewModel.getDataUnitConverter()
                        .formatDistance(viewModel.average.roundToInt(), viewModel.unit)
            }
            else -> {
                binding.lytGraphDetailsHeader.tvAverageSteps.text =
                    "${viewModel.average.roundToInt()}"
                binding.lytGraphDetailsHeader.tvTotalStepsCount.text = "${viewModel.steps}"
            }
        }


    }

    private fun setStepsCount(date: String) {
        binding.lytGraphDetailsHeader.tvDate.text = DateFormats.formatDateTime(
            date,
            DateFormats.dateFormat3,
            DateFormats.dateTimeFormatWithWeekDay
        )

    }

    private fun setDate(date: String?) {
        if (date.isNullOrEmpty()) {
            binding.lytGraphDetailsHeader.tvDate.text = ""
            return
        }
        binding.lytGraphDetailsHeader.tvDate.text = date
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
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        val markViewWidth = markerView!!.measuredWidth
        val params =
            RelativeLayout.LayoutParams(binding.markerContainer.width, markerView!!.measuredHeight)
        var totalMargin = marginStart - (markViewWidth / 3)
        val viewPlusTotalMargin = totalMargin + markViewWidth
        val barChartWidth = binding.barChart.width
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

    override fun onValueSelected(e: Entry, h: Highlight) {

        handleMarkerUi(true)
        inflateView(h.xPx, e)

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

    companion object {
        @JvmStatic
        fun newInstance(
            stepsDataList: ArrayList<StepsHistoryData>,
            historyType: String,
            average: Double,
            date: String,
            steps: Long,
            healthOverViewHistory: String
        ) =
            StepsGraphFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, stepsDataList)
                    putString(ARG_PARAM2, historyType)
                    putString(ARG_PARAM4, date)
                    putLong(ARG_PARAM5, steps)
                    putDouble(ARG_PARAM3, average)
                    putString(ARG_PARAM6, healthOverViewHistory)
                }
            }
    }

}