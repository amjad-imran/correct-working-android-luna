package com.noisefit.ui.settings.options

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentNotificationSettingBinding
import com.noisefit.ui.profile.ProfileEditViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationSettingFragment :
    BaseFragment<FragmentNotificationSettingBinding>(FragmentNotificationSettingBinding::inflate) {

    private val viewModel: ProfileEditViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initUi()
    }

    private fun initUi() {

        binding.toolbar.tvTitle.text = getString(R.string.text_notifications)

        binding.switchMaster.isChecked = viewModel.notificationSetting.value == 1
        binding.lytNotificationMain.switchMain.isChecked = viewModel.notificationSetting.value == 1

        binding.lytNotificationMain.apply {
            switchMain.isEnabled = false
            switchMain.alpha = 0.5f
            tvMessage.text = getString(R.string.text_notification_toggle_message)
            tvMessage.visible()
        }

        binding.lytOther.apply {
            lytHydration.tvTitle.text = getString(R.string.text_hydration)
            lytSteps.tvTitle.text = getString(R.string.text_steps)
            lytSleep.tvTitle.text = getString(R.string.text_sleep)
        }

    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.switchMaster.setOnCheckedChangeListener { buttonView, isChecked ->

            if (!buttonView.isPressed) return@setOnCheckedChangeListener

            binding.lytNotificationMain.switchMain.isChecked = isChecked

            if (isChecked) {
                viewModel.notificationSetting.value = 1
            } else {
                viewModel.notificationSetting.value = 0
            }
            viewModel.updateUserProfile()
        }

    }

    override fun subscribeObservers() {

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
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.userDetailsUpdated.observe(this) {
            it.getContent()?.let {

            }
        }
    }


}