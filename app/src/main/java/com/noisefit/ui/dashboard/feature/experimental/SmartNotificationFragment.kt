package com.noisefit.ui.dashboard.feature.experimental

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentSmartNotificationBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImage
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SmartNotificationFragment :
    BaseFragment<FragmentSmartNotificationBinding>(FragmentSmartNotificationBinding::inflate) {

    private val viewModel: ExperimentalFeaturesViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setSmartNotificationsStatus() {
        binding.lytFeatureTile.llSwitch.isChecked =
            (viewModel.experimentalSettings.smartNotification)
    }

    override fun initListener() {
        setSmartNotificationsStatus()
        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_smart_notification)
            tvTitle.text = getString(R.string.text_smart_notifications)
            tvTitleDisc.text = getString(R.string.text_data_not_syncing)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_smart_notifications)
            tvDesc.text = getString(R.string.text_smart_notifications_desc)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.lytFeatureTile.llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            viewModel.experimentalSettings.smartNotification = isChecked
            viewModel.saveExperimentalSettings()
        }
    }

    override fun subscribeObservers() {

    }

}