package com.noisefit.ui.dashboard.feature.spo2

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentSpo2MeasurementBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Spo2MeasurementFragment :
    BaseFragment<FragmentSpo2MeasurementBinding>(FragmentSpo2MeasurementBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager
    private val viewModel: Spo2MeasurementViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_spo2_measurement)
            tvDesc.gone()
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }


    fun onBackPressed() {
        if (binding.bUpdate.text == getString(R.string.text_stop_measurement)) {
            showCancelDialog()
            return
        }
        navigateUpSafe()
    }

    private fun showCancelDialog() {
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.AreYouSureDialog(
                    getString(
                        R.string.text_blood_oxygen
                    ), getString(
                        R.string.text_cancel_blood_oxygen_measurement
                    ), false,
                    getString(R.string.text_yes),
                    object : BinaryActionCallback {
                        override fun yes() {
                            sessionManager.sendUserActivityAction(
                                UserActivityAction.SetSpo2Measurement(
                                    false
                                )
                            )
                            navigateUpSafe()
                        }

                        override fun no() {}
                    })
            )
        )
    }

    override fun initListener() {
        binding.bUpdate.setOnClickListener {
            if (binding.bUpdate.text == getString(R.string.text_start_measurement)) {
                binding.bUpdate.text = getString(R.string.text_stop_measurement)
                binding.messageTv.visible()
                updateBloodOxygenValue("-")
                sessionManager.sendUserActivityAction(UserActivityAction.SetSpo2Measurement(true))
            } else {
                sessionManager.sendQueryAction(QueryAction.GetWristLiftGesture)
                binding.bUpdate.text = getString(R.string.text_start_measurement)
                binding.messageTv.gone()
                sessionManager.sendUserActivityAction(UserActivityAction.SetSpo2Measurement(false))
            }
        }
    }

    private fun updateBloodOxygenValue(value: String) {
        val bloodOxygenValue = "$value %"
        binding.tvValue.text = bloodOxygenValue
    }

    private fun updateBloodOxygen(value: Int?) {
        val bloodOxygen = if (value == null || value <= 0) {
            "-"
        } else {
            value.toString()
        }
        updateBloodOxygenValue(bloodOxygen)
    }

    override fun subscribeObservers() {
        viewModel.spo2Value.observe(this) {
            if (it != null) {
                updateBloodOxygen(it)
            }
        }
        sessionManager.userActivityCallback.observe(this) { event ->
            event.getContent()?.let {
                when (it) {
                    is UserActivityCallback.BloodOxygenObtained -> {
                        viewModel.saveOfflineDb(
                            it.bloodOxygen
                        )
                        binding.messageTv.gone()
                        binding.bUpdate.text = getString(R.string.text_start_measurement)

                        if (!it.bloodOxygen.isEmpty()) {
                            updateBloodOxygen(it.bloodOxygen[it.bloodOxygen.size - 1].value)
                        }
                        LOGS.d("BloodOxygenObtained : " + it.bloodOxygen)
                    }
                    else -> {}
                }
            }
        }

//        viewModel.summary.userActivities.observe(viewLifecycleOwner) { userActivities ->
//            userActivities?.boData?.let {
//
//            }
//        }
    }

}