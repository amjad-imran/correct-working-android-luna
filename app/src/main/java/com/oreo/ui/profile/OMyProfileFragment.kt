package com.oreo.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.freshchat.consumer.sdk.Freshchat
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMyProfileOreoBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit.ui.profile.LOGOUT_KEY
import com.noisefit.ui.profile.ProfileViewModel
import com.noisefit.ui.profile.ReferralRunningState
import com.noisefit.ui.web.WebViewActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.oreo.ui.chatGpt.PlanType
import com.noisefit.ui.profile.ProfileViewModel.DownloadMyDataBS.*
import com.oreo.ui.profile.downloadMyData.ProcessAndDownloadMyDataBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class OMyProfileFragment :
    BaseFragment<FragmentMyProfileOreoBinding>(FragmentMyProfileOreoBinding::inflate) {

    private val viewModel: ProfileViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

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


        if (user?.userInfo?.gender.equals("female", true).not()) {
            binding.rowCycleTracker.gone()
        } else {
            binding.rowCycleTracker.visible()
        }

        if (
            mainViewModel.registerDate > 6
        ) {
            binding.llCustomHomeScreen.visible()
        } else {
            binding.llCustomHomeScreen.gone()
        }

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val cannyState = viewModel.ringDataStore.getCannyState()
            withContext(Dispatchers.Main) {
                binding.rowCannyFeedback.setVisibilityByCondition(cannyState)
            }
        }

    }


    override fun initListener() {
        /*binding.lytReferralNo.tvMyReferrals.setOnClickListener {
            navigate(R.id.myReferralsFragment)
        }*/

        binding.rowCannyFeedback.setOnClickListener {
            viewModel.getCannyFeedbackUrl()
        }

        binding.llLunaAiCalibration.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "Voice_calibration"
                }
            )
            navigate(
                R.id.audioAiCalibrationFragment,
                bundleOf("planType" to PlanType.NONE, "text" to null)
            )
        }

        binding.lytUpdateToViewReferral.tvUpdateNow.setOnClickListener {
            ShareUtil.openPlayStore(requireContext(), "com.noisefit.luna")
        }

        binding.rowSelectLanguage.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "app_language"
                }
            )
            navigate(R.id.languageFragment, bundleOf("hideContinue" to true))
        }

        binding.lytReferralAvailable.ivCross.setOnClickListener {
            viewModel.onReferralCloseClicked()
        }

        binding.rowReferral.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_ham_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "refer_and_earn"
                    this["source"] = "menu_option/promo_card"
                })

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
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "cycle_tracking"
                })
            viewModel.getCycleTrackerInfo()
//            navigate(R.id.cycleTrackerStreakFragment)
        }
        binding.rowSupport.setOnClickListener {
            /*Freshchat.showFAQs(requireContext(), FaqOptions().apply {
                showFaqCategoriesAsGrid(false)
            })*/

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "live_support"
                })

            //Freshchat.showConversations(requireContext())

            context?.let {
                ShareUtil.composeEmail(it,"support@lunazone.com","[${getString(R.string.text_app_support)}]")
            }

        }

        binding.rowSettings.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "settings"
                })
            navigate(R.id.settingsFragment)
        }

        binding.rowAbout.setUpdateAvailable(viewModel.localDataStore.isNewAppVersionAvailable())
        binding.rowAbout.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "about"
                })
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
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "Rate us"
                })
            navigate(R.id.rateUsOreo)
        }

//        binding.imvProfile.setOnClickListener {
//
//            goToProfile()
//        }

        binding.llMyProfile.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "your_profile"
                })

            goToProfile()
        }
        binding.tvName.setOnClickListener {

            goToProfile()
        }

        binding.rowLearn.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "learn_more"
                }
            )
            navigate(R.id.learnFragment)
        }

        binding.rowHelp.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["target"] = "FAQ"
                })
            navigate(R.id.oreoHelpAndSupportFragment)
        }

        binding.tvLogout.setOnClickListener {

            setFragmentResultListener(LOGOUT_KEY) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_myprofile_logout_allow_click)
                    viewModel.logoutUser()
                    viewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.user_menu_option_clicked,
                        HashMap<String, Any>().apply {
                            this["target"] = "logout"
                        })
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

        //
        binding.llCustomHomeScreen.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_menu_option_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "hamburger"
                    this["target"] = "Customize_homescreen"
                }
            )
            navigate(R.id.custom_homecreen)
        }

        binding.rowReportToDevs.setOnClickListener {
            viewModel.sessionManager.sendQueryAction(QueryAction.GetFirmwareLogs)

            setFragmentResultListener(REPORT_TO_DEVS_KEY) { _, bundle ->
                val titleReportToDev = bundle.getString("title")
                val descReportToDev = bundle.getString("desc")

                context?.let { ctx ->
                    val status = ApplicationUtils.startFeedbackSubmitWorker(
                        ctx,
                        titleReportToDev,
                        descReportToDev
                    )
                }

                viewModel.sendReportToDevFeedback(titleReportToDev, descReportToDev)
            }
            navigate(R.id.reportToDevelopersBottomSheet)
        }

        binding.rowDownloadMyData.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.pdf_click
            )
            if(viewModel.downloadMyDataList==null) viewModel.setDownloadMyDataList()
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue") ?: return@setFragmentResultListener
                viewModel.downloadMyDataSelectedItem = selectedValue

                val dayVal = when (selectedValue) {
                    getString(R.string.text_today) -> 1
                    getString(R.string.text_last_val_days, 3) -> 3
                    else -> 7
                }

                viewModel.getDownloadMyDataPDF(dayVal)
            }
            navigate(
                R.id.valueSelectorBottomSheet,
                bundleOf(
                    "selectedValue" to viewModel.downloadMyDataSelectedItem,
                    "selectionList" to viewModel.downloadMyDataList?.toTypedArray(),
                    "title" to getString(R.string.text_download_my_data),
                    "isTopLineVisible" to true
                )
            )
        }
        //

    }

    private fun goToProfile() {
        if (viewModel.isProfileSetupPending()) {
            return
        }
        navigate(R.id.profileFragmentOreo)
    }

    private fun showProcessSheet() {
        viewModel.processSheet = ProcessAndDownloadMyDataBottomSheet()
        viewModel.processSheet?.show(childFragmentManager, "DownloadBS")
    }

    private fun dismissProcessSheetIfVisible() {
        viewModel.processSheet?.dismiss()
        viewModel.processSheet = null
    }

    override fun subscribeObservers() {

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.bsState.collect { state ->
                when (state) {
                    PROCESSING -> {
                        showProcessSheet()
                    }

                    SUCCESS -> {
                        // Option A: if you have a single sheet that changes UI, just show SUCCESS there
//                            showSuccessSheet()
                    }

                    OPEN_PDF -> {
                        viewModel.fileUri.value?.let { uri ->
                            ShareUtil.shareFile(requireContext(), uri)
                        } ?: requireContext().showShortToast(getString(R.string.text_try_again))
                    }

                    ERROR -> {
                        dismissProcessSheetIfVisible()
                        requireContext().showShortToast(getString(R.string.text_something_went_wrong))
                    }

                    else -> Unit
                }
            }
        }

        viewModel.dataReportToDevUpdated.observe(this) {
            it.getContent()?.let {
                navigate(R.id.reportToDevelopersSuccessBottomSheet)
            }
        }

        viewModel.cannyFeedbackUrl.observe(this) {
            it.getContent()?.let {
                startActivity(
                    WebViewActivity.getStartIntent(
                        requireContext(),
                        getString(R.string.text_suggest_a_feature),
                        it
                    )
                )
            }
        }
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

                ReferralRunningState.UpdateToViewReferral -> {
                    binding.lytUpdateToViewReferral.root.visible()
                    binding.lytReferralAvailable.root.gone()
                    binding.rowReferral.gone()
                }

                ReferralRunningState.Default -> {
                    binding.lytUpdateToViewReferral.root.gone()
                    binding.lytReferralAvailable.root.gone()
                    binding.rowReferral.gone()
                }

                is ReferralRunningState.CampaignRunningState -> TODO()
                ReferralRunningState.Default -> TODO()
                ReferralRunningState.PrizeOnlyState -> TODO()
                ReferralRunningState.ReferralAndCampaignState -> TODO()
                ReferralRunningState.ReferralOnlyState -> TODO()
                ReferralRunningState.UpdateToViewReferral -> TODO()
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