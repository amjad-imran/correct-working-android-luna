package com.noisefit.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentProfileBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

enum class UserType(val type: String){
    Admin("admin"),
    Influencer("influencer"),
    User("user"),
    None("none")
}
@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private val viewModel: ProfileViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.getUserData()
        //viewModel.getUserStats()
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.tvEdit.setOnClickListener {


            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.ACCOUNT_MYPROFILE_EDIT_CLICK)
            navigate(R.id.navigation_profile_edit)

            //navigate(ProfileFragmentDirections.actionProfileFragmentToProfileEditFragment())
        }

//        binding.tvLogout.setOnClickListener {
//
//            setFragmentResultListener(LOGOUT_KEY) { key, bundle ->
//                val isSelected = bundle.getBoolean("isSelected")
//                if (isSelected) {
//                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.ACCOUNT_MY_PROFILE_LOGOUT_CLICK)
//                    viewModel.logoutUser()
//                }
//            }
//
//            navigate(
//                ProfileFragmentDirections.actionProfileEditFragmentToLogoutBottomSheet()
//            )
//        }
        binding.tvDeleteAccount.setOnClickListener {
            setFragmentResultListener(DELETE_KEY) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.ACCOUNT_MY_PROFILE_DELETE_ACCOUNT_CLICK)
                    viewModel.deleteUser()
                }
            }

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


    private fun unpairDevice() {
        viewModel.localDataStore.getConnectedDevice()?.let {
            viewModel.connectionHandler.getConnectionActions(it)?.disconnect(it)
        }
        startActivity(OnBoardActivity.getStartIntent(requireActivity()))
        requireActivity().finish()
    }
}