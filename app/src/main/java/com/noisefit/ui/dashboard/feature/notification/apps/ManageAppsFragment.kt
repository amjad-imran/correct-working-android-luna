package com.noisefit.ui.dashboard.feature.notification.apps


import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit_commans.data.model.NotificationApp
import com.noisefit.luna.databinding.FragmentManageAppsBinding
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint


const val ADD_NOTIFICATION_KEY = "ADD_NOTIFICATION_KEY"

@AndroidEntryPoint
class ManageAppsFragment :
    BaseFragment<FragmentManageAppsBinding>(FragmentManageAppsBinding::inflate), ManageAppListener {

    private val viewModel: ManageAppsViewModel by viewModels()
    private val manageAdapter by lazy { ManageAppAdapter(this) }


    private fun setRecycler() {

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = manageAdapter
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val notificationList =
                ArrayList(ManageAppsFragmentArgs.fromBundle(it).selectedNotificationList.toList())
            viewModel.getInstalledApps(notificationList, false)
        }

        binding.lytRecommended.tvTabTitle.text = getString(R.string.text_recommended)
        binding.lytAll.tvTabTitle.text = getString(R.string.text_all)
        binding.lytSelected.tvTabTitle.text = getString(R.string.text_selected)

        setRecycler()


        binding.searchView.isIconified = false

    }


    private fun setTabBackground(position: Int) {
        val intervals = arrayOf(binding.lytRecommended, binding.lytAll, binding.lytSelected)
        intervals.forEachIndexed { index, binding ->
            if (index == position) {
                binding.tvTabTitle.setTextColor(binding.tvTabTitle.context.getColor(R.color.accent_color_purple))
                binding.vBottom.visible()
            } else {
                binding.tvTabTitle.setTextColor(Color.parseColor("#a3ffffff"))
                binding.vBottom.invisible()
            }
        }
    }

    override fun initListener() {
        binding.lytRecommended.root.setOnClickListener {
            setTabBackground(0)
            loadNotificationList(0)
        }
        binding.lytAll.root.setOnClickListener {
            setTabBackground(1)
            loadNotificationList(1)
        }
        binding.lytSelected.root.setOnClickListener {
            setTabBackground(2)
            loadNotificationList(2)
        }
        binding.btnSaveChanges.setOnClickListener {
            setFragmentResult(
                ADD_NOTIFICATION_KEY,
                bundleOf("notifications" to viewModel.getSelectedNotificationList())
            )
            navigateUpSafe()
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.APP_NOTIFICATION_SAVE_CHANGES_CLICK)
        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                manageAdapter.filter.filter(newText)
                return false
            }
        })

        binding.searchBtn.setOnClickListener {
            binding.searchView.visible()
            binding.searchView.isIconified = false
            binding.tvManageApp.gone()
            binding.searchBtn.gone()
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.APP_NOTIFICATION_SEARCH_CLICK)
            //  uiController.showSoftKeyboard(null)
        }

        binding.searchView.setOnCloseListener {
            binding.searchView.gone()
            binding.tvManageApp.visible()
            binding.searchBtn.visible()
            uiController.hideSoftKeyboard()
            false
        }
    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.showTabView.observe(this) {
            if (it == true) {
                binding.layoutTabs.visible()
                setTabBackground(0)
            }
        }
        viewModel.allRecommendedNotificationList.observe(this) {
            if (!it.isNullOrEmpty()) {
                manageAdapter.setEditMode(true)
                setData(it)
            } else {
                manageAdapter.setEditMode(true)
            }
        }

    }

    private fun setData(dataList: ArrayList<NotificationApp>) {
        manageAdapter.setDataSet(dataList)
    }

    private fun loadNotificationList(type: Int) {
        viewModel.notificationListType = type
        when (type) {
            0 -> {
                viewModel.allRecommendedNotificationList.value?.let {
                    setData(it)
                }
            }

            1 -> {
                viewModel.allInstalledNotificationList.value?.let {
                    setData(it)
                }
            }

            2 -> {
                setData(viewModel.getSelectedNotificationList())
            }
        }
    }


    override fun onNotificationClick(
        notificationApp: NotificationApp,
        isChecked: Boolean,
        position: Int,
        checkBox: CheckBox
    ) {

        if (viewModel.notificationListType == 2) {
            viewModel.removeSelectedNotification(notificationApp)
            manageAdapter.removeItem(notificationApp)

        } else {
            if (isChecked) {
                viewModel.updateNotification(notificationApp)


            } else {

                if (notificationApp.appPackageName == BuildConfig.APPLICATION_ID) {
                    checkBox.isChecked = true
                    uiController.onApiErrorReceived(
                        ErrorResponse(
                            UIComponentType.AreYouSureDialog(
                                getString(R.string.text_alert),
                                getString(R.string.text_enabling_noisefit_helps_you_get_health),
                                false,
                                getString(R.string.text_continue),
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        checkBox.isChecked = false
                                        viewModel.removeNotification(notificationApp)
                                        manageAdapter.updateNotificationList(position, isChecked)
                                    }

                                    override fun no() {

                                    }

                                }
                            )
                        )
                    )
                } else {
                    viewModel.removeNotification(notificationApp)
                    manageAdapter.updateNotificationList(position, isChecked)
                }


            }


        }
    }
}