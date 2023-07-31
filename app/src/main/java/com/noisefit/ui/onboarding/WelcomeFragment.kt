package com.noisefit.ui.onboarding

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentWelcomeBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>(FragmentWelcomeBinding::inflate) {

    private val viewModel: WelcomeViewModel by activityViewModels()
    private var currentPosition: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.autoContinue = false

        setOnBoardVideo()
    }

    private fun setOnBoardVideo() {
        binding.videoOnboard.apply {
//            setVideoURI(
//                Uri.parse(
//                    "android.resource://" + requireContext().packageName + "/" +
//                            R.raw.video_onboard
//                )
//            )
//            setOnPreparedListener { mp -> mp.isLooping = true }
//            start()
        }
    }

    override fun onPause() {
        super.onPause()
        currentPosition = binding.videoOnboard.currentPosition
        binding.videoOnboard.pause()
    }

    override fun onResume() {
        super.onResume()
        binding.videoOnboard.seekTo(currentPosition)
        binding.videoOnboard.start()
    }

    override fun initListener() {

        binding.bContinue.setOnClickListener {
//            if (viewModel.images.isEmpty()) {
//                viewModel.autoContinue = true
//                uiController.onDisplayError(getString(R.string.text_something_went_wrong_retrying))
//                viewModel.getConfig()
//                return@setOnClickListener
//            }
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WELCOME_PAGE_CONTINUE_BUTTON_CLICK)
            navigate(WelcomeFragmentDirections.actionWelcomeFragmentToJoinNoisefitFragment())
        }

    }

    override fun subscribeObservers() {

        viewModel.config.observe(this) {
            if (viewModel.autoContinue) {
                navigate(WelcomeFragmentDirections.actionWelcomeFragmentToJoinNoisefitFragment())
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }


        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }
        viewModel.killApp.observe(this) {
            it.getContent()?.let {
                activity?.finish()
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }
}

