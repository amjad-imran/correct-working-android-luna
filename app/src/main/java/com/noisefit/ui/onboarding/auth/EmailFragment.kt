package com.noisefit.ui.onboarding.auth

import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.R
import com.noisefit.databinding.FragmentEmailBinding
import com.noisefit.ui.common.*
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.StringUtils.isValidEmail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailFragment :
    BaseFragment<FragmentEmailBinding>(FragmentEmailBinding::inflate) {

    private val viewModel: AuthViewModel by activityViewModels()
    private val args: EmailFragmentArgs by navArgs()
    private var mLastClickTime: Long = 0



    override fun initListener() {
        binding.bContinue.setOnClickListener {
            sendOtp()
        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.etEmailId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                if (mLastClickTime != 0L) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return@setOnEditorActionListener false
                    }
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                sendOtp()
                return@setOnEditorActionListener true
            }
            false
        }


        binding.etEmailId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isValidEmail()) {
                    nullableBinding?.bContinue?.enable()
                } else {
                    nullableBinding?.bContinue?.disable()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    override fun subscribeObservers() {

        viewModel.emailOtpGenerated.observe(viewLifecycleOwner) {
            it.getContent()?.let { value ->
                if (value) {
                    navigate(
                        EmailFragmentDirections.actionEmailFragmentToEmailOtpFragment(
                            args.mode,
                            viewModel.usePassword
                        )
                    )
                }
            }
        }

        viewModel.getLoading().observe(this){
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

    private fun sendOtp() {
        if (binding.etEmailId.text.toString().isValidEmail()) {
            viewModel.sendOtp(binding.etEmailId.text.toString(), AuthMode.email)
        } else {
            context.showShortToast(getString(R.string.text_enter_valid_email))
        }
    }
}