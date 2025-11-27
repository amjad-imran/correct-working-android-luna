package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.noisefit.data.model.AiHeaderInsight1
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.custom.NightTimeGraphViewOreo
import com.noisefit_commans.ui.custom.SleepGraphViewOreo
import com.noisefit_commans.ui.gone
import com.oreo.data.dataConverter.GraphsKey
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.custom.HRCombinedChart
import com.oreo.ui.custom.ODayTimeInteractiveGraph
import com.oreo.ui.custom.StressCombinedChart
import com.oreo.ui.custom.sleep.internal.SleepSingleBarChart
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientChartType
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientLineChartInternal
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import com.oreo.ui.lifeos.charts.PayloadData
import com.oreo.ui.lifeos.insightsLvl1.HELP_US_IMPROVE_BS_INSIGHTS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsInsightDetailsFragment :
    BaseFragment<FragmentLifeOsInsightDetailsBinding>(FragmentLifeOsInsightDetailsBinding::inflate) {

    private val viewModel: LifeOsInsightDetailsViewModel by viewModels()

    private val args: LifeOsInsightDetailsFragmentArgs by navArgs()

    private var currentChartView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jsonRes = """
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
        )
//        viewModel.insightData = args.insightData
        setUi()
        setRecycler()
        setGraph()
    }

    private fun setGraph() {
        val item = viewModel.insightData
        val view = provideChartView(item?.chartKey, item?.payload, item?.styleRes)
        binding.chartContainer.removeAllViews()
        if (view != null) {
            binding.chartContainer.gone()
            binding.chartContainer.addView(view)
            currentChartView = view
        } else {
            currentChartView = null
            binding.chartContainer.gone()
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
                footerText = data
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

        // Chat Box
        binding.lytChatBox.btnAction.setImageResource(R.drawable.image_ai_message_send_3)
        binding.lytChatBox.btnAction.alpha = 0.5f
        binding.lytChatBox.btnAction.isClickable = false
    }

    override fun initListener() {
        binding.icThumbsDown.setOnClickListener {
            displayHelpUsImproveBS()
        }

        binding.lytChatBox.btnAction.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = binding.lytChatBox.chatEtx.text.toString(),
                title = null,
                aiTopic = AITopics.GENERAL
            )
            navigate(
                frag, bundle
            )
        }
    }

    private fun displayHelpUsImproveBS() {
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
    }

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

                val v = (currentChartView as? SleepSingleBarChart) ?: SleepSingleBarChart(ctx,null)
                v.setDataSet(
                    data?.list?:arrayListOf(),
                    data?.yAxisRange?:arrayListOf(),
                    data?.avgValue,
                    -1,
                    data?.contributorType,
                    data?.optimalRange,
                    data?.nonNullDataCount?:0
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

}