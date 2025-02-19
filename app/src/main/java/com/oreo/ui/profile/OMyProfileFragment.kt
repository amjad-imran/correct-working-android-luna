package com.oreo.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.freshchat.consumer.sdk.FaqOptions
import com.freshchat.consumer.sdk.Freshchat
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMyProfileOreoBinding
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit.ui.profile.LOGOUT_KEY
import com.noisefit.ui.profile.ProfileViewModel
import com.noisefit.ui.profile.ReferralRunningState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OMyProfileFragment :
    BaseFragment<FragmentMyProfileOreoBinding>(FragmentMyProfileOreoBinding::inflate) {

    private val viewModel: ProfileViewModel by viewModels()

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
        /*binding.lytReferralNo.tvMyReferrals.setOnClickListener {
            navigate(R.id.myReferralsFragment)
        }*/

        binding.lytUpdateToViewReferral.tvUpdateNow.setOnClickListener {
            ShareUtil.openPlayStore(requireContext(), "com.noisefit.luna")
        }

        binding.rowSelectLanguage.setOnClickListener {
            navigate(R.id.languageFragment, bundleOf("hideContinue" to true))
        }

        binding.lytReferralAvailable.ivCross.setOnClickListener {
            viewModel.onReferralCloseClicked()
        }

        binding.rowReferral.setOnClickListener {
            if (viewModel.referralRunningState.value == ReferralRunningState.ReferralAndCampaignState ||
                viewModel.referralRunningState.value is ReferralRunningState.CampaignRunningState ||
                viewModel.referralRunningState.value is ReferralRunningState.PrizeOnlyState
            ) {
                viewModel.referralResponse?.let {
                    navigate(R.id.referralFragment, bundleOf("referralInfo" to it))
                }
            } else if (viewModel.referralRunningState.value == ReferralRunningState.ReferralOnlyState) {
                navigate(R.id.myReferralsFragment)
            }
        }

        binding.lytReferralAvailable.root.setOnClickListener {
            viewModel.referralResponse?.let {
                navigate(R.id.referralFragment, bundleOf("referralInfo" to it))
            }
        }

        binding.rowCycleTracker.setOnClickListener {
            viewModel.getCycleTrackerInfo()
//            navigate(R.id.cycleTrackerStreakFragment)
        }
        binding.rowSupport.setOnClickListener {
            /*Freshchat.showFAQs(requireContext(), FaqOptions().apply {
                showFaqCategoriesAsGrid(false)
            })*/

            Freshchat.showConversations(requireContext())
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
        viewModel.referralRunningState.observe(this) {
            when (it) {
                is ReferralRunningState.CampaignRunningState -> {
                    binding.lytUpdateToViewReferral.root.gone()
                    binding.lytReferralAvailable.root.visible()
                    binding.lytReferralAvailable.apply {
                        tvReferralMessage.text = it.prizeTitle
                        this.ivMain.loadImageWithCache(this.root.context, it.prizeImageUrl)
                    }
                    binding.rowReferral.visible()
                }

                ReferralRunningState.ReferralOnlyState, ReferralRunningState.ReferralAndCampaignState,
                ReferralRunningState.PrizeOnlyState -> {
                    binding.lytUpdateToViewReferral.root.gone()
                    binding.lytReferralAvailable.root.gone()
                    binding.rowReferral.visible()
                }
                ReferralRunningState.UpdateToViewReferral->{
                    binding.lytUpdateToViewReferral.root.visible()
                    binding.lytReferralAvailable.root.gone()
                    binding.rowReferral.gone()
                }

                ReferralRunningState.Default -> {
                    binding.lytUpdateToViewReferral.root.gone()
                    binding.lytReferralAvailable.root.gone()
                    binding.rowReferral.gone()
                }
            }
        }

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
                    uiController.logAppEvent(
                        MoEngageLunaAppEvents.cycle_track
                    )


                    navigate(R.id.cycleTrackerSettingsNewFragment)
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