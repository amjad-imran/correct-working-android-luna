package com.noisefit.ui.dashboard.feature.healthMonitor.heartRate

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentEditHeartRateSettingsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint


const val EDIT_HR_REQUEST_KEY = "EDIT_HR_REQUEST_KEY"

@AndroidEntryPoint
class EditHeartRateSettingsFragment :
    BaseFragment<FragmentEditHeartRateSettingsBinding>(FragmentEditHeartRateSettingsBinding::inflate) {
    private val viewModel: HeartRateSettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.updateHRAlert(EditHeartRateSettingsFragmentArgs.fromBundle(it).heartRateAlert)
            viewModel.updateHRInterval(EditHeartRateSettingsFragmentArgs.fromBundle(it).heartRateInterval)
        }
        updateAllValues()
    }

    override fun initListener() {
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnAllow.setOnClickListener {
            updateHeartRateInterval()
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.heart_rate)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.lytHeartRateSettings.tvFreqData.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                viewModel.frequency = try {
                    selectedValue!!.split(" ")[0].toInt()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    1
                }
                updateFreq()

            }
            navigate(
                EditHeartRateSettingsFragmentDirections.actionEditHeartRateSettingsFragmentToValueSelectorBottomSheet(
                    viewModel.getFreqValue(),
                    viewModel.freqValueList!!,
                    getString(R.string.text_freqency)
                )
            )

        }

        binding.lytHeartRateSettings.tvLowHrData.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                viewModel.lowHrValue = try {
                    selectedValue!!.split(" ")[0].toInt()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    1
                }
                lowHr()

            }
            navigate(
                EditHeartRateSettingsFragmentDirections.actionEditHeartRateSettingsFragmentToValueSelectorBottomSheet(
                    viewModel.getLowHrValue(),
                    viewModel.lowHrValueList!!,
                    getString(R.string.text_low_hr_limit)
                )
            )

        }

        binding.lytHeartRateSettings.tvHighHrData.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                viewModel.highHrValue = try {
                    selectedValue!!.split(" ")[0].toInt()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    1
                }
                highHr()

            }
            navigate(
                EditHeartRateSettingsFragmentDirections.actionEditHeartRateSettingsFragmentToValueSelectorBottomSheet(
                    viewModel.getHighHrValue(),
                    viewModel.highHrValueList!!,
                    getString(R.string.text_high_hr_limit)
                )
            )

        }



        binding.lytHeartRateSettings.tvStartTimeData.setOnClickListener {
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        viewModel.endHour,
                        viewModel.endMinute
                    ) <= 0
                ) {
                    viewModel.startHour = hourOfDay
                    viewModel.startMinute = minute
                    setStartTime()
                } else {
                    context.showShortToast(getString(R.string.text_start_time_less))
                }

            }


            navigate(
                EditHeartRateSettingsFragmentDirections.actionEditHeartRateSettingsFragmentToTimeBottomSheet(
                    viewModel.startHour,
                    viewModel.startMinute,
                    1,
                    getString(R.string.text_start_time)
                )
            )
        }

        binding.lytHeartRateSettings.tvEndTimeData.setOnClickListener {
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        viewModel.startHour,
                        viewModel.startMinute
                    ) >= 0
                ) {
                    viewModel.endHour = hourOfDay
                    viewModel.endMinute = minute
                    setEndTime()
                } else {
                    context.showShortToast(getString(R.string.text_end_time_greater))
                }


            }


            navigate(
                EditHeartRateSettingsFragmentDirections.actionEditHeartRateSettingsFragmentToTimeBottomSheet(
                    viewModel.endHour,
                    viewModel.endMinute,
                    1,
                    getString(R.string.text_end_time)
                )
            )
        }


    }

    private fun updateAllValues() {
        setEndTime()
        setStartTime()
        updateFreq()
        lowHr()
        highHr()
    }

    private fun setStartTime() {
        binding.lytHeartRateSettings.tvStartTimeData.text =
            DateFormats.formatTimeWithAmPm(viewModel.startHour, viewModel.startMinute)

    }

    private fun setEndTime() {
        binding.lytHeartRateSettings.tvEndTimeData.text =
            DateFormats.formatTimeWithAmPm(viewModel.endHour, viewModel.endMinute)

    }

    private fun updateFreq() {
        binding.lytHeartRateSettings.tvFreqData.text =
            viewModel.getFreqValue()
    }

    private fun lowHr() {
        binding.lytHeartRateSettings.tvLowHrData.text =
            viewModel.getLowHrValue()
    }

    private fun highHr() {
        binding.lytHeartRateSettings.tvHighHrData.text =
            viewModel.getHighHrValue()
    }


    private fun updateHeartRateInterval() {

        viewModel.setLoading(true)
        val interval = HeartRateInterval(
            status = true,
            status2 = true,
            startTime = "${viewModel.startHour}:${viewModel.startMinute}",
            endTime = "${viewModel.endHour}:${viewModel.endMinute}",
            interval = viewModel.frequency,
        )
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetHeartRateInterval(
                interval
            )
        )

        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetHeartRateAlert(
                HeartRateAlert(
                    status = true,
                    min_hr = viewModel.lowHrValue,
                    max_hr = viewModel.highHrValue
                )
            )
        )
    }


    override fun subscribeObservers() {

        viewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {

                is QueryCallback.HeartRateIntervalObtained -> {
                    binding.progressBar.root.gone()
                    viewModel.updateHRInterval(it.interval)
                    updateAllValues()
                }
                is QueryCallback.HeartRateAlertDataObtained -> {
                    binding.progressBar.root.gone()
                    viewModel.updateHRAlert(it.heartRateAlert)
                    lowHr()
                    highHr()

                }
                else -> {}
            }
        }



        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }


        viewModel.showStartTimeLayout.observe(this) {
            if (it == true) {
                binding.lytHeartRateSettings.tvStartTimeData.visible()
                binding.lytHeartRateSettings.tvStartTimeText.visible()
            } else {
                binding.lytHeartRateSettings.tvStartTimeData.gone()
                binding.lytHeartRateSettings.tvStartTimeText.gone()
            }
        }

        viewModel.showEndTimeLayout.observe(this) {
            if (it == true) {
                binding.lytHeartRateSettings.tvEndTimeData.visible()
                binding.lytHeartRateSettings.tvEndTimeText.visible()
            } else {
                binding.lytHeartRateSettings.tvEndTimeData.gone()
                binding.lytHeartRateSettings.tvEndTimeText.gone()
            }
        }

        viewModel.showHrSettingLayout.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.lytHeartRateSettings.root.visible()
            } else {
                binding.lytHeartRateSettings.root.gone()
            }
        }



        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            it.getContent()?.let { callBack ->
                when (callBack) {
                    is UpdateDeviceDataCallback.HeartRateMeasureIntervalSet -> {
                        binding.progressBar.root.gone()
                        if (callBack.success) {
                            setFragmentResult(
                                EDIT_HR_REQUEST_KEY,
                                bundleOf(
                                    "hrInterval" to viewModel.getHeartRateInterval(),
                                    "hrAlert" to viewModel.getHeartRateAlert()
                                )
                            )
                            navigateUpSafe()
                            context.showShortToast(getString(R.string.text_auto_hr_success))
                        } else {
                            context.showShortToast(getString(R.string.text_auto_hr_failed))
                        }
                    }
                    is UpdateDeviceDataCallback.HeartRateAlertUpdated -> {
                        binding.progressBar.root.gone()
                        if (callBack.success) {
                            context.showShortToast(getString(R.string.text_auto_hr_success))
                        } else {
                            context.showShortToast(getString(R.string.text_auto_hr_failed))
                        }
                    }

                    else -> {}
                }
            }
        }

    }
}

