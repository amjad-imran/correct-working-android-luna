package com.oreo.ui.home.summary

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.noisefit.luna.databinding.FragmentAlertSettingsBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.DeviceAlertFeature
import com.noisefit_commans.models.HeartRateAlertSettings
import com.noisefit_commans.models.LocalDeviceAlertSettings
import com.noisefit_commans.models.PressureModeSettings
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SleepReminder
import com.noisefit_commans.models.Spo2AlertSettings
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AlertSettingsFragment :
    BaseFragment<FragmentAlertSettingsBinding>(FragmentAlertSettingsBinding::inflate) {

    private val viewModel: AlertSettingsViewModel by viewModels()
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private var isBindingState = false

    override fun initListener() {
        binding.ivBack.setOnClickListener { navigateUpSafe() }
        binding.tvBack.setOnClickListener { navigateUpSafe() }

        binding.switchRestingHeartRate.setOnCheckedChangeListener { _, _ ->
            if (!isBindingState) commitHeartRateSettings()
        }
        binding.switchWorkoutHeartRate.setOnCheckedChangeListener { _, _ ->
            if (!isBindingState) commitHeartRateSettings()
        }
        binding.switchLowHeartRate.setOnCheckedChangeListener { _, _ ->
            if (!isBindingState) commitHeartRateSettings()
        }
        binding.switchSpo2Alert.setOnCheckedChangeListener { _, _ ->
            if (!isBindingState) commitSpo2Settings()
        }
        binding.switchBedtimeReminder.setOnCheckedChangeListener { _, _ ->
            if (!isBindingState) commitSleepReminder()
        }
        binding.switchRelaxationPrompt.setOnCheckedChangeListener { _, _ ->
            if (!isBindingState) commitRelaxationPrompt()
        }
        binding.switchSedentaryReminder.setOnCheckedChangeListener { _, _ ->
            if (!isBindingState) commitSedentaryReminder()
        }

        setNumberCommitListeners(binding.etRestingHeartRate) { commitHeartRateSettings() }
        setNumberCommitListeners(binding.etWorkoutHeartRate) { commitHeartRateSettings() }
        setNumberCommitListeners(binding.etLowHeartRate) { commitHeartRateSettings() }
        setNumberCommitListeners(binding.etSpo2Alert) { commitSpo2Settings() }
        setNumberCommitListeners(binding.etSedentaryInterval) { commitSedentaryReminder() }

        binding.tvBedtimeReminderTime.setOnClickListener {
            showTimePicker(binding.tvBedtimeReminderTime.text.toString()) { formattedTime ->
                binding.tvBedtimeReminderTime.text = formattedTime
                commitSleepReminder()
            }
        }
        binding.tvSedentaryStartTime.setOnClickListener {
            showTimePicker(binding.tvSedentaryStartTime.text.toString()) { formattedTime ->
                binding.tvSedentaryStartTime.text = formattedTime
                commitSedentaryReminder()
            }
        }
        binding.tvSedentaryEndTime.setOnClickListener {
            showTimePicker(binding.tvSedentaryEndTime.text.toString()) { formattedTime ->
                binding.tvSedentaryEndTime.text = formattedTime
                commitSedentaryReminder()
            }
        }
    }

    override fun subscribeObservers() {
        viewModel.alertSettings.observe(viewLifecycleOwner) {
            bindState(it)
        }
        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            viewModel.handleQueryCallback(it)
        }
        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) { event ->
            event.getContent()?.let { callback ->
                viewModel.handleUpdateCallback(callback)
            }
        }
        viewModel.sessionManager.connectStateRing.observe(viewLifecycleOwner) { connectState ->
            updateConnectionStatus(connectState)
            viewModel.onConnectedStateChanged(connectState)
        }
        viewModel.loadState()
    }

    private fun bindState(state: LocalDeviceAlertSettings) {
        isBindingState = true

        binding.etRestingHeartRate.setText(state.heartRate.restingThreshold.toString())
        binding.switchRestingHeartRate.isChecked = state.heartRate.restingEnabled

        binding.etWorkoutHeartRate.setText(state.heartRate.workoutThreshold.toString())
        binding.switchWorkoutHeartRate.isChecked = state.heartRate.workoutEnabled

        binding.etLowHeartRate.setText(state.heartRate.lowThreshold.toString())
        binding.switchLowHeartRate.isChecked = state.heartRate.lowEnabled

        binding.etSpo2Alert.setText(state.spo2.threshold.toString())
        binding.switchSpo2Alert.isChecked = state.spo2.enabled

        binding.switchBedtimeReminder.isChecked = state.sleepReminder.status
        binding.tvBedtimeReminderTime.text = formatTime(state.sleepReminder.hour, state.sleepReminder.minute)

        binding.switchRelaxationPrompt.isChecked = state.pressureMode.relaxationPromptEnabled

        binding.switchSedentaryReminder.isChecked = state.sedentaryReminder.status
        binding.tvSedentaryStartTime.text =
            formatTime(state.sedentaryReminder.startHour, state.sedentaryReminder.startMinute)
        binding.tvSedentaryEndTime.text =
            formatTime(state.sedentaryReminder.endHour, state.sedentaryReminder.endMinute)
        binding.etSedentaryInterval.setText(state.sedentaryReminder.interval.toString())

        applyFeatureSupport(
            DeviceAlertFeature.HEART_RATE,
            state.isSupported(DeviceAlertFeature.HEART_RATE),
            binding.etRestingHeartRate,
            binding.switchRestingHeartRate,
            binding.etWorkoutHeartRate,
            binding.switchWorkoutHeartRate,
            binding.etLowHeartRate,
            binding.switchLowHeartRate
        )
        applyFeatureSupport(
            DeviceAlertFeature.SPO2,
            state.isSupported(DeviceAlertFeature.SPO2),
            binding.etSpo2Alert,
            binding.switchSpo2Alert
        )
        applyFeatureSupport(
            DeviceAlertFeature.SLEEP_REMINDER,
            state.isSupported(DeviceAlertFeature.SLEEP_REMINDER),
            binding.tvBedtimeReminderTime,
            binding.switchBedtimeReminder
        )
        applyFeatureSupport(
            DeviceAlertFeature.RELAXATION_PROMPT,
            state.isSupported(DeviceAlertFeature.RELAXATION_PROMPT),
            binding.switchRelaxationPrompt
        )
        applyFeatureSupport(
            DeviceAlertFeature.SEDENTARY_REMINDER,
            state.isSupported(DeviceAlertFeature.SEDENTARY_REMINDER),
            binding.tvSedentaryStartTime,
            binding.tvSedentaryEndTime,
            binding.etSedentaryInterval,
            binding.switchSedentaryReminder
        )

        isBindingState = false
    }

    private fun commitHeartRateSettings() {
        val restingThreshold = binding.etRestingHeartRate.text?.toString()?.trim()?.toIntOrNull()
        val workoutThreshold = binding.etWorkoutHeartRate.text?.toString()?.trim()?.toIntOrNull()
        val lowThreshold = binding.etLowHeartRate.text?.toString()?.trim()?.toIntOrNull()
        if (restingThreshold == null || workoutThreshold == null || lowThreshold == null) {
            context.showShortToast("Enter valid heart-rate thresholds.")
            return
        }
        viewModel.saveHeartRateSettings(
            HeartRateAlertSettings(
                restingEnabled = binding.switchRestingHeartRate.isChecked,
                restingThreshold = restingThreshold,
                workoutEnabled = binding.switchWorkoutHeartRate.isChecked,
                workoutThreshold = workoutThreshold,
                lowEnabled = binding.switchLowHeartRate.isChecked,
                lowThreshold = lowThreshold
            )
        )
    }

    private fun commitSpo2Settings() {
        val threshold = binding.etSpo2Alert.text?.toString()?.trim()?.toIntOrNull()
        if (threshold == null) {
            context.showShortToast("Enter a valid SpO2 threshold.")
            return
        }
        viewModel.saveSpo2Settings(
            Spo2AlertSettings(
                enabled = binding.switchSpo2Alert.isChecked,
                threshold = threshold
            )
        )
    }

    private fun commitSleepReminder() {
        val bedtime = parseTime(binding.tvBedtimeReminderTime.text.toString()) ?: return
        viewModel.saveSleepReminder(
            SleepReminder(
                status = binding.switchBedtimeReminder.isChecked,
                hour = bedtime.get(Calendar.HOUR_OF_DAY),
                minute = bedtime.get(Calendar.MINUTE),
                second = 0,
                millisecond = 0
            )
        )
    }

    private fun commitRelaxationPrompt() {
        val existingState = viewModel.alertSettings.value ?: return
        viewModel.savePressureModeSettings(
            PressureModeSettings(
                stressMonitoringEnabled = existingState.pressureMode.stressMonitoringEnabled,
                relaxationPromptEnabled = binding.switchRelaxationPrompt.isChecked
            )
        )
    }

    private fun commitSedentaryReminder() {
        val interval = binding.etSedentaryInterval.text?.toString()?.trim()?.toIntOrNull()
        val start = parseTime(binding.tvSedentaryStartTime.text.toString())
        val end = parseTime(binding.tvSedentaryEndTime.text.toString())
        if (interval == null || start == null || end == null) {
            context.showShortToast("Enter a valid sedentary schedule.")
            return
        }
        viewModel.saveSedentaryReminder(
            SedentaryData(
                status = binding.switchSedentaryReminder.isChecked,
                interval = interval,
                startHour = start.get(Calendar.HOUR_OF_DAY),
                startMinute = start.get(Calendar.MINUTE),
                endHour = end.get(Calendar.HOUR_OF_DAY),
                endMinute = end.get(Calendar.MINUTE)
            )
        )
    }

    private fun updateConnectionStatus(connectState: ConnectState?) {
        binding.tvStatus.text = when (connectState) {
            is ConnectState.ConnectSuccess -> {
                "Ring connected. Supported alert changes sync and verify immediately."
            }
            else -> {
                "Ring disconnected. Changes stay local and sync after reconnect."
            }
        }
    }

    private fun applyFeatureSupport(
        feature: DeviceAlertFeature,
        supported: Boolean,
        vararg views: View
    ) {
        views.forEach { view ->
            view.isEnabled = supported
            view.alpha = if (supported) 1f else 0.45f
        }
        when (feature) {
            DeviceAlertFeature.HEART_RATE -> binding.layoutStressAlert.isVisible = true
            DeviceAlertFeature.SPO2,
            DeviceAlertFeature.RELAXATION_PROMPT,
            DeviceAlertFeature.SLEEP_REMINDER,
            DeviceAlertFeature.SEDENTARY_REMINDER,
            DeviceAlertFeature.HIGH_STRESS_INDEX,
            DeviceAlertFeature.WEAR_DETECTION -> Unit
        }
    }

    private fun setNumberCommitListeners(view: View, onCommit: () -> Unit) {
        val editText = view as androidx.appcompat.widget.AppCompatEditText
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onCommit()
                true
            } else {
                false
            }
        }
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !isBindingState) {
                onCommit()
            }
        }
    }

    private fun showTimePicker(initialTime: String, onTimeSelected: (String) -> Unit) {
        val calendar = parseTime(initialTime) ?: Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                onTimeSelected(formatTime(hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun parseTime(value: String): Calendar? {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = timeFormat.parse(value) ?: return null
            calendar
        } catch (ex: Exception) {
            null
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.timeFormat()
    }

    private fun Calendar.timeFormat(): String {
        return timeFormat.format(time)
    }
}
