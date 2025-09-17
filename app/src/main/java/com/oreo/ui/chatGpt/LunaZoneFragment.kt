package com.oreo.ui.chatGpt

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLunaZoneBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.chatGpt.audio.AudioAiFragment
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
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    private val suggestionsAdapter: SuggestedQuestionAdapter by lazy {
        SuggestedQuestionAdapter(onQuestionClicked = {

            if (viewModel.ringDataStore.getRingDevice() == null) {
                context.showShortToast(getString(R.string.text_luna_ai_message))
                return@SuggestedQuestionAdapter
            }

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.Home_lunaai_suggested_question,
                HashMap<String, Any>().apply {
                    this["question"] = it.ques ?: ""
                }
            )

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
            layoutParams.height = (height.toFloat() * viewModel.CARD_RATIO).roundToInt()
            binding.lytDailySummaryAvailable.root.layoutParams = layoutParams

            setVideo()


            val layoutParamNoData = binding.lytNoData.root.layoutParams
            layoutParamNoData.height = (height.toFloat() * viewModel.CARD_RATIO).roundToInt()
            binding.lytNoData.root.layoutParams = layoutParams

            val layoutParamNoDevice = binding.lytNoDevice.root.layoutParams
            layoutParamNoDevice.height = (height.toFloat() * viewModel.CARD_RATIO).roundToInt()
            binding.lytNoDevice.root.layoutParams = layoutParams

            val layoutParamGenerating = binding.lytGeneratingData.root.layoutParams
            layoutParamGenerating.height = (height.toFloat() * viewModel.CARD_RATIO).roundToInt()
            binding.lytGeneratingData.root.layoutParams = layoutParams

        }
    }

    override fun onResume() {
        super.onResume()
        getVideoHeight()
    }

    private fun setBlur() {
        val radius = 18f
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
        binding.ivMic.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.Home_lunaai_mic_button)

            val (frag, bundle) = AudioAiFragment.getStartData(
                PlanType.NONE
            )
            navigate(frag, bundle)
        }
        binding.svMain.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > 0) {
                binding.imageGradientTop.visible()
            } else {
                binding.imageGradientTop.gone()
            }
        }

        binding.lytChatWidget.textView140.setOnClickListener {
            navigate(R.id.aiTopQuestionsFragment, bundleOf("aiTopic" to AITopics.GENERAL))
        }
        binding.lytChatWidget.ivHistory.setOnClickListener {
            navigate(R.id.chatHistoryFragment)
        }
        binding.lytChatWidget.ivMic.setOnClickListener {
            val (frag, bundle) = AudioAiFragment.getStartData(
                PlanType.NONE
            )
            navigate(frag, bundle)
        }

        binding.lytDailySummaryAvailable.root.setOnClickListener {
            if (viewModel.ringDataStore.getRingDevice() == null) {
                context.showShortToast(getString(R.string.text_luna_ai_message))
                return@setOnClickListener
            }
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.home_lunaai_daily_digest_plan)
            navigate(R.id.aiSummaryFragment)
        }
        binding.lytPlans.lytWorkoutPlan.root.setOnClickListener {
            val workoutSetup = viewModel.planState.value?.first ?: false
            if (workoutSetup) {
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.home_lunaai_workout_plan)

                navigate(R.id.workoutPlansFragment)

            } else {
                if (viewModel.ringDataStore.getRingDevice() == null) {
                    context.showShortToast(getString(R.string.text_luna_ai_message))
                    return@setOnClickListener
                }

                val (frag, bundle) = ChatGptFragment.getStartData(
                    null,
                    null,
                    getString(R.string.text_build_me_a_workout_plan),
                    null,
                    AITopics.GENERAL,
                    planType = PlanType.WORKOUT
                )
                navigate(frag, bundle)
            }
        }
        binding.lytPlans.lytMealPlan.root.setOnClickListener {
            val mealSetup = viewModel.planState.value?.second ?: false
            if (mealSetup) {
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.home_lunaai_nutrition_plan)

                navigate(R.id.aiMealPlanFragment)
            } else {
                if (viewModel.ringDataStore.getRingDevice() == null) {
                    context.showShortToast(getString(R.string.text_luna_ai_message))
                    return@setOnClickListener
                }

                val (frag, bundle) = ChatGptFragment.getStartData(
                    null,
                    null,
                    getString(R.string.text_build_me_a_weekly_diet_plan),
                    null,
                    AITopics.GENERAL,
                    planType = PlanType.DIET
                )
                navigate(frag, bundle)
            }
        }

        binding.lytPlans.lytWorkoutPlanSetup.root.setOnClickListener {
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                getString(R.string.text_build_me_a_workout_plan),
                null,
                AITopics.GENERAL,
                planType = PlanType.WORKOUT
            )
            navigate(frag, bundle)
        }

        binding.lytPlans.lytMealPlanSetup.root.setOnClickListener {
            if (viewModel.ringDataStore.getRingDevice() == null) {
                context.showShortToast(getString(R.string.text_luna_ai_message))
                return@setOnClickListener
            }
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                getString(R.string.text_build_me_a_weekly_diet_plan),
                null,
                AITopics.GENERAL,
                planType = PlanType.DIET
            )
            navigate(frag, bundle)
        }

        binding.lytNoDevice.btnPair.setOnClickListener {
            startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
        }
    }

    override fun subscribeObservers() {
        mainViewModel.lunaZoneReloadConfirm.observe(this) {
            it.getContent()?.let {
                viewModel.getLunaZoneData()
            }
        }

        mainViewModel.syncTextState.observe(this) {
            if (it.isNullOrEmpty()) {
                //binding.imageLogo.visible()
                //binding.tvHeaderStatus.gone()
            } else {
                /*binding.imageLogo.gone()
                binding.tvHeaderStatus.apply {
                    text = getString(R.string.text_syncing_dot)
                    visible()
                }*/
                viewModel.summaryStates.postValue(SummaryStates.GENERATING)
            }
        }


        viewModel.summaryStates.observe(this) {

            var state = it

            if (mainViewModel.syncTextState.value.isNullOrEmpty().not()
                && mainViewModel.syncTextState.value.equals(context?.getString(R.string.text_all_set)).not()) {
                state = SummaryStates.GENERATING
            }
            when (state) {
                SummaryStates.NO_DEVICE -> {
                    binding.lytNoDevice.root.visible()
                    binding.lytDailySummaryAvailable.root.gone()
                    binding.lytNoData.root.gone()
                    binding.lytGeneratingData.root.gone()
                }

                SummaryStates.NO_DATA -> {
                    binding.lytNoDevice.root.gone()
                    binding.lytDailySummaryAvailable.root.gone()
                    binding.lytNoData.root.visible()
                    binding.lytGeneratingData.root.gone()

                    binding.lytNoData.tvNoData.post {
                        val width = binding.lytNoData.tvNoData.width.toFloat()

                        val shader = LinearGradient(
                            0f, 0f, width, 0f,
                            intArrayOf(
                                Color.parseColor("#C9EEFF"),
                                Color.parseColor("#BFEBFF"),
                                Color.parseColor("#4AC6FF")
                            ),
                            null,
                            Shader.TileMode.CLAMP
                        )
                        binding.lytNoData.tvNoData.paint.shader = shader
                        binding.lytNoData.tvNoData.invalidate()
                    }
                }

                SummaryStates.GENERATING -> {
                    binding.lytNoDevice.root.gone()
                    binding.lytDailySummaryAvailable.root.gone()
                    binding.lytNoData.root.gone()
                    binding.lytGeneratingData.root.visible()
                }

                SummaryStates.DATA_AVAILABLE -> {
                    binding.lytNoDevice.root.gone()
                    binding.lytDailySummaryAvailable.root.visible()
                    binding.lytGeneratingData.root.gone()
                    binding.lytDailySummaryAvailable.tvDate.text = LocalDate.now().format(
                        DateTimeFormatter.ofPattern(
                            "E, MMM dd",
                            Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                        )
                    )

                    binding.lytNoData.root.gone()
                }

                SummaryStates.NONE, null -> {
                    binding.lytNoDevice.root.gone()
                    binding.lytDailySummaryAvailable.root.gone()
                    binding.lytNoData.root.gone()
                    binding.lytGeneratingData.root.gone()
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
                    ivBottomIcon.setImageResource(R.drawable.image_meal_plus)
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