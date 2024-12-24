package com.oreo.ui.chatGpt

import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.AiMeals
import com.noisefit.data.model.AiWorkout
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentChatGptBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.revealFromBottom
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChatGptOverview
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.absoluteValue


@AndroidEntryPoint
class ChatGptFragment : BaseFragment<FragmentChatGptBinding>(FragmentChatGptBinding::inflate) {
    /**
     *   val (frag, bundle) = ChatGptFragment.getStartData(
     *                 null,
     *                 null,
     *                 null,
     *                 null,
     *                 AITopics.GENERAL
     *             )
     *             navigate(frag, bundle)
     */
    companion object {
        fun getStartData(
            threadId: String?,
            defaultMessage: String?,
            userMessage: String?,
            title: String?,
            aiTopic: AITopics,
            meal: AiMeals? = null,
            workout: AiWorkout? = null,
            planType: PlanType? = null
        ): Pair<Int, Bundle?> {
            return Pair(R.id.chatGptFragment, Bundle().apply {
                putString("threadId", threadId ?: "")
                putString("defaultMessage", defaultMessage ?: "")
                putString("userMessage", userMessage ?: "")
                putString("title", title ?: "")
                putSerializable("aiTopic", aiTopic)
                putSerializable("planType", planType ?: PlanType.NONE)
                putParcelable("meal", meal)
                putParcelable("workout", workout)
            })
        }
    }

    private val viewModel: ChatGptViewModel by viewModels()
    private val args: ChatGptFragmentArgs by navArgs()

    private val mAdapter: ChatGptAdapter by lazy {
        ChatGptAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.threadId = args.threadId
        viewModel.defaultMessage = args.defaultMessage
        viewModel.userMessage = args.userMessage
        viewModel.meal = args.meal
        viewModel.workout = args.workout
        viewModel.planType = args.planType

        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_ai_page_visit)
        setAdapter()

        if (viewModel.planType == PlanType.NONE) {
            if (viewModel.threadId.isNullOrEmpty()) {
                viewModel.generateThreadId()
            } else {
                viewModel.loadMessagesByThreadId(viewModel.threadId!!)
            }
        } else {
            viewModel.generateInitMessage()
        }

        setVideo()
    }

    private fun setVideo() {
        val fileName =
            ("android.resource://" + requireContext().packageName) + "/raw/video_ai_generating"
        val uri = Uri.parse(fileName)
        val videoView = binding.videoView
        videoView.setVideoURI(uri)
        videoView.pause()
    }

    private fun setAdapter() {
        with(binding.rvChats) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        mAdapter.itemClickListener = { item, position ->
            when (item) {
                is ChatGptOverview.SentMessage -> {

                }

                is ChatGptOverview.ReceivedMessage -> {

                }

                is ChatGptOverview.RetryMessage -> {
                    viewModel.retryApi()

                }

                is ChatGptOverview.ThinkingMessage -> {

                }

                is ChatGptOverview.HeaderMeal -> {}
                is ChatGptOverview.HeaderWorkout -> {}
            }
        }
    }

    override fun initListener() {

        binding.lytChatBox.btnAudioChat.setOnClickListener {
            navigate(ChatGptFragmentDirections.actionChatGptFragmentToAudioAiFragment(null).apply {
                planType = PlanType.NONE
            })
        }

        binding.rvChats.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(-1)) {
                    binding.imageGradientTop.gone()
                } else {
                    if (binding.imageGradientTop.visibility == View.GONE) {
                        binding.imageGradientTop.visible()
                    }
                }

            }
        })

        /*binding.ivHistory.setOnClickListener {
            navigate(ChatGptFragmentDirections.actionChatGptFragmentToChatHistoryFragment())
        }*/

        binding.lytGeneratingData.ivStopGenerating.setOnClickListener {
            viewModel.stopResponseGeneration()
        }

        binding.lytSaveData.btnSave.setOnClickListener {
            viewModel.savePlanData()
        }

        binding.lytSaveData.btnCancel.setOnClickListener {
            binding.lytSaveData.root.gone()
            binding.ivGeneratingGradient.gone()
            binding.videoView.stopPlayback()
            binding.videoView.gone()
        }

        /* binding.lytChatBox.btnNewChat.setOnClickListener {
             navigate(ChatGptFragmentDirections.actionChatGptFragmentSelf("",""))
         }*/

        /* binding.lytChatBox.btnSendMessage.setOnClickListener {
             if (viewModel.fetchInProgress.value == true) {
                 viewModel.stopResponseGeneration()
             } else {
                 if (binding.lytChatBox.chatEtx.text.isNullOrEmpty().not()) {
                     sendMessage(binding.lytChatBox.chatEtx.text.toString())
                 }
             }
         }*/

        binding.ivClose.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytChatBox.chatEtx.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage(binding.lytChatBox.chatEtx.text.toString())
                true
            } else false
        })


    }

    private fun setupVisualizer(audioSessionId: Int) {
        Visualizer(audioSessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1] // Maximum capture size
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    visualizer: Visualizer,
                    waveform: ByteArray,
                    samplingRate: Int
                ) {
                    val amplitude = waveform.map { it.toInt().absoluteValue }.average().toFloat()
                    // binding.lytAudio.viewAudioVisualizer.updateRms(amplitude)
                }

                override fun onFftDataCapture(
                    visualizer: Visualizer,
                    fft: ByteArray,
                    samplingRate: Int
                ) {
                    // Optional: FFT data for frequency visualization
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false)
            enabled = true
        }
    }

    fun sendMessage(message: String) {
        if (message.isNotEmpty()) {
            viewModel.addSentMessage(message)
            viewModel.addThinkingMessage()


            //viewModel.addReceivedMessage("", true)
            binding.lytChatBox.chatEtx.setText("")

            viewModel.askQuestionStream(message)

            //viewModel.askQuestion(message)
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_ai_message_submit)
        }

    }

    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onDestroyView() {
        super.onDestroyView()
        requireView().viewTreeObserver.removeOnGlobalLayoutListener(keyboardListener)
    }

    private fun showSnackBar(text: String) {
        binding.lytSnackbar.apply {
            this.tvMessage.text = text
            this.root.visible()
            startSnackBarRemoveTimer()

            this.tvView.setOnClickListener {
                if (viewModel.showSavePlan.value?.peekContent() == AiPlanType.MEAL) {
                    navigate(ChatGptFragmentDirections.actionChatGptFragmentToAiMealPlanFragment())
                } else if (viewModel.showSavePlan.value?.peekContent() == AiPlanType.WORKOUT) {
                    navigate(
                        ChatGptFragmentDirections.actionChatGptFragmentToWorkoutPlansFragment()
                    )
                }
            }
        }
    }

    private fun startSnackBarRemoveTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.removeSnackBar()
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        nullableBinding?.lytSnackbar?.root?.gone()
    }

    override fun subscribeObservers() {
        viewModel.removeSnackBar.observe(this) {
            it.getContent()?.let {
                nullableBinding?.lytSnackbar?.root?.gone()
            }
        }

        viewModel.aiGeneratedPlanSaved.observe(this) {
            binding.lytSaveData.root.gone()

            binding.ivGeneratingGradient.gone()
            binding.videoView.stopPlayback()
            binding.videoView.gone()

            if (viewModel.showSavePlan.value?.peekContent() == AiPlanType.MEAL) {
                showSnackBar(getString(R.string.text_your_diet_plan_is_saved))
            } else if (viewModel.showSavePlan.value?.peekContent() == AiPlanType.WORKOUT) {
                showSnackBar(getString(R.string.text_your_workout_plan_is_saved))
            }
        }

        /* keyboardListener = ViewTreeObserver.OnGlobalLayoutListener {
             view?.let {
                 val insets = ViewCompat.getRootWindowInsets(it)
                 val isKeyboardVisible = insets?.isVisible(WindowInsetsCompat.Type.ime())
                 if (viewModel.threadTitle.value.isNullOrEmpty().not()) {
                     if (isKeyboardVisible == true) {
                         binding.tvChatTitle.gone()
                     } else {
                         binding.tvChatTitle.visible()
                     }
                 }
             }
         }
         requireView().viewTreeObserver.addOnGlobalLayoutListener(keyboardListener)*/

        viewModel.showSavePlan.observe(this) {
            it.getContent()?.let {
                when (it) {
                    AiPlanType.WORKOUT -> showSaveWorkoutPlan()
                    AiPlanType.MEAL -> showSaveMealPlan()
                }
            }
        }

        viewModel.threadTitle.observe(this) {
            binding.tvChatTitle.apply {
                visible()
                text = it
            }
        }

        /*binding.lytChatBox.chatEtx.doOnTextChanged { text, start, before, count ->
            if (viewModel.fetchInProgress.value == true) return@doOnTextChanged

             if (text.isNullOrEmpty()) {
                 binding.lytChatBox.btnSendMessage.setImageResource(0)
                 binding.vOverlay.gone()
             } else {
                 binding.lytChatBox.btnSendMessage.setImageResource(R.drawable.ic_ai_send_message)
                 binding.vOverlay.visible()
             }

        }*/

        viewModel.fetchInProgress.observe(this) {
            if (it) {
                binding.videoView.start()
                binding.videoView.visible()
                binding.ivGeneratingGradient.visible()
                binding.lytGeneratingData.root.visible()
                binding.lytChatBox.root.gone()
            } else {
                binding.videoView.stopPlayback()
                binding.videoView.gone()
                binding.ivGeneratingGradient.gone()
                binding.lytGeneratingData.root.gone()
                binding.lytChatBox.root.visible()
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
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.scrollToBottom.observe(this) {
            it.getContent()?.let {
                binding.rvChats.smoothScrollToPosition(mAdapter.getItemCount() - 1)
            }
        }

        viewModel.chatGptOverview.observe(this) {
            it?.let {
                mAdapter.items = it
            }
        }
    }

    private fun showSaveWorkoutPlan() {
        binding.ivGeneratingGradient.visible()
        binding.videoView.start()
        binding.videoView.visible()

        binding.lytSaveData.apply {
            testSaveQues.text = getString(R.string.text_would_you_like_to_save_this_workout_plan)
            root.revealFromBottom()
        }
    }


    private fun showSaveMealPlan() {
        binding.ivGeneratingGradient.visible()
        binding.videoView.start()
        binding.videoView.visible()

        binding.lytSaveData.apply {
            testSaveQues.text = getString(R.string.text_would_you_like_to_save_this_diet_plan)
            root.revealFromBottom()
        }
    }

}

enum class AITopics {
    SLEEP, READINESS, ACTIVITY, STRESS, MENSTRUAL_HEALTH, WORKOUT, GENERAL
}

enum class PlanType {
    WORKOUT, DIET, NONE
}