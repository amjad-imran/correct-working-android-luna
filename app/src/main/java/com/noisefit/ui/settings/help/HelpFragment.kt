package com.noisefit.ui.settings.help

import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentHelpBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment

import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HelpFragment : BaseFragment<FragmentHelpBinding>(FragmentHelpBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun initListener() {
        binding.layoutToolbar.tvTitle.text = getString(R.string.help)
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.rowAppPermission.setOnClickListener {

      //      navigate(R.id.permissionFragment)
        }
        binding.rowTroubleshoot.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.HELP_TROUBLESHOOTING_PAGE_VISIT)

        }
        binding.rowFitnessHealth.setOnClickListener {
            navigate(R.id.fitnessHealthFragment)
        }
        binding.rowFaq.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.HELP_FAQ_PAGE_VISIT)
            startActivity(
                WebViewActivity.getStartIntent(
                    requireActivity(),
                    "FAQ",
                    AppConstants.URL_FAQ
                )
            )
        }

    }

    override fun subscribeObservers() {

    }

}