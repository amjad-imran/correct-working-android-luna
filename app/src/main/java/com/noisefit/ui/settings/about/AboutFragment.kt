package com.noisefit.ui.settings.about

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAboutBinding
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : BaseFragment<FragmentAboutBinding>(FragmentAboutBinding::inflate) {
    private val viewModel: AboutViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun initListener() {
        binding.layoutToolbar.tvTitle.text = getString(com.noisefit_commans.R.string.text_about)
        binding.tvAppVersion.text = "V ${BuildConfig.VERSION_NAME}"
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.rowTermsCondition.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ABOUT_TERMS_CONDITIONS_CLICK)
            startActivity(
                WebViewActivity.getStartIntent(
                    requireActivity(),
                    getString(R.string.text_terms_and_conditions),
                    AppConstants.URL_TERMS_OF_USE
                )
            )
        }
        binding.rowPrivacyPolicy.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ABOUT_PRIVACY_POLICY_CLICK)
            startActivity(
                WebViewActivity.getStartIntent(
                    requireActivity(),
                    getString(R.string.text_privacy_policy),
                    AppConstants.URL_PRIVACY_POLICY
                )
            )
        }
        binding.rowCheckForUpdate.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ABOUT_CHECK_UPDATE_CLICK)
            ShareUtil.openPlayStore(requireContext(), "com.noisefit.luna")
        }
    }

    override fun subscribeObservers() {

    }

}