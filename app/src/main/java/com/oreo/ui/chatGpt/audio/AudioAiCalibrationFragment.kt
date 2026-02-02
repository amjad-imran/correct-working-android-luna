package com.oreo.ui.chatGpt.audio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAudioAiCalibrationBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AudioAiCalibrationFragment :
    BaseFragment<FragmentAudioAiCalibrationBinding>(FragmentAudioAiCalibrationBinding::inflate) {

    private val viewModel: AudioCalibrationViewModel by viewModels()
    val args: AudioAiCalibrationFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initSpeechRecognizer(requireContext())
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvRecord.setOnClickListener {
            if (viewModel.isRecording) return@setOnClickListener

            if(binding.tvRecord.text.equals(getString(R.string.text_next))){
                navigate(AudioAiCalibrationFragmentDirections.actionAudioAiCalibrationFragmentToAudioAiFragment(
                    args.text
                ).apply {
                    planType = args.planType
                })
            }else{
                binding.tvRecord.text = getString(R.string.record)
                checkMicrophonePermission {
                    binding.tvRecord.gone()
                    viewModel.startListening()
                    binding.groupListening.visible()
                }
            }
        }
    }

    override fun subscribeObservers() {
        viewModel.speechText.observe(this) {
            it?.getContent()?.let { data ->
                if (viewModel.isHeyLunaSpoken(data)) {
                    binding.groupListening.gone()
                    viewModel.stopListening()
                    viewModel.userAttemptsCount++
                    viewModel.completionState.postValue(viewModel.userAttemptsCount)
                    if (viewModel.userAttemptsCount >= 3) {
                        stateAllSet()
                    }
                }else{
                    showSpeechError()
                }
            } ?: showSpeechError()
        }

        viewModel.completionState.observe(this) {
            if (it == null) {
                binding.lytBottomChecks.root.gone()
                return@observe
            }
            showProgressState(it)
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }

    private fun showSpeechError(){
        binding.groupListening.gone()
        viewModel.stopListening()
        binding.tvRecord.apply {
            text = getString(R.string.text_try_again)
            visible()
        }
        binding.lytBottomChecks.root.visible()
        when(viewModel.userAttemptsCount){
            0 -> {
                binding.lytBottomChecks.ivCheck1.apply {
                    setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_failed))
                    visible()
                }
            }
            1 -> {
                binding.lytBottomChecks.ivCheck2.apply {
                    setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_failed))
                    visible()
                }
            }
            2 -> {
                binding.lytBottomChecks.ivCheck3.apply {
                    setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_failed))
                    visible()
                }
            }
        }
    }

    /**
     * 1,2,3
     */
    private fun showProgressState(position: Int) {
        binding.lytBottomChecks.root.visible()
        binding.tvRecord.visible()
        when (position) {
            1 -> {
                binding.apply {
                    lytBottomChecks.apply {
                        ivCheck1.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_check_ai
                            )
                        )
                        ivCheck1.visible()
                        ivCheck2.invisible()
                        ivCheck3.invisible()
                    }
                    tvHeyLuna1.alpha = 1f
                    tvSayHiLuna.text = getString(R.string.say_hey_luna_again)
                }
            }

            2 -> {
                binding.apply {
                    lytBottomChecks.apply {
                        ivCheck2.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_check_ai
                            )
                        )
                        ivCheck1.visible()
                        ivCheck2.visible()
                        ivCheck3.invisible()
                    }
                    tvHeyLuna2.alpha = 1f
                    tvSayHiLuna.text = getString(R.string.great_one_last_time)
                }
            }

            3 -> {
                binding.apply {
                    lytBottomChecks.apply {
                        ivCheck1.visible()
                        ivCheck2.visible()
                        ivCheck3.visible()
                    }
                    tvHeyLuna3.alpha = 1f
                }
            }
        }
    }

    private fun stateAllSet() {
        viewModel.completionState.postValue(null)
        binding.apply {
            tvSayHiLuna.gone()
            groupListening.gone()
            tvHeyLuna1.gone()
            tvHeyLuna2.gone()
            tvHeyLuna3.gone()
            tvDone.visible()
            tvRecord.text = getString(R.string.text_next)
            tvRecord.visible()
        }
    }

    private fun checkMicrophonePermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            binding.tvRecord.gone()
            micPermissionResult.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private val micPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            viewModel.startListening()
            binding.groupListening.visible()
        } else {
            context.showShortToast("Permission Required")
            navigateUpSafe()
        }
    }
}