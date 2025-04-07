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
import com.noisefit_commans.utils.MoEngageLunaAppEvents
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


            lytHydration.ivSetting.setImageResource(R.drawable.ic_settings_hydration)
            lytSteps.ivSetting.setImageResource(R.drawable.ic_settings_steps)
            lytSleep.ivSetting.setImageResource(R.drawable.ic_settings_sleep)

            if (viewModel.shouldShowFemaleHealth()) {
                lytFemaleHealth.tvTitle.text = getString(R.string.text_menstrual_health)
                lytFemaleHealth.ivSetting.setImageResource(R.drawable.ic_settings_female_health)
                lytFemaleHealth.root.visible()
            } else {
                lytFemaleHealth.root.gone()
            }
        }

    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.switchMaster.setOnCheckedChangeListener { buttonView, isChecked ->



            if (!buttonView.isPressed) return@setOnCheckedChangeListener

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.notification_toggled,
                HashMap<String, Any>().apply {
                    this["target"] = "main"
                }
            )

            binding.lytNotificationMain.switchMain.isChecked = isChecked

            viewModel.masterToggle = isChecked


            handleOtherNotifications(isChecked)

            viewModel.updateNotificationToggle()
        }

        binding.lytOther.switchOtherMain.setOnCheckedChangeListener { buttonView, isChecked ->

            if (buttonView.isPressed.not()) {
                return@setOnCheckedChangeListener
            }

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.notification_toggled,
                HashMap<String, Any>().apply {
                    this["target"] = "all_reminders"
                }
            )

            binding.lytOther.lytHydration.switchMain.isChecked = isChecked
            binding.lytOther.lytSteps.switchMain.isChecked = isChecked
            binding.lytOther.lytSleep.switchMain.isChecked = isChecked
            binding.lytOther.lytFemaleHealth.switchMain.isChecked = isChecked

            viewModel.hydrationToggle = isChecked
            viewModel.stepsToggle = isChecked
            viewModel.sleepToggle = isChecked
            viewModel.femaleHealthToggle = isChecked

            viewModel.updateNotificationToggle()

        }

        binding.lytOther.lytHydration.switchMain.setOnCheckedChangeListener { buttonView, isChecked ->

            if (buttonView.isPressed.not()) {
                return@setOnCheckedChangeListener
            }

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.notification_toggled,
                HashMap<String, Any>().apply {
                    this["target"] = "hydration"
                }
            )

            viewModel.hydrationToggle = isChecked
            if (isChecked) {
                binding.lytOther.switchOtherMain.isChecked = true
            }

            checkOtherNotifications()
            viewModel.updateNotificationToggle()
        }
        binding.lytOther.lytSteps.switchMain.setOnCheckedChangeListener { buttonView, isChecked ->

            if (buttonView.isPressed.not()) {
                return@setOnCheckedChangeListener
            }

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.notification_toggled,
                HashMap<String, Any>().apply {
                    this["target"] = "steps"
                }
            )

            viewModel.stepsToggle = isChecked
            if (isChecked) {
                binding.lytOther.switchOtherMain.isChecked = true
            }
            checkOtherNotifications()
            viewModel.updateNotificationToggle()
        }
        binding.lytOther.lytSleep.switchMain.setOnCheckedChangeListener { buttonView, isChecked ->

            if (buttonView.isPressed.not()) {
                return@setOnCheckedChangeListener
            }

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.notification_toggled,
                HashMap<String, Any>().apply {
                    this["target"] = "sleep"
                }
            )

            viewModel.sleepToggle = isChecked
            if (isChecked) {
                binding.lytOther.switchOtherMain.isChecked = true
            }
            checkOtherNotifications()
            viewModel.updateNotificationToggle()
        }
        binding.lytOther.lytFemaleHealth.switchMain.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed.not()) {
                return@setOnCheckedChangeListener
            }

            viewModel.femaleHealthToggle = isChecked
            if (isChecked) {
                binding.lytOther.switchOtherMain.isChecked = true
            }
            checkOtherNotifications()
            viewModel.updateNotificationToggle()
        }

    }

    fun checkOtherNotifications() {
        if (binding.lytOther.lytHydration.switchMain.isChecked.not() &&
            binding.lytOther.lytSteps.switchMain.isChecked.not() &&
            binding.lytOther.lytSleep.switchMain.isChecked.not() &&
            binding.lytOther.lytFemaleHealth.switchMain.isChecked.not()
        ) {
            binding.lytOther.switchOtherMain.isChecked = false
        }
    }
    private fun handleOtherNotifications(state: Boolean) {
        binding.lytOther.switchOtherMain.isEnabled = state
        binding.lytOther.lytHydration.switchMain.isEnabled = state
        binding.lytOther.lytSteps.switchMain.isEnabled = state
        binding.lytOther.lytSleep.switchMain.isEnabled = state
        binding.lytOther.lytFemaleHealth.switchMain.isEnabled = state

        binding.lytOther.switchOtherMain.isChecked = state
        binding.lytOther.lytHydration.switchMain.isChecked = state
        binding.lytOther.lytSteps.switchMain.isChecked = state
        binding.lytOther.lytSleep.switchMain.isChecked = state
        binding.lytOther.lytFemaleHealth.switchMain.isChecked = state

        viewModel.hydrationToggle = state
        viewModel.stepsToggle = state
        viewModel.sleepToggle = state
        viewModel.femaleHealthToggle = state
    }

    override fun subscribeObservers() {

        viewModel.valueUpdate.observe(this) {
            it.getContent()?.let {
                binding.switchMaster.isChecked = viewModel.masterToggle
                binding.lytNotificationMain.switchMain.isChecked = viewModel.masterToggle

                if (viewModel.masterToggle) {

                    binding.lytOther.switchOtherMain.isChecked =
                        viewModel.hydrationToggle || viewModel.stepsToggle || viewModel.sleepToggle|| viewModel.femaleHealthToggle

                    binding.lytOther.lytHydration.switchMain.isChecked = viewModel.hydrationToggle
                    binding.lytOther.lytSteps.switchMain.isChecked = viewModel.stepsToggle
                    binding.lytOther.lytSleep.switchMain.isChecked = viewModel.sleepToggle
                    binding.lytOther.lytFemaleHealth.switchMain.isChecked = viewModel.femaleHealthToggle
                }else{
                    binding.lytOther.switchOtherMain.isEnabled = false
                    binding.lytOther.lytHydration.switchMain.isEnabled = false
                    binding.lytOther.lytSteps.switchMain.isEnabled = false
                    binding.lytOther.lytSleep.switchMain.isEnabled = false
                    binding.lytOther.lytFemaleHealth.switchMain.isEnabled = false
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