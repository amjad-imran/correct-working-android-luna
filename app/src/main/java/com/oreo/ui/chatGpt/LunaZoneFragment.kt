package com.oreo.ui.chatGpt

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLunaZoneBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LunaZoneFragment : BaseFragment<FragmentLunaZoneBinding>(FragmentLunaZoneBinding::inflate) {

    private val viewModel: LunaZoneViewModel by viewModels()
    private val suggestionsAdapter: SuggestedQuestionAdapter by lazy {
        SuggestedQuestionAdapter(onQuestionClicked = {
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                "${it.ques}",
                null,
                AITopics.GENERAL
            )
            navigate(frag, bundle)
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()
        viewModel.getLunaZoneData()
        setVideo()
    }

    private fun setRecycler() {
        binding.rvQuestions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuestions.adapter = suggestionsAdapter
    }

    private fun setVideo() {
        val fileName = ("android.resource://" + requireContext().packageName) + "/raw/video_summary"
        val uri = Uri.parse(fileName)
        val videoView = binding.lytDailySummaryAvailable.videoView
        videoView.setVideoURI(uri)
        videoView.start()

        videoView.setOnPreparedListener { mediaPlayer ->
            val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
            val screenRatio = videoView.width / videoView.height.toFloat()
            val scaleX = videoRatio / screenRatio
            if (scaleX >= 1f) {
                videoView.scaleX = scaleX
            } else {
                videoView.scaleY = 1f / scaleX
            }
            //mediaPlayer.isLooping = true
        }
    }

    override fun initListener() {
        binding.svMain.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > 0) {
                binding.imageGradientTop.visible()
            } else {
                binding.imageGradientTop.gone()
            }
        }

        binding.lytChatWidget.textView140.setOnClickListener {
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                null,
                null,
                AITopics.GENERAL
            )
            navigate(frag, bundle)
        }
        binding.lytChatWidget.ivHistory.setOnClickListener {
            navigate(R.id.chatHistoryFragment)
        }
        binding.lytChatWidget.ivMic.setOnClickListener {
            navigate(R.id.audioAiFragment)
        }

        binding.lytDailySummaryAvailable.root.setOnClickListener {
            navigate(R.id.aiSummaryFragment)
        }
        binding.lytPlans.lytWorkoutPlan.root.setOnClickListener {
            navigate(R.id.workoutPlansFragment)
        }
        binding.lytPlans.lytMealPlan.root.setOnClickListener {
            navigate(R.id.aiMealPlanFragment)

        }

        binding.lytPlans.lytWorkoutPlanSetup.root.setOnClickListener {
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                "Build me a workout plan",
                null,
                AITopics.GENERAL
            )
            navigate(frag, bundle)
        }
        binding.lytPlans.lytMealPlanSetup.root.setOnClickListener {
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                "Build me a diet plan",
                null,
                AITopics.GENERAL
            )
            navigate(frag, bundle)
        }
    }

    override fun subscribeObservers() {
        viewModel.suggestedQuestions.observe(this) {
            suggestionsAdapter.setDataSet(it)
        }
        viewModel.workoutPlanState.observe(this) {
            if (it) {
                binding.lytPlans.lytWorkoutPlan.apply {
                    ivWorkoutPlan.setImageResource(R.drawable.image_ai_w_plan)
                    ivBottomIcon.setImageResource(R.drawable.ic_workout_plan)
                    tvPlanName.text = getString(R.string.text_my_workout_plan)
                    tvPlanName.setTextColor(Color.parseColor("#A8FFFF"))
                    root.visible()
                }
                binding.lytPlans.lytWorkoutPlanSetup.root.gone()
            } else {
                binding.lytPlans.lytWorkoutPlan.root.gone()
                binding.lytPlans.lytWorkoutPlanSetup.root.visible()
            }
        }
        viewModel.mealPLanState.observe(this) {
            if (it) {
                binding.lytPlans.lytMealPlan.apply {
                    ivWorkoutPlan.setImageResource(R.drawable.image_ai_m_plan)
                    ivBottomIcon.setImageResource(R.drawable.ic_meal_plan)
                    tvPlanName.text = getString(R.string.text_my_nutrition_plan)
                    tvPlanName.setTextColor(Color.parseColor("#61613D"))
                    root.visible()
                }
                binding.lytPlans.lytMealPlanSetup.root.gone()
            } else {
                binding.lytPlans.lytMealPlan.root.gone()
                binding.lytPlans.lytMealPlanSetup.root.visible()
            }
        }



        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

}