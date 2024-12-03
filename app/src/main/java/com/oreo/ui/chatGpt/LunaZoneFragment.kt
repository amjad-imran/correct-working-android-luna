package com.oreo.ui.chatGpt

import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLunaZoneBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LunaZoneFragment : BaseFragment<FragmentLunaZoneBinding>(FragmentLunaZoneBinding::inflate) {

    override fun initListener() {
        binding.tvDashboard.setOnClickListener {
            navigate(R.id.aiSummaryFragment)
        }
    }

    override fun subscribeObservers() {

    }


}