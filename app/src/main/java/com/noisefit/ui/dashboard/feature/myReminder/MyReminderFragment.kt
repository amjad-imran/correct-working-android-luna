package com.noisefit.ui.dashboard.feature.myReminder

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.databinding.FragmentMyReminderBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyReminderFragment :
    BaseFragment<FragmentMyReminderBinding>(FragmentMyReminderBinding::inflate), ReminderAction {

    private val viewModel: MyReminderViewModel by viewModels()

    private val maxCount = 5
    private val TAG = "MyReminderFragment"

    private val myReminderAdapter by lazy { MyReminderAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_REMINDER_PAGE_VISIT)

        setTextCount()
        setRecycler()
        if (!viewModel.syncWithDevice) {
            LOGS.d("$TAG init")
            getReminders()
        }

    }

    private fun getReminders() {
        viewModel.sessionManager.sendQueryAction(QueryAction.GetReminders)
        viewModel.setLoading(true)
    }

    override fun initListener() {

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnBottomAdd.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_REMINDER_ADD_REMINDER_CLICK)
            invokeAdd()
        }
        binding.tvEdit.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_REMINDER_EDIT_CLICK)
            viewModel.setEditMode(true)
        }
        binding.btnTopAdd.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_REMINDER_ADD_REMINDER_CLICK)
            invokeAdd()
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_REMINDER_ADD_REMINDER_SAVE_CLICK)
            viewModel.setEditMode(false)
            syncWithDevice()
        }

        setFragmentResultListener(REMINDER_REQUEST_KEY) { key, bundle ->
            val reminderReceived = bundle.getParcelable("reminder") as? ReminderList.Reminder
            if (reminderReceived != null) {
                LOGS.d("$TAG inside get reminder")
                viewModel.setEditMode(true)
                viewModel.updateData(reminderReceived)
            }

        }
    }

    private fun invokeAdd() {
        val count = viewModel.getReminderList().size

        if (count < 5) {

            viewModel.mSelectedPosition = -1
            navigate(
                MyReminderFragmentDirections.actionMyReminderFragmentToAddReminderFragment(
                    null,
                    viewModel.getMyReminder().toTypedArray()
                )
            )
        } else {
            val alertMessage = getString(R.string.text_max_reminder_message, 5)
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.InfoAlertDialog(
                        getString(
                            R.string.text_max_reminder_reached
                        ), alertMessage, getString(R.string.text_close)
                    )
                )
            )
        }


    }

    private fun syncWithDevice() {

        viewModel.sessionManager.connectedDevice.value?.deviceType?.let {
            if (!it.equals(DeviceType.COLORFIT_NAV.deviceType, true) &&
                !it.equals(DeviceType.COLORFIT_VISION.deviceType, true)
            ) {
                val list = ReminderList().apply {
                    this.reminders = viewModel.getReminderList()
                }
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.DeleteReminders(
                        list
                    )
                )
            }
        }


    }

    private fun setTextCount() {
        binding.textAddContact.text = getString(R.string.text_add_to_add_reminder)
        val countText = "(${myReminderAdapter.getCurrentDataSet().size}/$maxCount)"
        binding.tvContactCount.text = countText
        binding.tvContactAddedText.text =
            getString(R.string.text_after_setting_up_the_reminder_your_device_will, maxCount)
    }

    private fun setRecycler() {

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myReminderAdapter

        }

    }

    override fun subscribeObservers() {
        viewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.RemindersObtained -> {
                    viewModel.setLoading(false)
                    viewModel.syncWithDevice = true

                    if (it.reminderList.reminders.isNullOrEmpty()) {
                        viewModel.setReminderList(ArrayList())
                    } else {
                        viewModel.setReminderList(ArrayList(it.reminderList.reminders))
                    }
                }
                else -> {}
            }
        }



        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            it.getContent()?.let { callback ->
                if (callback is UpdateDeviceDataCallback.DeleteReminder) {
                    viewModel.setLoading(false)
                    updateContactCount()
                    if (callback.success) {
                        context.showShortToast(getString(R.string.text_reminder_updated))
                    } else {
                        context.showShortToast(getString(R.string.text_updated_failed))
                    }
                    viewModel.sessionManager.sendQueryAction(QueryAction.GetReminders)
                }
            }
        }

        viewModel.mReminderLiveData.observe(viewLifecycleOwner) {
            setContact(it)
            updateContactCount()
        }



        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.editMode.observe(viewLifecycleOwner) {
            if (it) {
                setStateEdit()
            } else {
                setStateDefault()
            }
        }
    }


    override fun onItemRemoved(position: Int, reminder: ReminderList.Reminder) {
        viewModel.setLoading(true)

        viewModel.sessionManager.connectedDevice.value?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_NAV.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_VISION.deviceType, true)
            ) {
                val deleteObj = viewModel.getReminderList()[position]
                val list = ReminderList().apply {
                    this.reminders = arrayListOf(deleteObj)
                }
                myReminderAdapter.removeItem(position)
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.DeleteReminders(
                        list
                    )
                )
            } else {
                myReminderAdapter.removeItem(position)
                syncWithDevice()
            }
        }


    }


    private fun setStateEdit() {
        /* binding.layoutToolbar.root.gone()
         binding.toolbarEdit.visible()
         binding.bEdit.gone()*/
        binding.tvEdit.gone()
        binding.btnSaveChanges.visible()
        myReminderAdapter.setEditMode(true)
    }

    private fun setStateDefault() {
        binding.tvEdit.visible()
        binding.btnSaveChanges.gone()
        /*binding.layoutToolbar.root.visible()
        binding.toolbarEdit.gone()
        binding.bEdit.visible()*/
        myReminderAdapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.textView18.gone()
        binding.tvContactCount.gone()
        binding.btnTopAdd.gone()
        binding.ivNoContact.visible()
        binding.textNoContact.visible()
        binding.textAddContact.visible()
        binding.btnBottomAdd.visible()
        binding.btnSaveChanges.gone()
        binding.tvEdit.gone()
    }

    private fun setStateHasData() {
        binding.textView18.visible()
        binding.tvContactCount.visible()
        binding.btnTopAdd.visible()
        binding.ivNoContact.gone()
        binding.textNoContact.gone()
        binding.textAddContact.gone()
        binding.btnBottomAdd.gone()
        if (viewModel.editMode.value != true) {
            binding.tvEdit.visible()
        }else{
            binding.btnSaveChanges.visible()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateContactCount() {
        val dataCount = viewModel.getReminderList().size
//        val dataCount = myReminderAdapter.itemCount
        binding.tvContactCount.text = "($dataCount/$maxCount)"

        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }


    private fun setContact(data: List<ReminderList.Reminder>) {
        myReminderAdapter.setDataSet(data)

    }

    override fun onItemClicked(position: Int, reminder: ReminderList.Reminder) {
        viewModel.mSelectedPosition = position
        navigate(
            MyReminderFragmentDirections.actionMyReminderFragmentToAddReminderFragment(
                reminder,
                viewModel.getMyReminder().toTypedArray()
            )
        )
    }
}