package com.noisefit.ui.dashboard.feature.experimental

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentExperimentalFeaturesBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExperimentalFeaturesFragment :
    BaseFragment<FragmentExperimentalFeaturesBinding>(FragmentExperimentalFeaturesBinding::inflate) {

    private val viewModel: ExperimentalFeaturesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.layoutToolbar.tvTitle.setTextColor(requireActivity().resources.getColor(R.color.text_enabled))
    }

    private fun setSmartNotificationsStatus() {
        binding.ssSmartNotifications.setSwitchState(viewModel.experimentalSettings.smartNotification)
    }

    private fun setFindMyPhoneNotifications() {
        binding.ssFindMyPhone.setSwitchState(viewModel.experimentalSettings.findMyPhoneNotification)
    }

    override fun initListener() {
        setFindMyPhoneNotifications()
        setSmartNotificationsStatus()
        binding.layoutToolbar.tvTitle.text = getString(R.string.text_smart_notifications)
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.ssSmartNotifications.switch?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            viewModel.experimentalSettings.smartNotification = isChecked
            viewModel.saveExperimentalSettings()
        }

        binding.ssFindMyPhone.switch?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            viewModel.experimentalSettings.findMyPhoneNotification = isChecked
            viewModel.saveExperimentalSettings()
        }

    }

    override fun onStop() {
        super.onStop()
        // viewModel.saveExperimentalSettings()
    }

    override fun subscribeObservers() {

    }


}