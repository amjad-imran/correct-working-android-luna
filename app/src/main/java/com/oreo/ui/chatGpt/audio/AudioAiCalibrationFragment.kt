package com.oreo.ui.chatGpt.audio

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
                checkMicrophonePermission {
                    binding.tvRecord.gone()
                    viewModel.startNewRecording()
                }
            }
        }
    }

    override fun subscribeObservers() {
        viewModel.videoPlayState.observe(this) {
            if (it) {
                if (binding.videoView.isPlaying.not()) {
                    binding.videoView.start()
                }
            } else {
                binding.videoView.pause()
            }
        }

        viewModel.currentTimer.observe(this) {
            it.getContent()?.let { percent ->
                if (percent == 100) {
                    binding.groupListening.gone()
                    val dataSize = viewModel.maxAmpList.size
                    viewModel.completionState.postValue(dataSize)
                    if (dataSize >= 3) {
                        viewModel.sameMaxAmp()
                        stateAllSet()
                    }
                } else {
                    binding.groupListening.visible()
                }
            }
        }

        viewModel.completionState.observe(this) {
            if (it == null) {
                binding.lytBottomChecks.root.gone()
                return@observe
            }
            showProgressState(it)
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
            binding.tvRecord.gone()
            callback.invoke()
        } else {
            micPermissionResult.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private val micPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            viewModel.startNewRecording()
        } else {
            context.showShortToast("Permission Required")
            navigateUpSafe()
        }
    }
}