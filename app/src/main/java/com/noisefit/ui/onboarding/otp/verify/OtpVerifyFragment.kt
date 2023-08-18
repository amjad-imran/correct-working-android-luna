package com.noisefit.ui.onboarding.otp.verify

import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOtpVerifyBinding
import com.noisefit.receiver.broadcastReceiver.OTPReceiveListener
import com.noisefit.receiver.broadcastReceiver.OtpBroadcastReceiver
import com.noisefit.ui.onboarding.auth.AuthViewModel
import com.noisefit.ui.onboarding.onboardProfile.ProfileSetupActivity
import com.noisefit.ui.onboarding.otp.OtpViewModel
import com.noisefit.ui.onboarding.pairing.DeviceSetupActivity
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpVerifyFragment :
    BaseFragment<FragmentOtpVerifyBinding>(FragmentOtpVerifyBinding::inflate) {

    private val viewModel: OtpViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private var otpBroadcastReceiver: OtpBroadcastReceiver? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.LAND_ON_ENTER_OTP_PAGE_VISIT)
        viewModel.user = authViewModel.user
        viewModel.email = authViewModel.email

        if (!viewModel.contactNumber.value.isNullOrEmpty()) {
            val mobileNumber = viewModel.contactNumber.value
            val text = "OTP has been send to ${mobileNumber} Enter OTP to continue."
            val spannableString = SpannableString(text)
            // It is used to set the span to the string
            val white80 = ForegroundColorSpan(Color.parseColor("#ccffffff"))
            val linkColor = ForegroundColorSpan(Color.parseColor("#82a8f3"))

            spannableString.setSpan(
                white80,
                1, "OTP has been send to ".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                linkColor,
                "OTP has been send to ".length,
                "OTP has been send to $mobileNumber".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                white80,
                "OTP has been send to $mobileNumber".length,
                "OTP has been send to $mobileNumber Enter OTP to continue.".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.tvSubHeading.text = spannableString
        } else {
            binding.tvSubHeading.text =
                getString(R.string.text_passwords_can_be_tricky_just_look_for_the_4_digit_otp_we_just_sent_and_kickstart_your_app_journey)
        }


        startSMSRetrieverClient()

        setBroadCastReceiver()
        viewModel.startOtpResendTimer()

    }

    private fun setBroadCastReceiver() {
        otpBroadcastReceiver = OtpBroadcastReceiver()
        activity?.registerReceiver(
            otpBroadcastReceiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        )
        otpBroadcastReceiver?.setListener(object : OTPReceiveListener {
            override fun onOTPReceived(otp: String?) {
                LOGS.d("OTP Received $otp")
//                binding.etOtp.setText(otp)
                binding.etOtp.setText(otp)
                unregisterOtpReceiver()
            }

            override fun onOTPTimeOut() {
                LOGS.d("OTP Timeout")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterOtpReceiver()
    }

    fun unregisterOtpReceiver() {
        if (otpBroadcastReceiver != null) {
            try {
                activity?.unregisterReceiver(otpBroadcastReceiver)
            } catch (exp: IllegalArgumentException) {

            }
        }
    }

    private fun startSMSRetrieverClient() {
        val client = SmsRetriever.getClient(requireContext())
        val task: Task<Void> = client.startSmsRetriever()
        task.addOnSuccessListener { aVoid -> }
        task.addOnFailureListener { e -> }
    }

    override fun initListener() {
        binding.bContinue.setOnClickListener {
            verifyOtp()
        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.etOtp.afterTextChanged {
            checkOtp()
        }


        binding.btnResendOtp.setOnClickListener {
            viewModel.startOtpResendTimer()

            binding.tvNotReceiveOtp.gone()
            binding.btnResendOtp.gone()
            viewModel.setEnteredOtp("")
            binding.etOtp.text?.clear()

            viewModel.sendOtp()
        }

    }

    private fun checkOtp() {
        val number = "${binding.etOtp.text}"
        viewModel.setEnteredOtp(number)
    }


    private fun verifyOtp() {
        if (viewModel.enteredOtp.value?.isValidOTP() == true) {
            viewModel.verifyOtp()
        }

    }

    override fun subscribeObservers() {

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
//        successfully otp match
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }
        viewModel.enteredOtp.observe(this) {
            if (it.isValidOTP()) {
                binding.bContinue.enable()
            } else {
                binding.bContinue.disable()
            }
        }

        viewModel.authSuccess.observe(this) {
            it.getContent()?.let { value ->
                if (value) {
                    if (authViewModel.isDevicePaired()) {
                        if (authViewModel.isProfileSetupComplete()) {
                            startActivity(DeviceSetupActivity.getStartIntent(requireContext(), setupDevice = true))
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

        viewModel.timerRunning.observe(viewLifecycleOwner) {
            if (!it) {
                binding.tvNotReceiveOtp.visible()
                binding.btnResendOtp.visible()
            }
        }
    }


}