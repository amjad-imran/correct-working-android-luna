package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.data.model.AiHeaderInsight1
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import com.oreo.ui.lifeos.insightsLvl1.HELP_US_IMPROVE_BS_INSIGHTS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsInsightDetailsFragment :
    BaseFragment<FragmentLifeOsInsightDetailsBinding>(FragmentLifeOsInsightDetailsBinding::inflate) {

    private val viewModel: LifeOsInsightDetailsViewModel by viewModels()

    private val args: LifeOsInsightDetailsFragmentArgs by navArgs()

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
    "graphs": "hrv",
    "graph": [
      {
        "date": "2025-11-09",
        "master_avg_hrv": 47
      },
      {
        "date": "2025-11-10",
        "master_avg_hrv": 70
      },
      {
        "date": "2025-11-11",
        "master_avg_hrv": 36
      },
      {
        "date": "2025-11-12",
        "master_avg_hrv": 50
      },
      {
        "date": "2025-11-13",
        "master_avg_hrv": 50
      },
      {
        "date": "2025-11-14",
        "master_avg_hrv": 51
      },
      {
        "date": "2025-11-15",
        "master_avg_hrv": 50
      },
      {
        "date": "2025-11-16",
        "master_avg_hrv": 40
      },
      {
        "date": "2025-11-17",
        "master_avg_hrv": 35
      }
    ]
  }
        """.trimIndent()
        viewModel.insightData = Gson().fromJson(jsonRes, InsightItemResponseModel::class.java)*/
        viewModel.insightData = args.insightData
        setUi()
        setRecycler()
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
                mainText = viewModel.insightData?.title ?: "",
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
    }

    override fun initListener() {
        binding.icThumbsDown.setOnClickListener {
            displayHelpUsImproveBS()
        }

        binding.lytChatBox.chatEtx.apply {
            setCursorVisible(false)
            setFocusable(false)
            setFocusableInTouchMode(false)
            setClickable(true)
        }

        binding.lytChatBox.root.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL
            )
            navigate(
                frag, bundle
            )
        }
        binding.lytChatBox.chatEtx.setOnClickListener {
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL
            )
            navigate(
                frag, bundle
            )
        }

        binding.lytChatBox.btnAction.setOnClickListener {
            val (frag, bundle) = AudioAiFragment.getStartData(
                PlanType.NONE
            )
            navigate(frag, bundle)
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

    }
}