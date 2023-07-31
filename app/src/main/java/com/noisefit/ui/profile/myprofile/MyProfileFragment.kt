package com.noisefit.ui.profile.myprofile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.model.trophies.TrophyBadge
import com.noisefit.databinding.FragmentMyProfileBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit.ui.onboarding.onboardProfile.GuestProfileSetupActivity
import com.noisefit.ui.profile.DELETE_KEY
import com.noisefit.ui.profile.LOGOUT_KEY
import com.noisefit.ui.profile.ProfileViewModel
import com.noisefit.ui.settings.helpAndSupport.HelpAndSupportType
import com.noisefit.ui.trophies.TrophiesType
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyProfileFragment :
    BaseFragment<FragmentMyProfileBinding>(FragmentMyProfileBinding::inflate) {

    private val viewModel: ProfileViewModel by viewModels()

    private val recentTrophiesAdapter: RecentTrophiesAdapter by lazy {
        RecentTrophiesAdapter(object : RecentTrophyAction {
            override fun onTrophyClicked(trophyBadge: TrophyBadge) {
                val defaultSelected = if (trophyBadge.activityType.equals("distance", true)) {
                    TrophiesType.DISTANCE.name
                } else TrophiesType.STEPS.name
                navigate(
                    MyProfileFragmentDirections.actionMyProfileFragmentToTrophiesFragment().apply {
                        this.userMobile = null
                        selectedDefault = defaultSelected
                    }
                )
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setRecycler()
    }

    private fun setRecycler() {
        binding.rvBadges.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvBadges.adapter = recentTrophiesAdapter
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
        //viewModel.getRecentTrophies()


        /*if (viewModel.canSubmitFeedback()) {*/
        binding.rowFeedBack.visible()
        /*} else {
            binding.rowFeedBack.gone()
        }*/
    }

    private fun goToProfile() {
        if (viewModel.isProfileSetupPending()) {
            return
        }
        logInsiderAppEvent(InsiderAppEvents.ACCOUNT_MYPROFILE_CLICK)
        navigate(R.id.profileFragment)
    }

    override fun initListener() {

        binding.rowRewards.setOnClickListener {
            navigate(R.id.coinFragment)
        }
        binding.rowStepStreak.setOnClickListener {
            navigate(R.id.stepStreakFragment)
        }

        binding.tvCustomerSupport.setOnClickListener {
            startActivity(
                WebViewActivity.getStartIntent(
                    requireActivity(),
                    getString(com.noisefit_commans.R.string.text_customer_support),
                    AppConstants.URL_CONTACT_SUPPORT
                )
            )
        }

        if (viewModel.isProfileSetupPending()) {
            binding.lytCreateProfile.layoutContainer.visible()
            binding.lytCreateProfile.btnPairDevice.text =
                getString(R.string.text_create_your_profile)
            binding.lytCreateProfile.tvMsg.text =
                getString(R.string.text_set_up_your_profile_msg)
//            binding.ivSettingsArrow.gone()
        } else {
            binding.lytCreateProfile.layoutContainer.gone()
//            binding.ivSettingsArrow.visible()
        }
        binding.lytCreateProfile.btnPairDevice.setOnClickListener {
            if (binding.lytCreateProfile.btnPairDevice.text == getString(R.string.text_create_your_profile)) {
                startActivity(GuestProfileSetupActivity.getStartIntent(requireContext()))
            }

        }
//        binding.imvProfile.setOnClickListener {
//
//            goToProfile()
//        }
        binding.tvName.setOnClickListener {

            goToProfile()
        }
        binding.rowBadge.setOnClickListener {
            if (viewModel.isProfileSetupPending()) {
                context.showShortToast(
                    String.format(
                        getString(R.string.text_set_up_your_profile_other),
                        "Badges"
                    )
                )

            } else {
                logInsiderAppEvent(InsiderAppEvents.ACCOUNT_BADGES_CLICK)
                navigate(
                    MyProfileFragmentDirections.actionMyProfileFragmentToTrophiesFragment().apply {
                        userMobile = null
                        selectedDefault = TrophiesType.STEPS.name
                    }
                )
            }

        }
        binding.llMyProfile.setOnClickListener {

            goToProfile()
        }

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.rowMyOrders.setOnClickListener {
            logInsiderAppEvent(InsiderAppEvents.ACCOUNT_ORDERS_CLICK)
            viewModel.getOrderToken()
        }
        binding.llMyGoals.setOnClickListener {
//            if (viewModel.localDataStore.getConnectedDevice() == null) {
//                uiController.onDisplayError(getString(R.string.text_no_device_paired))
//                return@setOnClickListener
//            }
            logInsiderAppEvent(InsiderAppEvents.ACCOUNT_GOALS_CLICK)
            navigate(R.id.myGoalFragment)
        }
        binding.settingsRow.setOnClickListener {
            navigate(R.id.settingFragment)
        }
        binding.rowFeedBack.setOnClickListener {
            logInsiderAppEvent(InsiderAppEvents.FEEDBACK_CLICK)
            navigate(R.id.navigation_activity_feedback)
        }
        binding.rowAbout.setOnClickListener {
            logInsiderAppEvent(InsiderAppEvents.ACCOUNT_ABOUT_CLICK)
            navigate(R.id.aboutFragment)
        }
        binding.rowHelp.setOnClickListener {
            logInsiderAppEvent(InsiderAppEvents.HELP_SUPPORT_CLICK)
            navigate(
                MyProfileFragmentDirections.actionMyProfileFragmentToHelpAndSupportFragment()
                    .apply {
                        this.highlightTopic = HelpAndSupportType.NONE
                    }
            )
        }

        binding.tvLogout.setOnClickListener {

            setFragmentResultListener(LOGOUT_KEY) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.logoutUser()
                }
            }
            navigate(
                MyProfileFragmentDirections.actionMyProfileFragmentToLogoutBottomSheet()
            )
        }

        binding.tvDeleteAccount.setOnClickListener {
            setFragmentResultListener(DELETE_KEY) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.deleteUser()
                }
            }
            navigate(MyProfileFragmentDirections.actionMyProfileFragmentToAccountDeleteBottomSheet())
        }
        binding.rowFitness.setOnClickListener {
            logInsiderAppEvent(InsiderAppEvents.USERPROFILE_FITNESS_HEALTH_CLICK)
            navigate(R.id.fitnessHealthFragment)
        }
    }


    private fun logInsiderAppEvent(eventName: String) {
        viewModel.sessionManager.logInsiderAppEvent(
            eventName
        )
    }

    override fun subscribeObservers() {

        viewModel.logoutSuccess().observe(viewLifecycleOwner) {
            if (it) {
                startActivity(OnBoardActivity.getStartIntent(requireContext(), true).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }


        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.orderUrl().observe(viewLifecycleOwner) {
            it.getContent()?.let { url ->

                activity?.let { act ->
                    startActivity(
                        WebViewActivity.getStartIntent(
                            act,
                            getString(R.string.text_my_orders),
                            url
                        )
                    )
                }

            }

        }

        //Removed
        /* viewModel.trophies.observe(viewLifecycleOwner) {
             recentTrophiesAdapter.setDataSet(it)
             recentTrophiesAdapter.setUnit(viewModel.getUnitValueForRecentTrophy())
             recentTrophiesAdapter.getDeviceWidth(viewModel.getDeviceWidth(requireActivity()))
             if (it.isEmpty()) {
                 binding.rvBadges.gone()
             } else {
                 binding.rvBadges.visible()
             }
         }*/
    }

}