package com.noisefit.ui.dashboard.feature.wristsense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import com.noisefit.R
import com.noisefit.databinding.FragmentWristSenseBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.WristLiftGesture
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WristSenseFragment :
    BaseFragment<FragmentWristSenseBinding>(FragmentWristSenseBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager
    private var mode: WristMode? = null
    private var startHour = 7
    private var startMinute = 0
    private var endHour = 23
    private var endMinute = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            mode = WristSenseFragmentArgs.fromBundle(it).mode
        }
        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_wrist_sense)
            tvTitle.text = getString(R.string.text_wrist_sense)
            tvTitleDisc.text = getString(R.string.text_not_receiving_alerts)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))

        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_wrist_sense)
//            tvDesc.gone()
              tvDesc.text = getString(R.string.text_wrist_sence_description)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.lytFeatureTile.imvIcon.setImageResource(R.drawable.ic_device_wrist_sense)

        sessionManager.sendQueryAction(QueryAction.GetWristLiftGesture)
        binding.progressBar.root.visible()
    }

    private fun handleUi() {

        if (mode == WristMode.SHOW_TIME) {
            if (binding.lytFeatureTile.llSwitch.isChecked) {
                binding.apply {
                    tvEndTimeData.visible()
                    tvEndTimeText.visible()
                    tvStartTimeData.visible()
                    tvStartTimeText.visible()
                    setTimeLayout()
                }
            } else {
                binding.apply {
                    tvEndTimeData.gone()
                    tvEndTimeText.gone()
                    tvStartTimeData.gone()
                    tvStartTimeText.gone()
                }
            }

        }
    }


    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            startHour,
            startMinute
        )
        binding.tvStartTimeData.text = startTime
    }

    private fun setEndTimeBetween() {
        val endTime = DateFormats.formatTimeWithAmPm(
            endHour,
            endMinute
        )
        binding.tvEndTimeData.text = endTime
    }

    override fun initListener() {

        binding.lytToolbarWithDetails.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvEndTimeData.setOnClickListener {
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        startHour,
                        startMinute
                    ) <= 0
                ) {
                    context.showShortToast(getString(R.string.text_end_time_greater))
                } else if (hourOfDay == startHour && minute == startMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else {
                    endHour = hourOfDay
                    endMinute = minute
                    setEndTimeBetween()
                    updateWristSense()
                }
            }



            navigate(
                WristSenseFragmentDirections.actionWristSenseFragmentToTimeBottomSheet(
                    endHour,
                    endMinute,
                    1,
                    getString(R.string.text_end_time)
                )
            )

        }

        binding.tvStartTimeData.setOnClickListener {
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        endHour,
                        endMinute
                    ) >= 0
                ) {
                    context.showShortToast(getString(R.string.text_start_time_less))
                } else if (hourOfDay == endHour && minute == endMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else {
                    startHour = hourOfDay
                    startMinute = minute
                    setStartTimeBetween()
                    updateWristSense()
                }


            }


            navigate(
                WristSenseFragmentDirections.actionWristSenseFragmentToTimeBottomSheet(
                    startHour,
                    startMinute,
                    1,
                    getString(R.string.text_start_time)
                )
            )
        }




        binding.lytFeatureTile.llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                updateWristSense()
                handleUi()
            }
        }
    }

    private fun updateWristSense() {
        binding.progressBar.root.visible()
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetWristLiftGesture(
                WristLiftGesture(
                    status = binding.lytFeatureTile.llSwitch.isChecked,
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute
                )
            )
        )
    }

    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.WristLiftGestureObtained -> {
                    binding.lytFeatureTile.llSwitch.isChecked = it.wristLiftGesture.status
                    binding.progressBar.root.gone()
                    startHour = it.wristLiftGesture.startHour
                    startMinute = it.wristLiftGesture.startMinute
                    endHour = it.wristLiftGesture.endHour
                    endMinute = it.wristLiftGesture.endMinute
                    setTimeLayout()
                    handleUi()
                }
                else -> {}
            }
        }

        sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            it.getContent()?.let { event ->
                if (event is UpdateDeviceDataCallback.WristLiftGestureUpdated) {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_wrist_sense_updated))
                    }
                }
            }
        }
    }

    private fun setTimeLayout() {
        setStartTimeBetween()
        setEndTimeBetween()
    }


}

enum class WristMode {
    SHOW_TIME,
    NONE
}