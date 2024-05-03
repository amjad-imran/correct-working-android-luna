package com.noisefit.ui.settings.options

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentUnitSelectionBinding
import com.noisefit.ui.profile.ProfileEditViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnitSelectionFragment :
    BaseFragment<FragmentUnitSelectionBinding>(FragmentUnitSelectionBinding::inflate) {

    private val viewModel: ProfileEditViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_units)

        binding.lytMetric.apply {
            tvTitle.text = getString(R.string.text_metric)
            tvMessage.text = getString(R.string.text_metric_message)
        }
        binding.lytImperial.apply {
            tvTitle.text = getString(R.string.text_imperial)
            tvMessage.text = getString(R.string.text_message_imperial)
        }
    }

    private fun updateRadioButtons() {
        if (viewModel.isMetric()) {
            binding.lytMetric.ivRadioButton.setImageResource(R.drawable.ic_radio_selected)
            binding.lytImperial.ivRadioButton.setImageResource(R.drawable.ic_radio_deselected)
        } else {
            binding.lytMetric.ivRadioButton.setImageResource(R.drawable.ic_radio_deselected)
            binding.lytImperial.ivRadioButton.setImageResource(R.drawable.ic_radio_selected)
        }
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytMetric.root.setOnClickListener {
            viewModel.updateUserProfile()
        }
        binding.lytImperial.root.setOnClickListener {
            viewModel.updateUserProfile()
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.userDetailsUpdated.observe(this) {
            it.getContent()?.let {
                updateRadioButtons()
            }
        }

    }

    override fun subscribeObservers() {

    }

}