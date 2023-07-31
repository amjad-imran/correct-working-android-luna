package com.noisefit.ui.onboarding.onboardProfile

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.noisefit.MainActivity
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetupProfileBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.onboarding.pairing.DeviceSetupActivity
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupProfileFragment :
    BaseFragment<FragmentSetupProfileBinding>(FragmentSetupProfileBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager
    private var currentPosition: Int = 0

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.LAND_ON_SET_UP_PROFILE_PAGE_VISIT)
//        if (localDataStore.getLocalUserData() != null) {
//            navigate(
//                SetupProfileFragmentDirections.actionSetupProfileFragmentToOnBoardNameFragment(
//                    ""
//                )
//            )
//        } else {
//            setOnBoardVideo()
//        }
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

    private fun setOnBoardVideo() {
//        binding.videoOnboard.apply {
//            setVideoURI(
//                Uri.parse(
//                    "android.resource://" + requireContext().packageName + "/" +
//                            R.raw.video_profile
//                )
//            )
//            setOnPreparedListener { mp -> mp.isLooping = true }
//            start()
//        }
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            goToHomeActivity()
        }
        binding.btnContinue.setOnClickListener {

            sessionManager.logInsiderAppEvent(InsiderAppEvents.PROFILE_DETAILS_CONTINUE_CLICK)
//            navigate(
//                SetupProfileFragmentDirections.actionSetupProfileFragmentToOnBoardNameFragment(
//                    "yes"
//                )
//            )
//            navigate(R.id.onBoardNameFragment)
        }
        binding.btnSkip.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.PROFILE_DETAILS_SKIPPED_CLICK)
            goToHomeActivity()


        }
    }

    private fun goToHomeActivity() {
        if (localDataStore.getConnectedDevice() == null) {
            startActivity(MainActivity.getStartIntent(requireContext()))
            activity?.finish()
        } else {
            startActivity(DeviceSetupActivity.getStartIntent(requireContext()))
            activity?.finish()
        }
    }

    override fun subscribeObservers() {

    }
}


