package com.noisefit.ui.dashboard.feature.alarm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.databinding.FragmentMyAlarmBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.SDKWatchType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.AlarmAction
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import com.noisefit_commans.ui.BaseFragment

private const val TAG = "MyAlarmFragment"

@AndroidEntryPoint
class MyAlarmFragment :
    BaseFragment<FragmentMyAlarmBinding>(FragmentMyAlarmBinding::inflate), AlarmRowAction {

    private val viewModel: MyAlarmViewModel by viewModels()


    private val maxCount: Int by lazy {
        AppStaticData.getMaxAlarmCount(viewModel.getConnectedDevice())
    }
    private val myAlarmAdapter by lazy { MyAlarmAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTextCount()
        setRecycler()
        if (!viewModel.syncWithDevice) {
            LOGS.d("$TAG init")
            viewModel.sessionManager.sendQueryAction(QueryAction.GetAlarms)
            viewModel.setLoading(true)
        } else {
            viewModel.setLoading(false)
        }

    }


    override fun initListener() {

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAddStockBottom.setOnClickListener {
            invokeAdd()
        }
        binding.tvEdit.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_ALARMS_EDIT_CLICK)
            viewModel.setEditMode(true)
        }
        binding.btnAddContact.setOnClickListener {
            invokeAdd()
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.setEditMode(false)
            syncWithDevice()
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_ALARMS_ADD_ALARMS_SAVE_CLICK)
        }

        setFragmentResultListener(ALARM_KEY) { _, bundle ->
            val alarmReceived = bundle.getSerializable("alarm") as AlarmsList.Alarm

            if (alarmReceived != null) {
                setStateHasData()
                viewModel.setEditMode(true)
                viewModel.updateData(alarmReceived)
            }

        }
    }

    private fun invokeAdd() {
        val count = viewModel.getAlarmList().size

        if (findNavController().currentDestination?.id != R.id.myAlarmFragment) {
            return
        }
        if (count < maxCount) {
            viewModel.mSelectedPosition = -1

            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_ALARMS_ADD_REMINDER_CLICK)
            navigate(
                MyAlarmFragmentDirections.actionAlarmsFragmentToBottomSheetCreateAlarm(
                    null
                )
            )

        } else {
            val alertMessage = getString(R.string.text_max_alarm_message, maxCount)
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.InfoAlertDialog(
                        getString(
                            R.string.text_max_alarm_reached
                        ), alertMessage, getString(R.string.text_close)
                    )
                )
            )
        }

    }

    private fun syncWithDevice() {
        val list = AlarmsList().apply {
            this.alarms = viewModel.getAlarmList()
        }
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.UpdateAlarm(
                list,
                AlarmAction.ALARM_ADD
            )
        )
    }

    private fun setTextCount() {
        binding.textAddContact.text = getString(R.string.text_add_to_add_alarm)
        val countText = "(${myAlarmAdapter.getCurrentDataSet().size}/$maxCount)"
        binding.tvContactCount.text = countText
        binding.tvContactAddedText.text =
            getString(R.string.text_after_setting_up_the_alarm_your_device_will_vibrate, maxCount)
    }

    private fun setRecycler() {

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAlarmAdapter
        }


        if (viewModel.watchesSDK.getWatchType(viewModel.getConnectedDevice()) == SDKWatchType.SDK_QUBE) {
            binding.tvEdit.gone()
            binding.btnAddStockBottom.gone()
            binding.btnAddContact.gone()
        }

    }

    override fun subscribeObservers() {
        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.AlarmsObtained -> {
                    viewModel.setLoading(false)
                    viewModel.syncWithDevice = true

                    if (it.alarmsList.alarms.isNullOrEmpty()) {
                        viewModel.setAlarmList(ArrayList())
                    } else {
                        it.alarmsList.alarms?.let { it1 -> ArrayList(it1) }
                            ?.let { it2 -> viewModel.setAlarmList(it2) }
                    }
                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { callback ->
                if (callback is UpdateDeviceDataCallback.AlarmUpdated) {
                    viewModel.setLoading(false)
                    updateContactCount()
                    if (callback.success) {
                        context.showShortToast(getString(R.string.text_alarm_updated))
                    } else {
                        context.showShortToast(getString(R.string.text_updated_failed))
                    }
                    //viewModel.sessionManager.sendQueryAction(QueryAction.GetAlarms)
                }
            }
        }

        viewModel.mAlarmLiveData.observe(this) {
            setData(it)
            updateContactCount()
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
        viewModel.editMode.observe(this) {
            if (it) {
                setStateEdit()
            } else {
                setStateDefault()
            }
        }
    }


    override fun onItemRemoved(position: Int, data: AlarmsList.Alarm) {
        viewModel.setLoading(true)

        when (viewModel.sessionManager.connectedDevice.value?.deviceType!!) {
            DeviceType.COLORFIT_NAV.deviceType,
            DeviceType.COLORFIT_VISION.deviceType,
            DeviceType.NOISE_EVOLVE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType,
            DeviceType.NOISE_EVOLVE_2_PLAY.deviceType,
            DeviceType.NOISEFIT_HYBRID.deviceType -> {
                val deleteObj = viewModel.getAlarmList()[position]
                val list = AlarmsList().apply {
                    this.alarms = arrayListOf(deleteObj)
                }
                myAlarmAdapter.removeItem(position)
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateAlarm(
                        list,
                        AlarmAction.ALARM_DELETE
                    )
                )

            }
            else -> {
                myAlarmAdapter.removeItem(position)
                val list = AlarmsList().apply {
                    this.alarms = viewModel.getAlarmList()
                }
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateAlarm(
                        list,
                        AlarmAction.ALARM_DELETE
                    )
                )
            }
        }

    }


    private fun setStateEdit() {
        /* binding.layoutToolbar.root.gone()
         binding.toolbarEdit.visible()
         binding.bEdit.gone()*/
        binding.tvEdit.gone()
        binding.btnSaveChanges.visible()
        myAlarmAdapter.setEditMode(true)
    }

    private fun setStateDefault() {
        if (viewModel.disableDeleteOption.value == false) {
            binding.tvEdit.visible()
        }

        binding.btnSaveChanges.gone()
        /*binding.layoutToolbar.root.visible()
        binding.toolbarEdit.gone()
        binding.bEdit.visible()*/
        myAlarmAdapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.textView18.gone()
        binding.tvContactCount.gone()
        binding.btnAddContact.gone()
        binding.ivNoContact.visible()
        binding.textNoContact.visible()
        binding.textAddContact.visible()
        binding.btnSaveChanges.gone()
        binding.btnAddStockBottom.visible()
        binding.tvEdit.gone()
    }

    private fun setStateHasData() {
        binding.textView18.visible()
        binding.tvContactCount.visible()
        if (viewModel.disableDeleteOption.value == false) {
            binding.btnAddContact.visible()
        }

        binding.ivNoContact.gone()
        binding.textNoContact.gone()
        binding.textAddContact.gone()
        binding.btnAddStockBottom.gone()
        if (viewModel.editMode.value != true && viewModel.disableDeleteOption.value == false) {
            binding.tvEdit.visible()
        }
    }


    private fun updateContactCount() {
        val dataCount = viewModel.getAlarmList().size
        val total = "($dataCount/$maxCount)"
        binding.tvContactCount.text = total

        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }


    private fun setData(data: List<AlarmsList.Alarm>) {
        myAlarmAdapter.setDataSet(data, viewModel.disableDeleteOption.value ?: false)

    }

    override fun onSwitchClicked(state: Boolean, data: AlarmsList.Alarm) {
        val alarmList = AlarmsList().apply {
            myAlarmAdapter.getCurrentDataSet().also { this.alarms = it }
        }
        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.UpdateAlarm(
                alarmList,
                AlarmAction.ALARM_CHANGE
            )
        )
        if (state)
            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.MY_ALARM_ENABLED_CLICK,
                HashMap<String, Any>().apply
                {
                    this["is_enabled"] = true
                })
        else
            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.MY_ALARM_ENABLED_CLICK,
                HashMap<String, Any>().apply
                {
                    this["is_enabled"] = false
                })

    }

    override fun onItemClicked(position: Int, data: AlarmsList.Alarm) {
        viewModel.mSelectedPosition = position
        navigate(
            MyAlarmFragmentDirections.actionAlarmsFragmentToBottomSheetCreateAlarm(
                viewModel.getAlarmList()[position]
            )
        )
    }


}