package com.oreo.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMyProfileOreoBinding
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit.ui.profile.DELETE_KEY
import com.noisefit.ui.profile.LOGOUT_KEY
import com.noisefit.ui.profile.ProfileViewModel
import com.noisefit.ui.profile.myprofile.MyProfileFragmentDirections
import com.noisefit.ui.settings.helpAndSupport.HelpAndSupportType
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConstants
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

    }


    override fun initListener() {
        binding.llMyGoals.setOnClickListener {
            navigate(R.id.myGoalFragment)
        }
        binding.rowAbout.setOnClickListener {
            navigate(R.id.aboutFragment)
        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
//        binding.rowHelp.setOnClickListener {
//            navigate(R.id.helpAndSupportFragment, Bundle().apply {
//                this.putSerializable("highlightTopic", HelpAndSupportType.NONE)
//            })
//        }

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
            navigate(R.id.rateUsOreo)
        }

//        binding.imvProfile.setOnClickListener {
//
//            goToProfile()
//        }

        binding.llMyProfile.setOnClickListener {
            goToProfile()
        }
        binding.tvName.setOnClickListener {

            goToProfile()
        }

        binding.rowHelp.setOnClickListener {

//            navigate(R.id.helpAndSupportFragment, Bundle().apply {
//                this.putSerializable("highlightTopic", HelpAndSupportType.NONE)
//            })
            navigate(R.id.oreoHealthAndSupportFragment)
        }

        binding.tvLogout.setOnClickListener {

            setFragmentResultListener(LOGOUT_KEY) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.logoutUser()
                }
            }
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