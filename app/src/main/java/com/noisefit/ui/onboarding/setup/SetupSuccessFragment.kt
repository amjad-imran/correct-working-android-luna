package com.noisefit.ui.onboarding.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSetupSuccessBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.onboarding.AllDoneActivity
import com.noisefit_commans.ui.BaseFragment

class SetupSuccessFragment :
    BaseFragment<FragmentSetupSuccessBinding>(FragmentSetupSuccessBinding::inflate) {
    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateProgress3.postValue(100)
    }


    override fun initListener() {
        binding.btnLetsGo.setOnClickListener {
            startActivity(OreoMainActivity.getStartIntent(requireContext()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

    }

    override fun subscribeObservers() {

    }


}