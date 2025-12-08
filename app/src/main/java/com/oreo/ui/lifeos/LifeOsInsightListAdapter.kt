package com.oreo.ui.lifeos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightCardBinding
import com.noisefit_commans.ui.custom.NightTimeGraphViewOreo
import com.noisefit_commans.ui.custom.SleepGraphViewOreo
import com.oreo.data.dataConverter.GraphsKey
import com.oreo.ui.custom.HRCombinedChart
import com.oreo.ui.custom.ODayTimeInteractiveGraph
import com.oreo.ui.custom.StressCombinedChart
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientChartType
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientLineChartInternal
import com.oreo.ui.custom.sleep.internal.SleepSingleLineChartInternal
import com.oreo.ui.custom.sleep.internal.SleepDailyGradientChartInternal
import com.oreo.ui.custom.sleep.internal.SleepHourVsNeedChartInternal
import com.oreo.ui.custom.sleep.internal.SleepRestorativeChartInternal
import com.oreo.ui.custom.sleep.internal.SleepSingleBarChart
import com.oreo.ui.custom.sleep.internal.SleepTimingChartInternal
import com.oreo.ui.lifeos.charts.BarChartSingleInsight1
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import com.oreo.ui.lifeos.charts.PayloadData
import com.oreo.ui.lifeos.charts.TimeSeriesPayload

class LifeOsInsightListAdapter(
    private val isFromLifeOsDash: Boolean = false,
    private val onClick: (InsightCardUiModel) -> Unit,
) : ListAdapter<InsightCardUiModel, LifeOsInsightListAdapter.ViewHolder>(Diff) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    inner class ViewHolder(val binding: FragmentLifeOsInsightCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentChartKey: GraphsKey? = null
        private var currentChartView: View? = null

        fun bind(item: InsightCardUiModel) {
            binding.tvTitle.text = item.raw?.title ?: ""
            binding.tvTime.text = item.timeText ?: ""

            val insightType = item.raw?.insightType?.lowercase()

            binding.ivInsight.setImageResource(
                when {
                    insightType == null -> R.drawable.ic_misc_insight_item
                    insightType.contains("sleep") -> R.drawable.ic_sleep_insight_item
                    insightType.contains("activity") -> R.drawable.ic_activity_insight_item
                    insightType.contains("readiness") -> R.drawable.ic_readiness_insight_item
                    insightType.contains("stress") -> R.drawable.ic_stress_insight_item
                    else -> R.drawable.ic_misc_insight_item
                }
            )

            binding.root.setOnClickListener { onClick(item) }

            val view = provideChartView(item.chartKey, item.payload, item.styleRes)
            binding.chartContainer.removeAllViews()
            if (view != null) {
                binding.chartContainer.addView(view)
                currentChartView = view
                currentChartKey = item.chartKey
            } else {
                currentChartView = null
                currentChartKey = null
            }
        }

        fun onAttach() {
            // no-op; hook for future
        }

        fun onDetach() {
            // no-op; hook for future
        }

        private fun detachCurrent() {
            currentChartKey = null
            currentChartView = null
            binding.chartContainer.removeAllViews()
        }

        /**
         * Simple factory/hook: provide chart view for a given type and payload.
         * Extend this with more cases as new chart types arrive.
         */
        private fun provideChartView(graphsKey: GraphsKey?, payload: PayloadData?, styleRes: Int?): View? {
            val ctx = binding.chartContainer.context
            return when (graphsKey) {
                GraphsKey.HEART_RATE-> {
                    val data = payload?.hrData
                    val v = (currentChartView as? HRCombinedChart) ?: HRCombinedChart(ctx)
                    v.applyStyle(styleRes ?: R.style.HrChartStyle)
                    data?.let { v.updateData(it.model, it.yAxisCount, it.minYAxis, it.maxYAxis) }
                    v
                }
                GraphsKey.STRESS -> {
                    val data = payload?.stressData
                    val v = (currentChartView as? StressCombinedChart) ?: StressCombinedChart(ctx)
                    v.applyStyle(styleRes ?: R.style.StressChartStyle)
                    data?.let { v.updateData(it.model) }
                    v
                }
                GraphsKey.DAY_TIME_MOVEMENT -> {
                    val data = payload?.dayTimeData
                    val v = (currentChartView as? ODayTimeInteractiveGraph) ?: ODayTimeInteractiveGraph(ctx)
                    v.enableInteractiveMode(true)
                    v.applyStyle(styleRes ?: R.style.DayTimeGraphStyle)
                    data?.let { v.updateData(it.model) }
                    v
                }
                GraphsKey.SLEEP_BREAKUP -> {
                    val data = payload?.sleepBreakup as? ArrayList
                    val v = (currentChartView as? SleepGraphViewOreo) ?: SleepGraphViewOreo(ctx)
                    v.enableInteractiveMode(false)
                    v.init(false)
                    //data?.totals?.let { v.setData(it) }
                    data?.let { v.setData(it) }
                    v.invalidate()
                    v
                }
                GraphsKey.SLEEP_MOVEMENT -> {
                    val data = payload?.sleepMovement
                    val v = (currentChartView as? NightTimeGraphViewOreo) ?: NightTimeGraphViewOreo(ctx)


                    v.init(false)

                    v.setData(data?.second)
                    v.setData(
                        data?.first
                    )
                    v.invalidate()
                    v

                }
                GraphsKey.TREND_SLEEP_SINGLE -> {
                    val data = payload?.trendData

                    if(data?.xAxisRangeInsights.isNullOrEmpty()) return null

                    val v = (currentChartView as? BarChartSingleInsight1) ?: BarChartSingleInsight1(ctx,null)
                    v.setDataSet(
                        data.list?:arrayListOf(),
                        data.yAxisRange?:arrayListOf(),
                        data.avgValue,
                        -1,
                        data.contributorType,
                        data.optimalRange,
                        data.nonNullDataCount?:0,
                        data.xAxisRangeInsights
                    )
                    v

                }
                GraphsKey.TREND_SLEEP_SINGLE_LINE_GRADIENT -> {
                    val data = payload?.trendData

                    val v = (currentChartView as? SleepSingleGradientLineChartInternal) ?: SleepSingleGradientLineChartInternal(ctx,null)
                    v.setDataSet(
                        data?.list?:arrayListOf(),
                        data?.yAxisRange?:arrayListOf(),
                        data?.xAxisRange?:arrayListOf(),
                        data?.avgValue,
                        -1,
                        data?.chartType?:SleepSingleGradientChartType.DEFAULT,
                        data?.optimalRange,
                        data?.nonNullDataCount?:0
                    )
                    v

                }
                GraphsKey.TREND_SLEEP_MULTI_BAR -> {
                    val data = payload?.trendData

                    val v = (currentChartView as? SleepRestorativeChartInternal) ?: SleepRestorativeChartInternal(ctx,null)
                    v.setDataSet(
                        list = data?.list!!,
                        yAxisRange = data.yAxisRange!!,
                        maxValue = data.maxValue!!,
                        selectedPosition = -1
                    )
                    v

                }


                GraphsKey.TREND_SLEEP_TIMING_INTERNAL -> {
                    val data = payload?.trendData

                    val v = (currentChartView as? SleepTimingChartInternal) ?: SleepTimingChartInternal(ctx,null)
                    v.setDataSet(
                        data?.list ?: ArrayList(),
                        data?.xAxisRange!!,
                        data.yAxisRange!!,
                        data.maxDeviation!!,
                        data.optimalRange
                    )
                    v

                }

                GraphsKey.TREND_SLEEP_HOUR_VS_NEED_CHARD_INTERNAL -> {
                    val data = payload?.trendData

                    val v = (currentChartView as? SleepHourVsNeedChartInternal) ?: SleepHourVsNeedChartInternal(ctx,null)
                    v.setDataSet(
                        data?.list ?: ArrayList(),
                        data?.yAxisRange!!,
                        data.maxValue!!,
                        data.selectedPosition!!
                    )
                    v

                }

                /*"respiratory_daily" -> dailyGradient(payload)
                "respiratory_day" -> dayBar(payload)
                "respiratory_week" -> weekMonthLine(payload)
                "respiratory_month" -> weekMonthLine(payload)
                // Resting HR
                "resting_hr_daily" -> dailyGradient(payload)
                "resting_hr_day" -> dayGradient(payload, SleepSingleGradientChartType.DEFAULT)
                "resting_hr_week" -> weekMonthLine(payload)
                "resting_hr_month" -> weekMonthLine(payload)
                // HRV
                "hrv_daily" -> dailyGradient(payload)
                "hrv_day" -> dayGradient(payload, SleepSingleGradientChartType.DEFAULT)
                "hrv_week" -> weekMonthLine(payload)
                "hrv_month" -> weekMonthLine(payload)
                // Skin temperature (float)
                "skin_temp_daily" -> dailyGradient(payload)
                "skin_temp_day" -> dayGradient(payload, SleepSingleGradientChartType.FLOAT)
                "skin_temp_week" -> weekMonthLine(payload)
                "skin_temp_month" -> weekMonthLine(payload)
                // Blood oxygen (percent)
                "blood_oxygen_daily" -> dailyGradient(payload)
                "blood_oxygen_day" -> dayBar(payload)
                "blood_oxygen_week" -> weekMonthLine(payload)
                "blood_oxygen_month" -> weekMonthLine(payload)*/
                else -> null
            }
        }

        private fun dailyGradient(payload: Any?): View? {
            val ctx = binding.chartContainer.context
            val data = payload as? TimeSeriesPayload ?: return null
            val v = (currentChartView as? SleepDailyGradientChartInternal)
                ?: SleepDailyGradientChartInternal(ctx, null)
            val yAxis = buildYAxis(data.values)
            val avgValue = data.values.filterNotNull().average().toFloat() to data.unitLabel
            val list = data.dates.mapIndexed { idx, d -> GraphDataModel(value1 = data.values[idx], date = d) }
            v.setDataSet(list, yAxis, avgValue, -1, null, null, null)
            return v
        }

        private fun dayGradient(payload: Any?, chartType: SleepSingleGradientChartType): View? {
            val ctx = binding.chartContainer.context
            val data = payload as? TimeSeriesPayload ?: return null
            val v = (currentChartView as? SleepSingleGradientLineChartInternal)
                ?: SleepSingleGradientLineChartInternal(ctx, null)
            val yAxis = buildYAxis(data.values)
            val avgValue = data.values.filterNotNull().average().toFloat() to data.unitLabel
            val nonNull = data.values.count { it != null }
            val list = data.dates.mapIndexed { idx, d -> GraphDataModel(value1 = data.values[idx], date = d) }
            v.setDataSet(list, yAxis, data.dates, avgValue, -1, chartType, null, nonNull)
            return v
        }

        private fun dayBar(payload: Any?): View? {
            val ctx = binding.chartContainer.context
            val data = payload as? TimeSeriesPayload ?: return null
            val v = (currentChartView as? SleepSingleBarChart) ?: SleepSingleBarChart(ctx, null)
            val yAxis = buildYAxis(data.values)
            val avgValue = data.values.filterNotNull().average().toFloat() to data.unitLabel
            val nonNull = data.values.count { it != null }
            val list = data.dates.mapIndexed { idx, d -> GraphDataModel(value1 = data.values[idx], date = d) }
            v.setDataSet(list, yAxis, avgValue, -1, null, null, nonNull)
            return v
        }

        private fun weekMonthLine(payload: Any?): View? {
            val ctx = binding.chartContainer.context
            val data = payload as? TimeSeriesPayload ?: return null
            val v = (currentChartView as? SleepSingleLineChartInternal)
                ?: SleepSingleLineChartInternal(ctx, null)
            val yAxis = buildYAxis(data.values)
            val avgValue = data.values.filterNotNull().average().toFloat() to data.unitLabel
            val nonNull = data.values.count { it != null }
            val list = data.dates.mapIndexed { idx, d -> GraphDataModel(value1 = data.values[idx], date = d) }
            val showOverlay = true
            v.setDataSet(list, yAxis, data.dates, avgValue, -1, showOverlay, null, null, nonNull)
            return v
        }

        private fun buildYAxis(values: List<Float?>): List<Pair<Int, String>> {
            val nonNull = values.filterNotNull()
            if (nonNull.isEmpty()) return listOf(0 to "0", 25 to "25", 50 to "50", 75 to "75", 100 to "100")
            val min = nonNull.minOrNull()!!.toInt()
            val max = nonNull.maxOrNull()!!.toInt()
            val steps = 4
            val range = (max - min).coerceAtLeast(4)
            val step = (range / steps).coerceAtLeast(1)
            val out = ArrayList<Pair<Int, String>>()
            var v = min
            repeat(steps) {
                out.add(v to v.toString())
                v += step
            }
            out.add((min + range) to (min + range).toString())
            return out
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentLifeOsInsightCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if(isFromLifeOsDash){
            val view = binding.root
            view.post {
                val rvWidth = parent.width
                if (rvWidth > 0) {
                    val density = parent.resources.displayMetrics.density
                    val peekPx = (40f * density).toInt()
                    val spacePx = (16f * density).toInt()

                    val itemWidth = rvWidth - peekPx - spacePx

                    view.layoutParams = view.layoutParams.apply {
                        width = itemWidth
                    }
                }
            }
        }
        return ViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }

    object Diff : DiffUtil.ItemCallback<InsightCardUiModel>() {
        override fun areItemsTheSame(oldItem: InsightCardUiModel, newItem: InsightCardUiModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: InsightCardUiModel, newItem: InsightCardUiModel): Boolean =
            oldItem == newItem
    }
}
