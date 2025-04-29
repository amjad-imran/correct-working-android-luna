package com.noisefit.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentProfileBinding
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private val viewModel: ProfileViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.getUserData()
        //viewModel.getUserStats()

        if (viewModel.numberAvailable.value == true) {
            binding.include44.root.visible()
        } else {
            binding.include44.root.gone()
        }


        val isInDemoMode = viewModel.localDataStore.isInDemoMode()
        if(isInDemoMode){
            binding.tvDeleteAccount.gone()
        }
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.tvEdit.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_ham_clicked,
                HashMap<String, Any>().apply {
                    this["property"] = "your_profile"
                    this["property_description"] = "edit"
                })

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_profile_edit_click)
            navigate(R.id.navigation_profile_edit)
        }

        binding.tvDeleteAccount.setOnClickListener {
            setFragmentResultListener(DELETE_KEY) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_myprofile_delete_allow_click)
                    viewModel.deleteUser()
                } else {
                    viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_myprofile_delete_cancel_click)
                }
            }
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_myprofile_page_delete_account_click)
            navigate(R.id.deleteAccountBottomSheet)
        }

        binding.ivUserImage.setOnClickListener {
            viewModel.getUser().value?.imageUrl?.let { imageUrl ->


                val imageViewPair =
                    androidx.core.util.Pair.create(binding.ivUserImage as View, "profilePic")

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    imageViewPair
                )


                startActivity(Intent(requireContext(), ProfilePicActivity::class.java).apply {
                    this.putExtra("imageUrl", imageUrl)
                }, options.toBundle())
            }
        }

    }

    override fun subscribeObservers() {
        viewModel.logoutSuccess().observe(viewLifecycleOwner) {
            if (it) {
                startActivity(OnBoardActivity.getStartIntent(requireContext(), true).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })

                //unpairDevice()
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        /*viewModel.getStats().observe(this) {
            binding.tvBuddiesValue.text = "${it.total_buddy}"
            logEvent(AppEvents.MYBUDDIES_COUNT, it.trophy_count ?: 0)
            binding.tvBadgeValue.text = "${it.trophy_count}"
        }*/
    }

}