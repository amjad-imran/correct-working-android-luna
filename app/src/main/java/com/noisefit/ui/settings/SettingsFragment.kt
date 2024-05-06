package com.noisefit.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSettingsBinding
import com.noisefit.ui.profile.ProfileEditViewModel
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel: ProfileEditViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_settings)

        binding.lytGroup1.tvUnitValue.text = if (viewModel.isMetric()) {
            getString(R.string.text_metric)
        } else {
            getString(R.string.text_imperial)
        }

    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytGroup1.tvUnits.setOnClickListener {
            navigate(R.id.unitSelectionFragment)
        }
        binding.lytGroup1.tvNotifications.setOnClickListener {
            navigate(R.id.notificationSettingFragment)
        }
        binding.lytGroup2.tvGoogleFit.setOnClickListener {
            navigate(R.id.googleFitFragmentOreo)
        }


    }

    override fun subscribeObservers() {

    }


}