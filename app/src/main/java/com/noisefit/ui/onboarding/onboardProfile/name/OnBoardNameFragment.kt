package com.noisefit.ui.onboarding.onboardProfile.name

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnBoardNameBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.afterTextChanged
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit_commans.utils.InsiderAppEvents

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardNameFragment :
    BaseFragment<FragmentOnBoardNameBinding>(FragmentOnBoardNameBinding::inflate) {

    private val viewModel: SetupProfileViewModel by activityViewModels()
    private var isOnboardSetup = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        arguments?.let {
//            if (!OnBoardNameFragmentArgs.fromBundle(it).guest.isNullOrEmpty()) {
//                isOnboardSetup = true
//
//            }
//        }
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.ACCOUNT_SET_UP_STARTED)
        binding.lytOnBoardProgress.apply {
            pgBr.progress = 20
            tvCount.text = getString(R.string.text_1)
        }


        binding.tvName.setText(viewModel.getLocalUserFirstName())

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)


    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPress()
            }
        }

    fun onBackPress(){
//        if (!isOnboardSetup) {
//            requireActivity().finish()
//        } else {
//            navigateUpSafe()
//        }
        navigateUpSafe()
    }

    override fun initListener() {
        binding.tvName.afterTextChanged {
            viewModel.setUserName(it.trim())
        }
        binding.backBtn.setOnClickListener {
            onBackPress()
        }
        binding.btnContinue.setOnClickListener {
            goToNextScreen()
        }

        binding.tvName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                goToNextScreen()
                true
            }
            false
        }
    }

    private fun goToNextScreen() {
        if (viewModel.userName.value?.isNotEmpty() == true) {

            viewModel.saveUserInfoLocally()
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTINUE_ENTER_NAME_CLICK)
            navigate(R.id.onBoardDobFragment)
        }
    }

    override fun subscribeObservers() {

        viewModel.userName.observe(this) {
            if (it.isEmpty()) {
                binding.btnContinue.disable()
            } else {
                binding.btnContinue.enable()
            }
        }
    }
}
