package com.noisefit.ui.settings.about

import android.annotation.SuppressLint
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAboutBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.share.ShareUtil


class AboutFragment : BaseFragment<FragmentAboutBinding>(FragmentAboutBinding::inflate) {


    @SuppressLint("SetTextI18n")
    override fun initListener() {
        binding.layoutToolbar.tvTitle.text = getString(com.noisefit_commans.R.string.text_about)
        binding.tvAppVersion.text = "V ${BuildConfig.VERSION_NAME}"
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.rowTermsCondition.setOnClickListener {
            startActivity(
                WebViewActivity.getStartIntent(
                    requireActivity(),
                    getString(R.string.text_terms_and_conditions),
                    AppConstants.URL_TERMS_OF_USE
                )
            )
        }
        binding.rowPrivacyPolicy.setOnClickListener {
            startActivity(
                WebViewActivity.getStartIntent(
                    requireActivity(),
                    getString(R.string.text_privacy_policy),
                    AppConstants.URL_PRIVACY_POLICY
                )
            )
        }
        binding.rowCheckForUpdate.setOnClickListener {
            ShareUtil.openPlayStore(requireContext(),"com.noisefit")
        }
    }

    override fun subscribeObservers() {

    }

}