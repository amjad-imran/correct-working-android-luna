package com.noisefit.ui.onboarding.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentEmailOtpBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.onboarding.onboardProfile.ProfileSetupActivity
import com.noisefit.ui.onboarding.pairing.DeviceSetupActivity
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailOtpFragment :
    BaseFragment<FragmentEmailOtpBinding>(FragmentEmailOtpBinding::inflate) {

    private val viewModel: AuthViewModel by activityViewModels()
    private val args: EmailOtpFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.useOtp = true
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.LAND_ON_ENTER_OTP_PAGE_VISIT)
        if (args.mode == EmailMode.VERIFY) {
            binding.btnUsePassword.gone()
        } else {
            if (!args.usePassword) {
                binding.btnUsePassword.gone()
            }
        }

        if (viewModel.useOtp) {
            viewModel.startOtpResendTimer()
            binding.tvSubHeading.text = viewModel.enteredValue?.let { email ->
                getString(R.string.text_email_otp, email)
            }
                ?: getString(R.string.text_passwords_can_be_tricky_just_look_for_the_4_digit_otp_we_just_sent_and_kickstart_your_app_journey)
        }
    }

    override fun initListener() {

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnUsePassword.setOnClickListener {
            if (viewModel.useOtp) {
                viewModel.useOtp = false
                binding.otpContainer.gone()
                binding.inputPassword.visible()
                binding.btnUsePassword.text = getString(R.string.text_use_otp)
                binding.tvNotReceiveOtp.gone()
                binding.btnResendOtp.gone()
                binding.tvNotReceiveOtp.text = getString(R.string.text_remember_pass)
                binding.btnResendOtp.text = getString(R.string.text_forgot_pass)
                binding.textView.text = getString(R.string.text_password_please)
                binding.etPassword.setText("")
                binding.tvSubHeading.invisible()
            } else {
                viewModel.useOtp = true
                binding.otpContainer.visible()
                binding.inputPassword.gone()
                binding.tvNotReceiveOtp.visible()
                binding.btnResendOtp.visible()
                binding.btnUsePassword.text = getString(R.string.text_use_password)
                binding.tvNotReceiveOtp.text = getString(R.string.text_did_not_receive_otp)
                binding.btnResendOtp.text = getString(R.string.text_resend_otp)
                binding.textView.text = getString(R.string.text_otp_please)
                binding.tvSubHeading.visible()
                checkOtp()
            }
        }

        binding.bContinue.setOnClickListener {

            if (viewModel.useOtp) {
                viewModel.verifyOtp(
                    viewModel.enteredValue ?: "",
                    AuthMode.email,
                    viewModel.enteredOtp.value ?: ""
                )
            } else {
                viewModel.emailLoginPassword(
                    viewModel.enteredValue ?: "",
                    binding.etPassword.text.toString()
                )
            }

            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTINUE_OTP_CLICK)
        }

        binding.etOtp.afterTextChanged {
            checkOtp()
        }

        binding.etPassword.afterTextChanged {
            viewModel.setEnteredPassword(binding.etPassword.text.toString())
        }

        binding.btnResendOtp.setOnClickListener {

            if (viewModel.useOtp) {
                viewModel.startOtpResendTimer()

                binding.tvNotReceiveOtp.gone()
                binding.btnResendOtp.gone()
                binding.etOtp.text?.clear()

                viewModel.sendOtp(viewModel.enteredValue ?: "", AuthMode.email)
            }

        }


    }

    private fun checkOtp() {
        if (viewModel.useOtp) {
            val otp = "${binding.etOtp.text}"
            viewModel.setEnteredOtp(otp)
        }
    }


    override fun subscribeObservers() {

        viewModel.timerRunning.observe(viewLifecycleOwner) {
            if (!it) {
                binding.tvNotReceiveOtp.visible()
                binding.btnResendOtp.visible()
            }
        }

        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context?.showShortToast(message)
                if (!message.lowercase().equals("Otp Verified ".lowercase(), true))
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.OTP_VERIFICATION_FAILED)
                else
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.REGISTRATION_OTP_VERIFIED_SUCCESSFULLY)
            }
        }

        viewModel.enteredOtp.observe(viewLifecycleOwner) {
            if (it.isValidOTP()) {
                binding.bContinue.enable()
            } else {
                binding.bContinue.disable()
            }
        }

        viewModel.enteredPass.observe(viewLifecycleOwner) {
            if (it.length >= 6) {
                binding.bContinue.enable()
            } else {
                binding.bContinue.disable()
            }
        }

        viewModel.authSuccess.observe(this) {
            it.getContent()?.let { value ->
                if (value) {
                    if (viewModel.isDevicePaired()) {
                        if (viewModel.isProfileSetupComplete()) {
                            startActivity(
                                DeviceSetupActivity.getStartIntent(
                                    requireContext(),
                                    setupDevice = true
                                )
                            )
                            activity?.finish()
                        } else {
                            startActivity(ProfileSetupActivity.getStartIntent(requireContext()))
                            activity?.finish()
                        }
                    } else {
                        startActivity(PairDeviceActivity.getStartIntent(requireContext()))
                    }
                    activity?.finish()
                }
            }
        }

        viewModel.verifyMobile.observe(this) {
            it.getContent()?.let { value ->
                if (value) {
                    navigate(R.id.otpNumberFragment)
                }
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) binding.progressBar.root.visible() else binding.progressBar.root.gone()
        }

    }
}

enum class EmailMode {
    VERIFY, LOGIN
}