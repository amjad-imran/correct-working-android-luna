package com.noisefit.ui.settings.options

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentNotificationSettingBinding
import com.noisefit.ui.profile.ProfileEditViewModel
import com.noisefit_commans.ui.BaseFragment
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

        binding.toolbar.tvTitle.text = getString(R.string.text_notification)

        binding.lytNotificationMain.apply {
            tvTitle.text = getString(R.string.text_enable_notifications)
            tvMessage.text = getString(R.string.text_notification_message)
        }

        binding.lytNotificationMain.switchMain.isChecked = viewModel.notificationSetting.value == 1
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytNotificationMain.switchMain.setOnCheckedChangeListener { buttonView, isChecked ->

            if (!buttonView.isPressed) return@setOnCheckedChangeListener

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