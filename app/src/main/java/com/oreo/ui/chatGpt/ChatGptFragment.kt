package com.oreo.ui.chatGpt

import android.media.audiofx.Visualizer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentChatGptBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChatGptOverview
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.absoluteValue


@AndroidEntryPoint
class ChatGptFragment : BaseFragment<FragmentChatGptBinding>(FragmentChatGptBinding::inflate) {
    companion object {
        fun getStartData(
            threadId: String?,
            defaultMessage: String?,
            userMessage: String?,
            title: String?,
            aiTopic: AITopics
        ): Pair<Int, Bundle?> {
            return Pair(R.id.chatGptFragment, Bundle().apply {
                putString("threadId", threadId ?: "")
                putString("defaultMessage", defaultMessage ?: "")
                putString("userMessage", userMessage ?: "")
                putString("title", title ?: "")
                putSerializable("aiTopic", aiTopic)
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

        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_ai_page_visit)
        setAdapter()

        if (viewModel.threadId.isNullOrEmpty()) {
            viewModel.generateThreadId()
        } else {
            viewModel.loadMessagesByThreadId(viewModel.threadId!!)
            viewModel.threadTitle.postValue(args.title)
        }

    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            LOGS.d("RecognitionListener", "onReadyForSpeech() $params")
        }

        override fun onBeginningOfSpeech() {
            LOGS.d("RecognitionListener", "onBeginningOfSpeech()")
        }

        override fun onRmsChanged(rmsdB: Float) {
            //LOGS.d("RecognitionListener", "onRmsChanged - $rmsdB")
            binding.lytAudio.viewAudioVisualizer.updateRms(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            LOGS.d("RecognitionListener", "onBufferReceived() - $buffer")
        }

        override fun onEndOfSpeech() {
            LOGS.d("RecognitionListener", "onEndOfSpeech()")
        }

        override fun onError(error: Int) {}

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.getOrNull(0) ?: ""
            binding.lytAudio.testUserText.text = text
            LOGS.d("RecognitionListener", "onResults ${matches?.getOrNull(0)}")
            sendMessage(text)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial =
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            LOGS.d("RecognitionListener", " onPartialResults ${partial?.getOrNull(0)}")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            LOGS.d("RecognitionListener", " onEvent $eventType")
        }
    }


    private fun setAdapter() {
        with(binding.rv) {
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
            }
        }
    }

    override fun initListener() {

        binding.testAudio.setOnClickListener {
            binding.lytAudio.root.visible()
        }
        binding.lytAudio.ivCross.setOnClickListener {
            binding.lytAudio.root.gone()
            viewModel.isAudioMode = false
            viewModel.stopSpeechRecognition()
        }
        binding.lytAudio.bTextMode.setOnClickListener {
            binding.lytAudio.root.gone()
            viewModel.isAudioMode = false
            viewModel.stopSpeechRecognition()
        }
        binding.lytAudio.bAudioMode.setOnClickListener {
            viewModel.isAudioMode = true
            viewModel.startSpeechRecognition(listener)
        }

        binding.ivHistory.setOnClickListener {
            navigate(ChatGptFragmentDirections.actionChatGptFragmentToChatHistoryFragment())
        }

        /* binding.lytChatBox.btnNewChat.setOnClickListener {
             navigate(ChatGptFragmentDirections.actionChatGptFragmentSelf("",""))
         }*/

        binding.lytChatBox.btnSendMessage.setOnClickListener {
            if (viewModel.fetchInProgress.value == true) {
                viewModel.stopResponseGeneration()
            } else {
                if (binding.lytChatBox.chatEtx.text.isNullOrEmpty().not()) {
                    sendMessage(binding.lytChatBox.chatEtx.text.toString())
                }
            }
        }

        binding.toolbar.backBtn.setOnClickListener {
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
                    binding.lytAudio.viewAudioVisualizer.updateRms(amplitude)
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

    fun sendMessage(message:String) {
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

    override fun subscribeObservers() {

        keyboardListener = ViewTreeObserver.OnGlobalLayoutListener {
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
        requireView().viewTreeObserver.addOnGlobalLayoutListener(keyboardListener)

        viewModel.threadTitle.observe(this) {
            binding.tvChatTitle.apply {
                visible()
                text = it
            }
        }

        binding.lytChatBox.chatEtx.doOnTextChanged { text, start, before, count ->
            if (viewModel.fetchInProgress.value == true) return@doOnTextChanged

            if (text.isNullOrEmpty()) {
                binding.lytChatBox.btnSendMessage.setImageResource(0)
                binding.vOverlay.gone()
            } else {
                binding.lytChatBox.btnSendMessage.setImageResource(R.drawable.ic_ai_send_message)
                binding.vOverlay.visible()
            }

        }

        viewModel.fetchInProgress.observe(this) {
            if (it) {
                binding.lytChatBox.chatEtx.isEnabled = false
                binding.lytChatBox.chatEtx.setText(getString(R.string.text_generating_data))
                binding.lytChatBox.chatEtx.setTextColor(android.graphics.Color.parseColor("#9ecfff"))
                binding.lytChatBox.btnSendMessage.setImageResource(R.drawable.ic_round_stop_circle)
            } else {
                binding.lytChatBox.chatEtx.isEnabled = true
                binding.lytChatBox.chatEtx.setText("")
                binding.lytChatBox.chatEtx.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                binding.lytChatBox.btnSendMessage.setImageResource(0)
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
                binding.rv.smoothScrollToPosition(mAdapter.getItemCount() - 1)
            }
        }

        viewModel.chatGptOverview.observe(this) {
            it?.let {
                mAdapter.items = it
            }
        }
    }

}

enum class AITopics {
    SLEEP, READINESS, ACTIVITY, STRESS, MENSTRUAL_HEALTH, WORKOUT, GENERAL
}