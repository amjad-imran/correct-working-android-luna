package com.noisefit.ui.dashboard.feature.notification.apps

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.model.NotificationApp
import com.noisefit.luna.databinding.FragmentAppNotificationBinding
import com.noisefit.receiver.service.NotificationAlertService
import com.noisefit.ui.common.*
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppNotificationFragment :
    BaseFragment<FragmentAppNotificationBinding>(FragmentAppNotificationBinding::inflate),
    ManageAppListener {

    private val viewModel: ManageAppsViewModel by viewModels()
    private val manageAdapter by lazy { ManageAppAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_notification_bell_white)
            tvTitle.text = getString(R.string.text_app_notifications)
            tvTitleDisc.gone()
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                val localDataStore = viewModel.getLocalDataStore()
                localDataStore.setNotificationAlertStatus(status = isChecked)
                if (isChecked) {
                    viewModel.addDefaultRecommended()
                    binding.btnTopManageApp.enable()
                    binding.btnManageApp.enable()
                    ApplicationUtils.startNotificationListenerService(localDataStore, requireContext())
                    viewModel.sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.APP_NOTIFICATION_CLICK,
                        HashMap<String, Any>().apply {
                            this["is_enabled"] = isChecked

                        })
                } else {
                    viewModel.sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.APP_NOTIFICATION_CLICK,
                        HashMap<String, Any>().apply {
                            this["is_enabled"] = isChecked

                        })
                    binding.btnTopManageApp.disable()
                    binding.btnManageApp.disable()
                    context?.let {
                        try {
                            val pm = it.packageManager
                            pm.setComponentEnabledSetting(
                                ComponentName(
                                    it,
                                    NotificationAlertService::class.java
                                ),
                                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                                PackageManager.DONT_KILL_APP
                            )
                        } catch (exp: Exception) {
                            exp.printStackTrace()
                        }
                    }
                }

            }
        }

        setRecycler()
        if (viewModel.manageNotificationList.value.isNullOrEmpty()) {
            viewModel.feedManageNotification()
        }

        setSwitchState(viewModel.getLocalDataStore().isNotificationAlertEnabled())

    }



    private fun setSwitchState(isChecked: Boolean) {

        binding.lytFeatureTile.llSwitch.isChecked = isChecked
        if (!isChecked) {
            binding.btnTopManageApp.disable()
            binding.btnManageApp.disable()
        }
    }

    override fun initListener() {

//        binding.lytFeatureTile.tvTitleDisc.setOnClickListener {
//            navigate(AppNotificationFragmentDirections.actionAppNotificationFragmentToSmartNotificationFragment())
//        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnManageApp.setOnClickListener {
            invokeAddNotifications()
        }

        binding.btnTopManageApp.setOnClickListener {
            invokeAddNotifications()
        }

        binding.btnSaveChanges.setOnClickListener {


        }
    }

    private fun setRecycler() {

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = manageAdapter
        }

    }

    private fun invokeAddNotifications() {

        setFragmentResultListener(ADD_NOTIFICATION_KEY) { _, bundle ->
            val contactList = bundle.getParcelableArrayList<NotificationApp>("notifications")
            contactList?.let {
                viewModel.setManageAppList(it)
                viewModel.getLocalDataStore().saveNotificationAppList(it)
            }
        }
        navigate(
            AppNotificationFragmentDirections
                .actionAppNotificationFragmentToManageAppsFragment(
                    viewModel.getManageAppList().toTypedArray()
                )
        )
    }

    override fun subscribeObservers() {
        viewModel.manageNotificationList.observe(this) {
            setContact(it)
            updateContactCount()

        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }


    private fun setStateEdit() {
        /* binding.layoutToolbar.root.gone()
         binding.toolbarEdit.visible()
         binding.bEdit.gone()*/

        binding.btnSaveChanges.visible()
        manageAdapter.setEditMode(true)
    }

    private fun setStateDefault() {

        binding.btnSaveChanges.gone()
        /*binding.layoutToolbar.root.visible()
        binding.toolbarEdit.gone()
        binding.bEdit.visible()*/
        manageAdapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.textView18.gone()
        binding.tvContactCount.gone()
        binding.btnTopManageApp.gone()
        binding.ivNoContact.visible()
        binding.textNoContact.visible()
        binding.textAddContact.visible()
        binding.btnManageApp.visible()
    }

    private fun setStateHasData() {
        binding.textView18.visible()
        binding.tvContactCount.visible()
        binding.btnTopManageApp.visible()
        binding.ivNoContact.gone()
        binding.textNoContact.gone()
        binding.textAddContact.gone()
        binding.btnManageApp.gone()

    }


    private fun updateContactCount() {
        val dataCount = manageAdapter.itemCount

        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }


    private fun setContact(data: List<NotificationApp>) {
        manageAdapter.setDataSet(ArrayList(data))
//        if (viewModel.syncWithDevice) {
//            syncContactWithDevice()
//        }
    }

    override fun onNotificationClick(
        notificationApp: NotificationApp,
        isChecked: Boolean,
        position: Int,
        checkBox: CheckBox
    ) {

    }


}