package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.data.model.AiHeaderInsight1
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.custom.NightTimeGraphViewOreo
import com.noisefit_commans.ui.custom.SleepGraphViewOreo
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.oreo.data.dataConverter.GraphsKey
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.custom.HRCombinedChart
import com.oreo.ui.custom.ODayTimeInteractiveGraph
import com.oreo.ui.custom.StressCombinedChart
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import com.oreo.ui.custom.sleep.internal.SleepHourVsNeedChartInternal
import com.oreo.ui.custom.sleep.internal.SleepRestorativeChartInternal
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientChartType
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientLineChartInternal
import com.oreo.ui.custom.sleep.internal.SleepSingleLineChartInternal
import com.oreo.ui.custom.sleep.internal.SleepTimingChartInternal
import com.oreo.ui.lifeos.charts.BarChartSingleInsight1
import com.oreo.ui.lifeos.charts.PayloadData
import com.oreo.ui.lifeos.charts.TimeSeriesPayload
import com.oreo.ui.sleep2.internal.InternalSelectedPeriod
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.ArrayList

@AndroidEntryPoint
class LifeOsInsightDetailsFragment :
    BaseFragment<FragmentLifeOsInsightDetailsBinding>(FragmentLifeOsInsightDetailsBinding::inflate) {

    private val viewModel: LifeOsInsightDetailsViewModel by viewModels()

    private val args: LifeOsInsightDetailsFragmentArgs by navArgs()

    private var currentChartView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*val jsonRes = """
            {
    "relevancy": 0.95,
    "title": "HRV dropped ~37% last night",
    "description": "Your average sleep HRV fell from ~56 ms (prior week average) to 35 ms on 2025-11-17 — a ~37% drop. That large decline suggests reduced physiological recovery overnight (could reflect higher daytime stress, recent training load, illness, or alcohol/caffeine late in the day). Your readiness score also fell to 54 the same day, which supports the idea your body felt less recovered despite an OK sleep duration.",
    "suggestions": "Prioritize an easy/recovery day today — avoid high-intensity training until HRV recovers.",
    "related_suggested_questions": [
      "Did you feel more stressed or have unusual symptoms on 2025-11-16/17?",
      "Was there alcohol, extra caffeine, or a late heavy meal the night before the HRV drop?",
      "How does this HRV drop compare to other low-readiness days in the past month?"
    ],
    "graph_type": "hr"
  }
        """.trimIndent()
        val rawData = Gson().fromJson(jsonRes, InsightItemResponseModel::class.java)
        viewModel.insightData = InsightCardUiModel(
            raw = rawData,
            id = 1,
            title = null,
            timeText = null,
            chartKey = null,
            payload = null,
            styleRes = null
        )*/

        viewModel.insightData = args.insightData
        setUi()
        setRecycler()
        setGraph()
    }

    private fun setGraph() {
        val data = viewModel.insightData
        if(data==null) return

        if(data.raw?.fallbackImage.isNullOrEmpty()) {
            binding.fallbackImg.gone()
            if (data.raw?.graph_type.isNullOrEmpty() || data.raw.graph == null) {
                binding.chartContainer.gone()
            } else {
                val view = provideChartView(data.chartKey, data.payload, data.styleRes)
                if (view != null) {
                    binding.chartContainer.visible()
                    binding.chartContainer.removeAllViews()
                    binding.chartContainer.addView(view)
                } else {
                    binding.chartContainer.gone()
                }
            }
        }
        else{
            binding.fallbackImg.apply {
                visible()
                loadImage(this.context, data.raw.fallbackImage)
            }
        }
    }

    private fun setRecycler() {
        viewModel.insightData?.raw?.related_suggested_questions?.let { list ->
            binding.rvRelatedSuggestedQues.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = RelatedSuggestedQuesAdapter(
                    list
                ){
                    handleRelatedQuesClick(it)
                }
            }
        }
    }

    private fun handleRelatedQuesClick(data: String) {
        val (frag, bundle) = LifeOsChatFragment.getStartData(
            threadId = null,
            userMessage = null,
            title = null,
            headerInsight1 = AiHeaderInsight1(
                headerText = getString(R.string.text_follow_up_to),
                mainText = viewModel.insightData?.raw?.title ?: "",
                footerText = data,
                insightData = viewModel.insightData?.raw
            ),
            aiTopic = AITopics.GENERAL
        )
        navigate(
            frag, bundle
        )
    }

    private fun setUi() {
        val data = viewModel.insightData
        if(data==null) return
        binding.tvTitle.text = data.raw?.title
        binding.tvMessage.text = data.raw?.description

        binding.tvLifeOsSuggestedQues.text = data.raw?.suggestions

        // set time
        binding.tvTime.text = data.timeText

        // Chat Box
        binding.lytChatBox.ivAddAttachment.gone()
        binding.lytChatBox.btnAction.setImageResource(R.drawable.image_ai_message_send_3)
        binding.lytChatBox.btnAction.alpha = 0.5f
        binding.lytChatBox.btnAction.isClickable = false
        binding.lytChatBox.chatEtx.hint = getString(R.string.reply_to_life_os)
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener {
            navigateUpSafe()
        }

        /*binding.icThumbsDown.setOnClickListener {
            displayHelpUsImproveBS()
        }*/

        binding.lytChatBox.btnAction.setOnClickListener {
            handleRelatedQuesClick(binding.lytChatBox.chatEtx.text.toString())
        }
    }

    /*private fun displayHelpUsImproveBS() {
        setFragmentResultListener(HELP_US_IMPROVE_BS_INSIGHTS){ _, bundle ->
            val feedbackText = bundle.getString("feedbackText")
            val reasons = bundle.getStringArrayList("reasons")
            if(feedbackText.isNullOrEmpty()){
                return@setFragmentResultListener
            }
            viewModel.submitDislikeBtmShtData(feedbackText, reasons){
                navigateUpSafe()
            }
        }
        navigate(
            R.id.helpUsImproveBottomSheet,
            Bundle().apply {
                putStringArrayList(
                    "reasons",
                    ArrayList<String>().apply {
                        this.add(getString(R.string.text_inaccurate))
                        this.add(getString(R.string.text_out_of_date))
                        this.add(getString(R.string.text_too_short))
                        this.add(getString(R.string.text_this_isn_t_helpful))
                    }
                )
            }
        )
    }*/

    override fun subscribeObservers() {
        binding.lytChatBox.chatEtx.addTextChangedListener { editable ->
            if(editable.isNullOrEmpty()){
                binding.lytChatBox.btnAction.alpha = 0.5f
                binding.lytChatBox.btnAction.isClickable = false
            }else{
                binding.lytChatBox.btnAction.alpha = 1f
                binding.lytChatBox.btnAction.isClickable = true
            }
        }
    }

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

                val v = (currentChartView as? BarChartSingleInsight1) ?: BarChartSingleInsight1(ctx,null)
                v.setDataSet(
                    data?.list?:arrayListOf(),
                    data?.yAxisRange?:arrayListOf(),
                    data?.avgValue,
                    -1,
                    data?.contributorType,
                    data?.optimalRange,
                    data?.nonNullDataCount?:0,
                    data?.xAxisRangeInsights!!
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
                    selectedPosition = -1,
                    data.xAxisRangeInsights
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

            GraphsKey.BAR_PLOT_COLOR -> {
                drawBarPlotColorChart(payload?.timeSeriesPayload)
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

    private fun drawBarPlotColorChart(payload: Any?): View? {
        val ctx = binding.chartContainer.context
        val data = payload as? TimeSeriesPayload ?: return null
        val v = (currentChartView as? SleepSingleLineChartInternal)
            ?: SleepSingleLineChartInternal(ctx, null)
        val yAxis = buildYAxis(data.list.map { it.value1 })
        val avgValue = data.list.mapNotNull { it.value1 }.average().toFloat() to data.unitLabel
        val nonNull = data.list.map { it.value1 }.count { it != null }
        val list = data.list.map { it.value1 }.mapIndexed { idx, d -> GraphDataModel(value1 = data.list[idx].value1, date = data.list[idx].date) }
        val showOverlay = true
        v.setDataSet(
            list,
            yAxis,
            getXAxisRange(data),
            avgValue,
            -1,
            showOverlay,
            null,
            data.selectedPeriod,
            nonNull,
            false
        )
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
    fun getXAxisRange(data: TimeSeriesPayload): List<LocalDate> {
        return when (data.selectedPeriod) {
            InternalSelectedPeriod.MONTH -> {
                val monthListString = ArrayList<LocalDate>()
                var lastYearMonth: YearMonth? = null
                data.list.forEach {
                    val currentYearMonth = YearMonth.from(it.date)
                    if (lastYearMonth == null) {
                        lastYearMonth = currentYearMonth
                        monthListString.add(currentYearMonth.atDay(1))
                    } else if (lastYearMonth != currentYearMonth) {
                        lastYearMonth = currentYearMonth
                        monthListString.add(currentYearMonth.atDay(1))
                    }
                }
                return monthListString
            }

            InternalSelectedPeriod.WEEK -> {
                val weekListReturn = ArrayList<LocalDate>()

                var lastWeek: Int? = null
                data.list.forEach {
                    val date = it.date

                    val weekFields = WeekFields.of(DayOfWeek.MONDAY, 7)
                    val weekNumber = date.get(weekFields.weekOfWeekBasedYear())

                    if (lastWeek == null) {
                        lastWeek = weekNumber
                        weekListReturn.add(date)
                    } else if (lastWeek != weekNumber) {
                        lastWeek = weekNumber
                        weekListReturn.add(date)
                    }
                }
                weekListReturn
            }

            else -> {
                val dayList = ArrayList<LocalDate>()
                data.list.forEach {
                    dayList.add(it.date)
                }
                return dayList
            }
        }
    }

}