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

    private val viewModel: NotificationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()

        viewModel.getNotificationToggle()
    }

    private fun initUi() {

        binding.toolbar.tvTitle.text = getString(R.string.text_notifications)

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

            viewModel.masterToggle = isChecked

            binding.lytOther.switchOtherMain.isEnabled = isChecked
            if (isChecked.not()) {
                disableOtherNotifications()
            }

            viewModel.updateNotificationToggle()
        }

        binding.lytOther.switchOtherMain.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed.not()) {
                return@setOnCheckedChangeListener
            }

            binding.lytOther.lytHydration.switchMain.isChecked = isChecked
            binding.lytOther.lytSteps.switchMain.isChecked = isChecked
            binding.lytOther.lytSleep.switchMain.isChecked = isChecked

            viewModel.hydrationToggle = isChecked
            viewModel.stepsToggle = isChecked
            viewModel.sleepToggle = isChecked

            viewModel.updateNotificationToggle()

        }

        binding.lytOther.lytHydration.switchMain.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed.not()) {
                return@setOnCheckedChangeListener
            }
            viewModel.hydrationToggle = isChecked
            if(isChecked){
                binding.lytOther.switchOtherMain.isChecked = true
            }

            checkOtherNotifications()
            viewModel.updateNotificationToggle()
        }
        binding.lytOther.lytSteps.switchMain.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed.not()) {
                return@setOnCheckedChangeListener
            }
            viewModel.stepsToggle = isChecked
            if(isChecked){
                binding.lytOther.switchOtherMain.isChecked = true
            }
            checkOtherNotifications()
            viewModel.updateNotificationToggle()
        }
        binding.lytOther.lytSleep.switchMain.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed.not()) {
                return@setOnCheckedChangeListener
            }

            viewModel.sleepToggle = isChecked
            if(isChecked){
                binding.lytOther.switchOtherMain.isChecked = true
            }
            checkOtherNotifications()
            viewModel.updateNotificationToggle()
        }

    }

    fun checkOtherNotifications() {
        if (binding.lytOther.lytHydration.switchMain.isChecked.not() &&
            binding.lytOther.lytSteps.switchMain.isChecked.not() &&
            binding.lytOther.lytSleep.switchMain.isChecked.not()
        ) {
            binding.lytOther.switchOtherMain.isChecked = false
        }
    }

    private fun disableOtherNotifications() {
        binding.lytOther.switchOtherMain.isChecked = false
        binding.lytOther.lytHydration.switchMain.isChecked = false
        binding.lytOther.lytSteps.switchMain.isChecked = false
        binding.lytOther.lytSleep.switchMain.isChecked = false

        viewModel.hydrationToggle = false
        viewModel.stepsToggle = false
        viewModel.sleepToggle = false
    }

    override fun subscribeObservers() {

        viewModel.valueUpdate.observe(this) {
            it.getContent()?.let {
                binding.switchMaster.isChecked = viewModel.masterToggle

                if (viewModel.masterToggle) {

                    binding.lytOther.switchOtherMain.isChecked =
                        viewModel.hydrationToggle || viewModel.stepsToggle || viewModel.sleepToggle

                    binding.lytOther.lytHydration.switchMain.isChecked = viewModel.hydrationToggle
                    binding.lytOther.lytSteps.switchMain.isChecked = viewModel.stepsToggle
                    binding.lytOther.lytSleep.switchMain.isChecked = viewModel.sleepToggle
                }
            }
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
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }


}