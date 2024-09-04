package com.oreo.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMyProfileOreoBinding
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit.ui.profile.LOGOUT_KEY
import com.noisefit.ui.profile.ProfileViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.utils.share.ShareUtil.SUPPORT_URL
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OMyProfileFragment :
    BaseFragment<FragmentMyProfileOreoBinding>(FragmentMyProfileOreoBinding::inflate) {

    private val viewModel: ProfileViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onResume() {
        super.onResume()

        val user = viewModel.localDataStore.getUser()
        val title = "Hey, ${user?.firstName?.trim()?.ifEmpty { "Stranger" }}"
        binding.tvName.text = title
//        binding.imvProfile.loadCircleImage(
//            requireContext(),
//            user?.imageUrl ?: "",
//            R.drawable.ic_default_profile_image
//        )


        if (user?.userInfo?.gender.equals("male", true)) {
            binding.rowCycleTracker.gone()
        } else {
            binding.rowCycleTracker.visible()
        }

    }


    override fun initListener() {

        binding.rowCycleTracker.setOnClickListener {
            viewModel.getCycleTrackerInfo()
//            navigate(R.id.cycleTrackerStreakFragment)
        }
        binding.rowSupport.setOnClickListener {
            context?.let {
                ShareUtil.openExternalUrl(it, SUPPORT_URL)
            }
        }

        binding.rowSettings.setOnClickListener {
            navigate(R.id.settingsFragment)
        }

        binding.rowAbout.setUpdateAvailable(viewModel.localDataStore.isNewAppVersionAvailable())
        binding.rowAbout.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_about_click)
            navigate(R.id.aboutFragment)
        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

//        binding.tvCustomerSupport.setOnClickListener {
//            startActivity(
//                WebViewActivity.getStartIntent(
//                    requireActivity(),
//                    getString(com.noisefit_commans.R.string.text_customer_support),
//                    AppConstants.URL_CONTACT_SUPPORT
//                )
//            )
//        }
        binding.rowFeedBack.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_rate_us_click)
            navigate(R.id.rateUsOreo)
        }

//        binding.imvProfile.setOnClickListener {
//
//            goToProfile()
//        }

        binding.llMyProfile.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_your_profile_click)
            goToProfile()
        }
        binding.tvName.setOnClickListener {

            goToProfile()
        }

        binding.rowLearn.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_learnmore_click)
            navigate(R.id.learnFragment)
        }

        binding.rowHelp.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.luna_help_support_click,
                HashMap<String, Any>().apply {
                    this[MoEngageAppEventParams.operating_system] = "Android"
                    this[MoEngageAppEventParams.mobile_manufacturer] = viewModel.getDeviceName()
                })
            navigate(R.id.oreoHelpAndSupportFragment)
        }

        binding.tvLogout.setOnClickListener {

            setFragmentResultListener(LOGOUT_KEY) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_myprofile_logout_allow_click)
                    viewModel.logoutUser()
                } else {
                    viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_myprofile_logout_cancel_click)
                }
            }
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_hamburger_logout_click)
            navigate(
                R.id.logoutBottomSheet2
            )
        }

//        binding.tvDeleteAccount.setOnClickListener {
//            setFragmentResultListener(DELETE_KEY) { key, bundle ->
//                val isSelected = bundle.getBoolean("isSelected")
//                if (isSelected) {
//                    viewModel.deleteUser()
//                }
//            }
//            navigate(R.id.deleteAccountBottomSheet)
//        }

    }

    private fun goToProfile() {
        if (viewModel.isProfileSetupPending()) {
            return
        }
        navigate(R.id.profileFragmentOreo)
    }


    override fun subscribeObservers() {
        viewModel.cycleTrackInfo.observe(this) {
            it?.getContent()?.let {
                navigate(R.id.cycleTrackerSettingFragment, Bundle().apply {
                    this.putParcelable("data", it)
                })
            }
        }
        viewModel.showFemaleHealthSplash.observe(this) {
            it?.getContent()?.let {
                if (it) {
                    navigate(R.id.femaleHealthSplashFragment)
                }
            }
        }

        viewModel.logoutSuccess().observe(viewLifecycleOwner) {
            if (it) {
                startActivity(OnBoardActivity.getStartIntent(requireContext(), true).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
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

}