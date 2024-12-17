package com.oreo.ui.chatGpt

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLunaZoneBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

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

        setBlur()
        setRecycler()
        viewModel.getLunaZoneData()

    }

    private fun getVideoHeight() {
        binding.rootView.post {
            val height: Int = binding.rootView.height

            val layoutParams = binding.lytDailySummaryAvailable.root.layoutParams
            layoutParams.height = (height.toFloat() * 0.65).roundToInt()
            binding.lytDailySummaryAvailable.root.layoutParams = layoutParams

            setVideo()

        }
    }

    override fun onResume() {
        super.onResume()
        getVideoHeight()
    }

    private fun setBlur() {
        val radius = 20f
        val decorView = binding.lytChatWidget.root
        val rootView = binding.rootView
        val windowBackground = decorView.background

        val blurAlgo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffectBlur()
        } else {
            RenderScriptBlur(requireContext())
        }
        binding.blurView.setupWith(rootView, blurAlgo) // or RenderEffectBlur
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)

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
        viewModel.summaryStates.observe(this) {
            when (it) {
                SummaryStates.NO_DEVICE -> {
                    binding.lytNoDevice.root.visible()
                    binding.lytDailySummaryAvailable.root.gone()
                    binding.lytNoData.root.gone()
                }

                SummaryStates.NO_DATA -> {
                    binding.lytNoDevice.root.gone()
                    binding.lytDailySummaryAvailable.root.gone()
                    binding.lytNoData.root.visible()
                }

                SummaryStates.GENERATING -> {
                    binding.lytNoDevice.root.gone()
                    binding.lytDailySummaryAvailable.root.gone()
                    binding.lytNoData.root.gone()
                }

                SummaryStates.DATA_AVAILABLE -> {
                    binding.lytNoDevice.root.gone()
                    binding.lytDailySummaryAvailable.root.visible()
                    binding.lytDailySummaryAvailable.tvDate.text = LocalDate.now().format(
                        DateTimeFormatter.ofPattern("E, MMM dd",
                            Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                        ))

                    binding.lytNoData.root.gone()
                }

                SummaryStates.NONE -> {
                    binding.lytNoDevice.root.gone()
                    binding.lytDailySummaryAvailable.root.gone()
                    binding.lytNoData.root.gone()
                }
            }
        }

        viewModel.suggestedQuestions.observe(this) {
            suggestionsAdapter.setDataSet(it)
        }

        viewModel.planState.observe(this) {
            setPlanUi(it)
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

    private fun setPlanUi(data: Pair<Boolean, Boolean>) {

        val (workoutState, mealState) = data

        if (workoutState) {
            binding.lytPlans.lytWorkoutPlan.apply {
                ivWorkoutPlan.setImageResource(R.drawable.image_ai_w_plan)
                ivBottomIcon.setImageResource(R.drawable.ic_workout_plan)
                tvPlanName.text = getString(R.string.text_my_workout_plan)
                tvPlanName.setTextColor(Color.parseColor("#A8FFFF"))
                root.visible()
            }
            binding.lytPlans.lytWorkoutPlanSetup.root.gone()
        } else {
            if (mealState) {
                binding.lytPlans.lytWorkoutPlan.apply {
                    ivWorkoutPlan.setImageResource(R.drawable.image_ai_w_plan)
                    ivBottomIcon.setImageResource(R.drawable.image_w_plus)
                    tvPlanName.text = getString(R.string.text_setup_nworkout_plan)
                    tvPlanName.setTextColor(Color.parseColor("#A8FFFF"))
                    root.visible()
                }
                binding.lytPlans.lytWorkoutPlanSetup.root.gone()
            } else {
                binding.lytPlans.lytWorkoutPlan.root.gone()
                binding.lytPlans.lytWorkoutPlanSetup.root.visible()
            }

        }


        if (mealState) {
            binding.lytPlans.lytMealPlan.apply {
                ivWorkoutPlan.setImageResource(R.drawable.image_ai_m_plan)
                ivBottomIcon.setImageResource(R.drawable.ic_meal_plan)
                tvPlanName.text = getString(R.string.text_my_nutrition_plan)
                tvPlanName.setTextColor(Color.parseColor("#61613D"))
                root.visible()
            }
            binding.lytPlans.lytMealPlanSetup.root.gone()
        } else {
            if (workoutState) {
                binding.lytPlans.lytMealPlan.apply {
                    ivWorkoutPlan.setImageResource(R.drawable.image_ai_m_plan)
                    ivBottomIcon.setImageResource(R.drawable.image_meal_plus)
                    tvPlanName.text = getString(R.string.text_setup_diet_plan)
                    tvPlanName.setTextColor(Color.parseColor("#61613D"))
                    root.visible()
                }
                binding.lytPlans.lytMealPlanSetup.root.gone()
            } else {
                binding.lytPlans.lytMealPlan.root.gone()
                binding.lytPlans.lytMealPlanSetup.root.visible()
            }

        }

    }

}