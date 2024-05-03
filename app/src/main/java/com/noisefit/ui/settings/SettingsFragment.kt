package com.noisefit.ui.settings

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSettingsBinding
import com.noisefit_commans.ui.BaseFragment

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_settings)

    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytGroup1.tvUnits.setOnClickListener {
            navigate(R.id.unitSelectionFragment)
        }
        binding.lytGroup1.tvNotifications.setOnClickListener {

        }
        binding.lytGroup2.tvGoogleFit.setOnClickListener {
            navigate(R.id.googleFitFragmentOreo)
        }

    }

    override fun subscribeObservers() {

    }


}