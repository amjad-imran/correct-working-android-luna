package com.noisefit.ui.settings.setting.device

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.slider.Slider
import com.noisefit.R
import com.noisefit.data.local.AppStaticData
import com.noisefit.databinding.FragmentDeviceSettingBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.*
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import com.noisefit_commans.ui.BaseFragment

@AndroidEntryPoint
class DeviceSettingFragment :
    BaseFragment<FragmentDeviceSettingBinding>(FragmentDeviceSettingBinding::inflate) {

    private val viewModel: DeviceViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel.setLoading(true)

        getInitialDeviceTimeFormat()
        handleUI()
    }

    private fun getInitialDeviceTimeFormat() {

        viewModel.getInitialDeviceTimeFormat()
        setDeviceTimeTxtData()
    }

    private fun handleUI() {
        val feature = viewModel.localDataStore.getDeviceFeatures()
//        if (feature?.brightnessLevel == 1) {
//            viewModel.sessionManager.sendQueryAction(QueryAction.GetBrightnessLevelSettings)
//            binding.tvBrightnessLabel.visible()
//            binding.ivBrightnessLow.visible()
//            binding.ivBrightnessHigh.visible()
//            binding.lytProgress.root.visible()
//            binding.include6.root.visible()
//        }

        if (feature?.language == 1) {
            viewModel.sessionManager.sendQueryAction(QueryAction.GetLanguage)
            binding.tvLanguageLabel.visible()
            binding.tvLanguageMsg.visible()
            binding.tvLanguageValue.visible()
            binding.include9.root.visible()
        }

        if (feature?.password == 1) {
            viewModel.sessionManager.sendQueryAction(QueryAction.GetWatchPassword)
            binding.tvDevicePasswordLabel.visible()
            binding.switchCompatPassword.visible()
            binding.btnEditPassword.visible()
        }

        if (feature?.bodyTemperatureUnit == 1) {
            viewModel.sessionManager.sendQueryAction(QueryAction.GetBodyTempUnit)
            binding.tvTemperatureLabel.visible()
            binding.tvUnitValue.visible()
            binding.include6.root.visible()
        }

        if (feature?.doNotDisturb == 1) {
            viewModel.sessionManager.sendQueryAction(QueryAction.GetDoNotDisturbData)
            binding.tvDoNotDisturb.visible()
            binding.switchCompat.visible()
            binding.tvDesc.visible()
            binding.include7.root.visible()
        }

        if (feature?.screenTime == 1) {
            viewModel.sessionManager.sendQueryAction(QueryAction.GetScreenAwakeInterval)
            binding.tvScreenLabel.visible()
            binding.tvScreenTimeValue.visible()
            binding.tvScreenMsg.visible()
            binding.include10.root.visible()
        }

        if (feature?.vibrationIntensity == 1) {
            viewModel.sessionManager.sendQueryAction(QueryAction.GetVibrationIntensity)
            binding.tvVibrationLabel.visible()
            binding.tvVibrationValue.visible()
            binding.tvVibrationMsg.visible()
            binding.include11.root.visible()
        }

    }

    private fun syncPassword() {
        viewModel.setLoading(true)
        val watchPassword =
            WatchPassword(viewModel.watchPassword.password, viewModel.watchPassword.status)
        uiController.hideSoftKeyboard()
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetWatchPassword(
                watchPassword
            )
        )
    }

    override fun initListener() {

        binding.lytProgress.pgBr.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                setBrightnessLevel(value.toInt())
            }
        })
        binding.layoutToolbar.tvTitle.text = getString(R.string.text_device_setting)
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnSyncTime.setOnClickListener {
            syncTime()
        }

        binding.switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            viewModel.setLoading(true)
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.UpdateDND(
                    DoNotDisturb(
                        status = isChecked
                    )
                )
            )
        }

        setFragmentResultListener(PASSWORD_REQUEST_KEY) { _, bundle ->
            val cancel = bundle.getBoolean("cancel")
            val selectedValue = bundle.getString("password")
            if (!selectedValue.isNullOrEmpty()) {
                viewModel.watchPassword.apply {
                    password = selectedValue
                    status = true
                }
                editButtonState(true)
                syncPassword()
            }
            if (cancel) {
                setSwitchState(viewModel.watchPassword.status)
            }


        }

        binding.btnEditPassword.setOnClickListener {
            navigate(
                DeviceSettingFragmentDirections.actionSettingDeviceFragmentToPasswordBottomSheet(
                    viewModel.watchPassword.password ?: ""
                )
            )
        }

        binding.switchCompatPassword.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            if (viewModel.watchPassword.password.isNullOrEmpty()) {
                navigate(
                    DeviceSettingFragmentDirections.actionSettingDeviceFragmentToPasswordBottomSheet(
                        viewModel.watchPassword.password ?: ""
                    )
                )
            } else {
                editButtonState(isChecked)
                viewModel.watchPassword.status = isChecked
                syncPassword()
            }
        }

        binding.tvUnitValue.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                viewModel.setLoading(true)
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                viewModel.tempUnit = if (position == 0) {
                    Units.METRIC
                } else {
                    Units.IMPERIAL
                }

                setTempUnit()
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetBodyTemperatureUnit(
                        unit = viewModel.tempUnit
                    )
                )

            }

            navigate(
                DeviceSettingFragmentDirections.actionSettingDeviceFragmentToValueSelectorBottomSheet(
                    viewModel.getTemperatureUnit(),
                    AppStaticData.getTemperatureValues(),
                    getString(R.string.body_temperature_unit)
                )
            )
        }
        binding.tvDeviceTimeValue.setOnClickListener {

            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                viewModel.setLoading(true)
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                if (selectedValue != null) {
                    viewModel.timeFormat = if (position == 0) {
                        TimeFormats.HOURS_12
                    } else {
                        TimeFormats.HOURS_24
                    }
                    setDeviceTimeTxtData()
                    syncTime()
                    //viewModel.setLoading(true)
                    viewModel.localDataStore.setTimeFormat(selectedValue)
                }

            }
            navigate(
                DeviceSettingFragmentDirections.actionSettingDeviceFragmentToValueSelectorBottomSheet(
                    viewModel.timeFormat.type,
                    AppStaticData.getTimeFormatValues(),
                    getString(R.string.device_time)
                )
            )
        }
        binding.tvLanguageValue.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->

                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                if (selectedValue != null) {
                    viewModel.language = selectedValue
                    syncLanguage()
                    setLanguage()
                }

            }
            navigate(
                DeviceSettingFragmentDirections.actionSettingDeviceFragmentToValueSelectorBottomSheet(
                    viewModel.language,
                    viewModel.deviceLangList.toTypedArray(),
                    getString(R.string.device_language)
                )
            )
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MYDEVICE_LANGUAGE_SETTINGS)
        }
        binding.tvScreenTimeValue.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { key, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                selectedValue?.let {
                    viewModel.screenTime = try {
                        selectedValue.split(" ")[0].toInt()
                    } catch (exp: Exception) {
                        5
                    }
                    setScreenTime()
                    syncScreenTime()
                }
            }
            navigate(
                DeviceSettingFragmentDirections.actionSettingDeviceFragmentToValueSelectorBottomSheet(
                    "${viewModel.screenTime} sec",
                    AppStaticData.getScreenInterval(),
                    getString(R.string.screen_time)
                )
            )
        }

        binding.tvVibrationValue.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { key, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                selectedValue?.let {
                    viewModel.vibrationIntensity = selectedValue
                    syncVibrationIntensity()
                    setVibrationIntensity()
                }
            }
            navigate(
                DeviceSettingFragmentDirections.actionSettingDeviceFragmentToValueSelectorBottomSheet(
                    viewModel.vibrationIntensity,
                    AppStaticData.getVibrationIntensity(),
                    getString(R.string.text_vibration_intensity)
                )
            )

        }
    }

    private fun syncScreenTime() {
        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetScreenAwakeInterval(
                viewModel.screenTime
            )
        )
    }

    private fun syncVibrationIntensity() {
        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetVibrationIntensity(
                VibrationIntensity(viewModel.getVibration())
            )
        )
    }


    private fun syncLanguage() {
        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.UpdateLanguage(
                Language(
                    language = viewModel.fromLanguage().type
                )
            )
        )
    }

    private fun syncTime() {
//        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetDeviceDateTime(
                Calendar.getInstance(), TimeFormat(viewModel.timeFormat.type)
            )
        )


    }

    private fun setSwitchState(isChecked: Boolean) {
        binding.switchCompatPassword.isChecked = isChecked
        editButtonState(isChecked)
    }

    private fun editButtonState(isChecked: Boolean) {
        if (isChecked) {
            binding.btnEditPassword.enable()
        } else {
            binding.btnEditPassword.disable()
        }
    }

    private fun setDeviceTimeTxtData() {
        binding.tvDeviceTimeValue.text = viewModel.timeFormat.type
    }

    private fun setBrightnessLevel(level: Int) {
        binding.progressBar.root.visible()
        viewModel.sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetBrightnessLevel(level))

    }

    override fun subscribeObservers() {
        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.BodyTempUnit -> {
                    viewModel.setLoading(false)
                    viewModel.tempUnit = it.unit
                    setTempUnit()
                }
                is QueryCallback.LanguageReceived -> {
                    viewModel.setLoading(false)
                    viewModel.toLanguage(it.language)
                    setLanguage()
                }
                is QueryCallback.ScreenAwakeIntervalObtained -> {
                    viewModel.setLoading(false)
                    viewModel.screenTime = it.interval
                    setScreenTime()
                }
                is QueryCallback.DoNotDisturbObtained -> {
                    binding.switchCompat.isChecked = it.doNotDisturb.status
                    viewModel.setLoading(false)
                }
                is QueryCallback.BrightnessLevelObtained -> {
                    LOGS.d("brightnress ${it.level}")
                    binding.progressBar.root.gone()
                    viewModel.brightnessLevel = it.level
                    setBrightness(it.level)
                }
                is QueryCallback.VibrationIntensityObtained -> {
                    LOGS.d("vibration ${it.vibrationIntensity.vibrationIntensityEnum}")
                    binding.progressBar.root.gone()
                    viewModel.vibrationIntensity =
                        it.vibrationIntensity.vibrationIntensityEnum.intensity
                    setVibrationIntensity()
                }
                is QueryCallback.WatchPasswordObtained -> {
                    binding.progressBar.root.gone()
                    viewModel.setPassword(wPassword = it.watchPassword)
                    setSwitchState(it.watchPassword.status)

                }
                else -> {}
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { event ->
                if (event is UpdateDeviceDataCallback.DeviceTimeSynced) {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_sync_device_time_done))
                    }
                } else if (event is UpdateDeviceDataCallback.BodyTempDataUpdated) {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_body_temp_unit_updated))
                    }
                } else if (event is UpdateDeviceDataCallback.LanguageUpdated) {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_language_updated))
                    }
                } else if (event is UpdateDeviceDataCallback.ScreenAwakeIntervalUpdated) {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.screen_time_updated))
                    }
                } else if (event is UpdateDeviceDataCallback.DoNotDisturbUpdated) {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_do_not_disturb_updated))
                    }
                } else if (event is UpdateDeviceDataCallback.BrightnessLevelUpdated) {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_brightness_level_updated))
                    }
                } else if (event is UpdateDeviceDataCallback.VibrationIntensityUpdated) {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_vibration_intensity_updated))
                    }
                } else if (event is UpdateDeviceDataCallback.WatchPasswordUpdated) {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast("watch password updated successfully")
                    }
                }
            }

        }
    }

    private fun setTempUnit() {
        binding.tvUnitValue.text = viewModel.getTemperatureUnit()
    }

    private fun setVibrationIntensity() {
        binding.tvVibrationValue.text = viewModel.vibrationIntensity
    }

    private fun setScreenTime() {

        val screenTime = "${viewModel.screenTime} sec"
        LOGS.d("getScreenAwakeInterval $screenTime")
        binding.tvScreenTimeValue.text = screenTime
    }

    private fun setLanguage() {
        LOGS.d("getLanguage ${viewModel.language}")
        binding.tvLanguageValue.text = viewModel.language
    }

    private fun setBrightness(progress: Int) {
        binding.lytProgress.pgBr.value = progress.toFloat()
    }


}