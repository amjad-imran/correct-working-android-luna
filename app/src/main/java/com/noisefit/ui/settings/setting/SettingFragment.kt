package com.noisefit.ui.settings.setting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSettingBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {

    private val viewModel: SettingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDeviceUnit()
        setLastSyncTime()
    }

    private fun setLastSyncTime() {
        val lastSync = viewModel.sessionManager.getLastSyncTime()?.let { it1 ->
            DateFormats.convertTimestampToDate(
                it1, SimpleDateFormat("h:mm a, dd MMM yyyy", DateFormats.defaultLocale)
            )
        }

        binding.tvDateSyncValue.text = lastSync
    }

    private fun syncData() {
        scope.launch {
            viewModel.sessionManager.forceSyncDataWithServer = true
            ApplicationUtils.startSyncScheduler(requireContext())

        }
    }

    private fun setDeviceUnit() {
        binding.tvUnitValue.text = viewModel.getSelectedUnit()
    }

    override fun initListener() {
        binding.layoutToolbar.tvTitle.text = getString(R.string.text_settings)
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnManualSync.setOnClickListener {
            syncData()
        }

        binding.tvUnitValue.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    viewModel.setSelectedUnit(it1)
                    setDeviceUnit()
                }

            }
            navigate(
                SettingFragmentDirections.actionSettingFragmentToValueSelectorBottomSheet(
                    viewModel.getSelectedUnit(),
                    viewModel.unitList.toTypedArray(),
                    getString(R.string.unit)
                )
            )
        }
    }


    override fun subscribeObservers() {
        viewModel.sessionManager.syncCompleted.observe(viewLifecycleOwner) {
            it?.getContent()?.let { syncDataStatus ->
                when (syncDataStatus.status) {
                    EventConstants.UPDATE_STATUS_SUCCESS -> {
                        setLastSyncTime()
                    }
                    EventConstants.UPDATE_STATUS_FAILED -> {
//                        showSyncingText(true, 0)
                    }
                    EventConstants.UPDATE_STATUS_STARTED -> {
//                        showSyncingText(false, 0)
                    }
                }
            }
        }
    }


}