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
import com.noisefit_commans.utils.MoEngageLunaAppEvents
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

        binding.btnCheckForUpdate.text = if (viewModel.localDataStore.isNewAppVersionAvailable()) {
            getString(R.string.text_update_available)
        } else {
            getString(R.string.text_check_for_update)
        }
        binding.rowTermsCondition.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_about_terms_conditions_click)
            startActivity(
                WebViewActivity.getStartIntent(
                    requireActivity(),
                    getString(R.string.text_terms_and_conditions),
                    AppConstants.URL_TERMS_OF_USE
                )
            )
        }
        binding.rowPrivacyPolicy.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_about_privacy_policy_click)
            startActivity(
                WebViewActivity.getStartIntent(
                    requireActivity(),
                    getString(R.string.text_privacy_policy),
                    AppConstants.URL_PRIVACY_POLICY
                )
            )
        }
        binding.btnCheckForUpdate.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_about_check_update_click)
            ShareUtil.openPlayStore(requireContext(), "com.noisefit.luna")
        }
    }

    override fun subscribeObservers() {

    }

}