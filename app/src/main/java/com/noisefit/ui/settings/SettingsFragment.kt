package com.noisefit.ui.settings

import com.noisefit.luna.databinding.FragmentSettingsBinding
import com.noisefit_commans.ui.BaseFragment

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override fun initListener() {

        binding.lytGroup1.tvUnits.setOnClickListener {

        }
        binding.lytGroup1.tvNotifications.setOnClickListener {

        }
        binding.lytGroup2.tvGoogleFit.setOnClickListener {

        }

    }

    override fun subscribeObservers() {

    }


}