package com.noisefit.ui.onboarding.otp.number

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.auth.api.credentials.HintRequest
import com.noisefit.R
import com.noisefit.databinding.FragmentOtpNumberBinding
import com.noisefit.ui.onboarding.auth.AuthViewModel
import com.noisefit.ui.onboarding.otp.OtpViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.isValidMobileNumber
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.TextWatcherExtended
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpNumberFragment :
    BaseFragment<FragmentOtpNumberBinding>(FragmentOtpNumberBinding::inflate) {

    companion object {
        var CREDENTIAL_PICKER_REQUEST = 1
    }

    private val viewModel: OtpViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.LAND_ON_ENTER_PHONE_NO_PAGE_VISIT)
        viewModel.uuid = authViewModel.uuid
        viewModel.uuidType = authViewModel.uuidType
        viewModel.googleImageUrl = authViewModel.googleImageUrl
        viewModel.email = authViewModel.email
        viewModel.isMobileExist = false
        //phoneSelection()
    }

    override fun initListener() {
        //binding.etMobileNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        binding.etMobileNumber.addTextChangedListener(object : TextWatcherExtended() {
            override fun afterTextChanged(s: Editable?, backSpace: Boolean) {
                // Here you are! You got missing "backSpace" flag
                viewModel.setContactNumber(s.toString())
//                val initLength: Int = binding.etMobileNumber.text.length
//                if (!backSpace) {
//                    val digits = java.lang.StringBuilder()
//                    val phone = java.lang.StringBuilder()
//                    val chars: CharArray = binding.etMobileNumber.text.toString().toCharArray()
//                    for (x in chars.indices) {
//                        if (Character.isDigit(chars[x])) {
//                            digits.append(chars[x])
//                        }
//                    }
//
//
//                    if (digits.toString().length >= 3) {
//                        var countryCode = String()
//
//                        countryCode += digits.toString()
//                            .substring(0, 3) + " "
//                        phone.append(countryCode)
//                        if (digits.toString().length >= 6) {
//                            var regionCode = String()
//                            regionCode += digits.toString().substring(3, 6) + " "
//                            phone.append(regionCode)
//                            if (digits.toString().length >= 12) {
//                                phone.append(digits.toString().substring(6, 12))
//                            } else {
//                                phone.append(digits.toString().substring(6))
//                            }
//                        } else {
//                            phone.append(digits.toString().substring(3))
//                        }
//
//                        binding.etMobileNumber.removeTextChangedListener(this)
//                        val cp = binding.etMobileNumber.selectionStart
//                        binding.etMobileNumber.setText(phone.toString())
//                        val endlen: Int = binding.etMobileNumber.text.length
//                        val sel = cp + (endlen - initLength)
//                        if (sel > 0 && sel <= phone.length) {
//                            binding.etMobileNumber.setSelection(sel)
//                        } else {
//                            // place cursor at the end?
//                            binding.etMobileNumber.setSelection(binding.etMobileNumber.text.length)
//                        }
//
//
//                        // binding.etMobileNumber.setSelection(binding.etMobileNumber.selectionStart)
//                        binding.etMobileNumber.addTextChangedListener(this)
//                    } else {
//                        return
//                    }
//                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do something useful if you wish.
                // Or override it in TextWatcherExtended class if want to avoid it here
            }
        })


        binding.bContinue.setOnClickListener {
            sendOtp()
        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.etMobileNumber.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendOtp()
                true
            }

            false
        }
    }

    private fun sendOtp() {
        if (viewModel.contactNumber.value?.isValidMobileNumber() == true) {
            viewModel.sendOtp()
        }

    }

    override fun subscribeObservers() {

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
        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }
        viewModel.successMessage.observe(this) { message ->
            message?.getContent()?.let { msg ->
                if (!msg.isNullOrEmpty()) {
                    viewModel.resetSuccessMessage()
                    uiController.onDisplayError(msg)
                    authViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTINUE_ENTER_PHONE_NUMBER_CLICK)
                    navigate(R.id.otpVerifyFragment)
                }
            }

        }

        viewModel.showMobExistingBSheet.observe(this) {
            it.getContent()?.let { response ->
                LOGS.d("show existing user bottomsheet")
                setFragmentResultListener(EXISTING_USER_KEY) { key, bundle ->
                    val isSelected = bundle.getBoolean("isSelected")
                    if (isSelected) {
                        viewModel.isMobileExist = true
                        viewModel.sendOtp()
                    } else {
                        viewModel.isMobileExist = false
                    }
                }
                navigate(
                    R.id.existingUserBottomSheet, Bundle().apply {
                        putString("imageUrl", response.image_url)
                        putString("email", response.mask_email)
                        putString("mobileNumber", response.mobile)
                    }
                )
            }

        }

        viewModel.contactNumber.observe(this) { number ->
            if (!number.isValidMobileNumber()) {
                binding.bContinue.disable()
            } else {
                binding.bContinue.enable()
            }
        }
    }

    private fun phoneSelection() {

        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val options = CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()
        val credentialsClient = Credentials.getClient(requireContext(), options)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        try {
            startIntentSenderForResult(
                intent.intentSender,
                CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0, Bundle()
            )
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == RESULT_OK) {
            val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)

            credential?.apply {
                binding.etMobileNumber.setText(credential.id.replace("+91", ""))
            }
        }
    }

}