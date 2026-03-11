package com.oreo.ui.chatGpt.audio

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAudioAiBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.PlanType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt


@AndroidEntryPoint
class AudioAiFragment : BaseFragment<FragmentAudioAiBinding>(FragmentAudioAiBinding::inflate) {

    val viewModel: AudioAiViewModel by viewModels()
    val args: AudioAiFragmentArgs by navArgs()
    private var mVisualizer: Visualizer? = null
    private var dotAnimationJob: Job? = null


    companion object {
        fun getStartData(
            planType: PlanType,
            text: String? = null,
        ): Pair<Int, Bundle?> {
            return Pair(R.id.audioAiFragment, Bundle().apply {
                putSerializable("planType", planType)
                putString("text", text)
            })
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding.tvMessage.text = getString(R.string.text_setting_up)

//        if (viewModel.isCalibrated().not()) {
//            navigate(
//                AudioAiFragmentDirections.actionAudioAiFragmentToAudioAiCalibrationFragment(
//                    args.text
//                ).apply {
//                    planType = args.planType
//                })
//            return
//        }

        binding.tvMessage.apply {
            setTextColor(Color.parseColor("#80E4FF"))
            val textShader: Shader = LinearGradient(
                0f,
                this.paint.measureText(this.text.toString()),
                0f,
                0f,
                intArrayOf(
                    Color.parseColor("#80E4FF"),
                    Color.parseColor("#74D0FF"),
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            this.paint.shader = textShader
        }

        viewModel.planType = args.planType
        viewModel.planQusetion = args.text

        /*if (args.planType == PlanType.WORKOUT) {
            binding.tvAskLuna.visible()
            args.text?.let {
                binding.tvMessage.text = getString(R.string.text_how_to_perform_a_value, it)
            }
        } else if (args.planType == PlanType.DIET) {
            binding.tvAskLuna.visible()
            val questions = arrayListOf(
                getString(R.string.text_diet_1),
                getString(R.string.text_diet_2)
            )
            binding.tvMessage.text = questions.random()
        }*/

        if (args.planType != PlanType.NONE) {
            binding.ivCross.setImageResource(R.drawable.ic_toolbar_back_ai)
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        viewModel.getCredentials()
        setVideo()
    }


    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.cleanup()
                navigateUpSafe()
            }
        }

    private fun setVideo() {
       /* val fileName = ("android.resource://" + requireContext().packageName) + "/raw/video_chat_ai"
        val uri = Uri.parse(fileName)
        val videoView = binding.videoView
        videoView.setVideoURI(uri)
        videoView.stopPlayback()
        videoView.setOnPreparedListener { it.isLooping = true }
        videoView.start()

        adjustVideoSize(0.6f)*/
    }

    private fun adjustVideoSize(heightPercent: Float) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)

        constraintSet.constrainPercentHeight(R.id.videoView, heightPercent)

        constraintSet.applyTo(binding.root)
    }


    override fun onDestroyView() {
        viewModel.cleanup()
        mVisualizer?.release()
        dotAnimationJob?.cancel()
        super.onDestroyView()
    }

    private fun checkMicrophonePermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            micPermissionResult.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onResume() {
        super.onResume()
        setVideo()
    }

    private val micPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            showSettingUpAnimation()
        } else {
            context.showShortToast("Permission Required")
            navigateUpSafe()
        }
    }


    override fun initListener() {
        binding.tvMessageTest.setOnClickListener {
            viewModel.localDataStore.saveAudioMaxAmp(0)
            viewModel.cleanup()
            navigateUpSafe()
        }

        binding.ivCross.setOnClickListener {
            viewModel.cleanup()
            navigateUpSafe()
        }

        binding.ivMic.setOnClickListener {
            if (viewModel.isAiReplying()) {
                viewModel.interruptAi()
                viewModel.audioAiState.postValue(AudioAiState.AI_TALKING_STOP)
            } else {
                if (viewModel.isMicOn && viewModel.audioAiState.value != AudioAiState.LISTENING) {
                    viewModel.audioAiState.postValue(AudioAiState.DEFAULT)
                    viewModel.stopRecording(false)
                    viewModel.sendRecordingToServer()
                } else {
                    viewModel.stopRecording(true)
                    viewModel.audioAiState.postValue(AudioAiState.SPEAK_NOW)
                    viewModel.startNewRecording(true)
                }
            }
        }

        binding.ivTextChat.setOnClickListener {
            navigate(
                AudioAiFragmentDirections.actionAudioAiFragmentToAiTopQuestionsFragment(
                    AITopics.GENERAL
                )
            )
        }
    }

    private fun micStateOff() {
        viewModel.isMicOn = false
        binding.ivMic.visible()
        binding.ivMic.setBackgroundColor(android.graphics.Color.parseColor("#F76968"))
        binding.ivMic.setImageResource(R.drawable.ic_ai_mic_off)
    }

    private fun micStateGenerating() {
        viewModel.isMicOn = false
        binding.ivMic.invisible()
    }

    private fun micStateOn() {
        viewModel.isMicOn = true
        binding.ivMic.visible()
        binding.ivMic.setBackgroundColor(android.graphics.Color.parseColor("#26FFFFFF"))
        binding.ivMic.setImageResource(R.drawable.ic_ai_mic)
    }

    private fun micStateShowStop() {
        viewModel.isMicOn = true
        binding.ivMic.visible()
        binding.ivMic.setBackgroundColor(android.graphics.Color.parseColor("#26FFFFFF"))
        binding.ivMic.setImageResource(R.drawable.ic_mic_stop)
    }

    override fun subscribeObservers() {
        /* viewModel.maxAmplitudeDebug.observe(this) {
             binding.tvMessageTest.apply {
                 visible()
                 text = "Amplitude: $it\nAMPLITUDE_MAX - ${viewModel.AMPLITUDE_MAX}"
             }
         }*/

        viewModel.audioSessionId.observe(this) {
            it.getContent()?.let {
                if (it != -1) {
                    setUpVisualizer(it)

                }
            }
        }


        viewModel.audioAiState.observe(this) {
            when (it) {
                AudioAiState.DEFAULT -> {
                    dotAnimationJob?.cancel()
                    binding.talkingView.gone()
                    binding.tvMessage.text = ""
                    micStateOff()
                }

                AudioAiState.SPEAK_NOW -> {
                    dotAnimationJob?.cancel()
                    adjustVideoSize(0.6f)
                    binding.talkingView.gone()
                    if (viewModel.planType !=PlanType.NONE && viewModel.isFirstLoad) {
                        binding.tvAskLuna.visible()
                        binding.tvMessage.text =
                            viewModel.planQusetion ?: getString(R.string.text_speak_now)
                    } else {
                        binding.tvAskLuna.gone()
                        binding.tvMessage.text = getString(R.string.text_speak_now)
                    }
                    micStateOn()
                }

                AudioAiState.LISTENING -> {
                    viewModel.isFirstLoad = false
                    binding.talkingView.gone()
                    binding.tvAskLuna.gone()
                    startDotAnimation(getString(R.string.text_listening))
                    micStateShowStop()
                }

                AudioAiState.GENERATING -> {
                    viewModel.isFirstLoad = false
                    adjustVideoSize(0.9f)
                    binding.talkingView.gone()
                    binding.tvAskLuna.gone()

                    startDotAnimation(getString(R.string.text_analysing))

                    micStateGenerating()
                    viewModel.stopRecording(true)
                }

                AudioAiState.AI_TALKING -> {
                    viewModel.isFirstLoad = false
                    dotAnimationJob?.cancel()
                    binding.tvAskLuna.gone()
                    adjustVideoSize(0.6f)
                    binding.tvMessage.text = ""
                    showTalkingWidget()
                    micStateOff()
                }

                AudioAiState.AI_TALKING_STOP -> {
                    viewModel.isFirstLoad = false
                    dotAnimationJob?.cancel()
                    binding.tvAskLuna.gone()
                    adjustVideoSize(0.6f)
                    mVisualizer?.release()
                    binding.talkingView.gone()
                    viewModel.startNewRecording(false)
                }
            }
        }


        /* viewModel.textReceived.observe(this) {

             it.getContent()?.let {
                 binding.tvMessageTest.text = viewModel.stringBuilder.toString()
             }
         }*/

        viewModel.onCredentialsReceived.observe(this) {
            it.getContent()?.let {
                //binding.tvMessage.text = ""
                checkMicrophonePermission {
                    showSettingUpAnimation()
                }
            }
        }

        viewModel.videoPlayState.observe(this) {
            if (it) {
                if (binding.videoView.isPlaying.not()) {
                    binding.videoView.start()
                }
            } else {
                binding.videoView.pause()
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

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    private fun startDotAnimation(text: String) {
        if (viewModel.lastAnimatingText.equals(text, true)) {
            return
        }
        dotAnimationJob?.cancel()
        viewModel.lastAnimatingText = text
        dotAnimationJob = CoroutineScope(Dispatchers.Main).launch {
            var dotCount = 0
            while (true) {
                nullableBinding?.tvMessage?.text = text + ".".repeat(dotCount)
                dotCount = (dotCount + 1) % 4
                delay(500) // Update every 500ms
            }
        }
    }

    private fun setUpVisualizer(audioSessionId: Int) {
        if (mVisualizer != null) {
            mVisualizer?.release()
        }

        mVisualizer = Visualizer(audioSessionId)
        mVisualizer?.setCaptureSize(Visualizer.getCaptureSizeRange()[1])
        mVisualizer?.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(
                visualizer: Visualizer, bytes: ByteArray,
                samplingRate: Int
            ) {
                /*  val amplitude: Float = calculateAmplitude(bytes)

                  LOGS.d("sdkflhsldkfhsdkl $amplitude")*/
                //audioVisualizerView.setAmplitude(amplitude)

            }

            override fun onFftDataCapture(
                visualizer: Visualizer, bytes: ByteArray,
                samplingRate: Int
            ) {
                val amplitude = calculateAmplitudeFromFft(bytes)

                val multiplied = (amplitude * 1.5f)

                nullableBinding?.talkingView?.updateAmplitude(multiplied.roundToInt())

            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true)

        mVisualizer?.setEnabled(true)
    }


    val MAX_POSSIBLE_MAGNITUDE: Float =
        sqrt((128 * 128 + 128 * 128).toDouble()).toFloat() // ~181.02
    private val smoothingFactor = 0.2f
    private var smoothedAmplitude = 0f

    private fun calculateAmplitudeFromFft(fft: ByteArray): Float {
        var maxMagnitude = 0f
        for (i in 1 until fft.size / 2) {
            val real = fft[i * 2].toFloat()
            val imaginary = fft[i * 2 + 1].toFloat()
            val magnitude =
                sqrt((real * real + imaginary * imaginary).toDouble()).toFloat()
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
            }
        }
        val normalizedAmplitude = maxMagnitude / MAX_POSSIBLE_MAGNITUDE
        val scaledAmplitude =
            min((normalizedAmplitude * 100).toDouble(), 100.0).toFloat()
        smoothedAmplitude =
            (scaledAmplitude * smoothingFactor) + (smoothedAmplitude * (1 - smoothingFactor))
        return if (smoothedAmplitude < 1.0f) 0f else smoothedAmplitude
    }

    private fun showTalkingWidget() {
        binding.talkingView.visible()
    }


    private fun showSettingUpAnimation() {
        binding.tvMessage.text = getString(R.string.text_setting_up)

        val alphaAnimation: ObjectAnimator =
            ObjectAnimator.ofFloat(binding.imageGradientLayer, View.ALPHA, 1f, 0f)
        alphaAnimation.apply {
            duration = 1500
            start()
        }

        alphaAnimation.doOnEnd {
            nullableBinding?.imageGradientLayer?.gone()
            nullableBinding?.ivMic?.visible()
            viewModel.startNewRecording(true)
        }
    }
}