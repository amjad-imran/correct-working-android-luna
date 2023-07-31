package com.noisefit.ui.dashboard.feature.appList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.databinding.FragmentAppListSelectionBinding
import com.noisefit.databinding.FragmentWidgetSelectionBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.feature.sportSelection.ADD_SPORT_REQUEST_KEY
import com.noisefit.ui.dashboard.feature.widget.WidgetSelectionAdapter
import com.noisefit.ui.dashboard.feature.widget.WidgetSelectionFragmentDirections
import com.noisefit.ui.dashboard.feature.widget.WidgetSelectionListener
import com.noisefit.ui.dashboard.feature.widget.WidgetSelectionViewModel
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.Widget
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AppListSelectionFragment"

@AndroidEntryPoint
class AppListSelectionFragment :
    BaseFragment<FragmentAppListSelectionBinding>(FragmentAppListSelectionBinding::inflate),
    AppListSelectionListener {


    private val viewModel: AppListSelectionViewModel by viewModels()
    lateinit var touchHelper: ItemTouchHelper
    private val adapter by lazy { AppListSelectionAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCount()
        setRecycler()
        if (!viewModel.fetchData) {
            LOGS.d("$TAG inside")
            viewModel.sessionManager.sendQueryAction(QueryAction.GetApplicationList)
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
            viewModel.setEditMode(true)
        }
        binding.btnAddContact.setOnClickListener {
            invokeAdd()
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.setEditMode(false)
            syncDataWithDevice()
        }
    }

    private fun showMaxDialog(alertMessage: String, title: String) {
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.InfoAlertDialog(
                    title, alertMessage, getString(R.string.text_close)
                )
            )
        )

    }

    private fun invokeAdd() {
        val count = viewModel.getSelectedWidgetSort().size

        if (count >= viewModel.maxCount) {
            showMaxDialog(
                getString(R.string.text_max_app_message, viewModel.maxCount),
                getString(R.string.text_max_app_reached)
            )

            return
        }


        setFragmentResultListener(ADD_SPORT_REQUEST_KEY) { _, bundle ->
            val allSports = bundle.getParcelableArrayList<Widget>("allSports")
            allSports?.let {
                viewModel.setEditMode(true)
                viewModel.setWidgetList(allSports)
            }
        }

        navigate(
            AppListSelectionFragmentDirections.actionAppListSelectionFragmentToAddAppListSelectionFragment(
                viewModel.getAllWidgetSortList().toTypedArray()
            )
        )
    }

    private fun syncDataWithDevice() {
        viewModel.setLoading(true)

        val sportList = viewModel.getSyncWidgetList()

        sportList.forEach {
            LOGS.d("$TAG 12 $it")
        }

        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.UpdateApplicationList(
                sportList
            )
        )

    }

    private fun setCount() {
        binding.textAddContact.text = getString(R.string.text_add_to_add_app, viewModel.maxCount)
//        val countText = "(${adapter.getCurrentDataSet().size}/${viewModel.maxCount})"
//        binding.tvContactCount.gone()

    }

    private fun setRecycler() {
        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter)
        touchHelper = ItemTouchHelper(callback)
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            touchHelper.attachToRecyclerView(this)
        }
        binding.rv.adapter = adapter
    }

    override fun subscribeObservers() {



        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.AppListObtained -> {
                    viewModel.fetchData = true
                    viewModel.setLoading(false)
                    viewModel.setWidgetList(ArrayList(it.appList))

                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.UpdateAppList -> {
                        binding.progressBar.root.gone()
                        if (value.success) {
                            updateWidgetCount()
                            context.showShortToast(getString(R.string.text_app_update_success))
                            // viewModel.sessionManager.sendQueryAction(QueryAction.GetSportModeInfo)
                        }
                    }
                    else -> {}
                }
            }
        }

        viewModel.widgetSortList.observe(this) {
            setWidgets(it)
            updateWidgetCount()
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

    override fun onLongPressed(position: Int) {
        setStateEdit()
    }

    override fun onItemRemoved(position: Int, widget: Widget) {
        //LOGS.d("onItemRemoved ${adapter.itemCount} --  ${viewModel.selectedSports.value?.size}")

        if (adapter.itemCount == 3) {
            showMaxDialog(
                getString(R.string.text_atleast_one_app_required), getString(R.string.text_alert)
            )
            return
        }

        viewModel.removeSelectedFromAllWidgetSort(widget)
        adapter.removeItem(position)
        updateWidgetCount()
    }


    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    private fun setStateEdit() {
        /* binding.layoutToolbar.root.gone()
         binding.toolbarEdit.visible()
         binding.bEdit.gone()*/
        binding.tvEdit.gone()
        binding.btnSaveChanges.visible()
        adapter.setEditMode(true)
    }

    private fun setStateDefault() {
        binding.tvEdit.visible()
        binding.btnSaveChanges.gone()
        /*binding.layoutToolbar.root.visible()
        binding.toolbarEdit.gone()
        binding.bEdit.visible()*/
        adapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.textView18.gone()
        binding.btnSaveChanges.gone()
//        binding.tvContactCount.gone()

        if(viewModel.disableEditMode.value != true){
            binding.btnAddContact.gone()
            binding.textAddContact.visible()
            binding.btnAddStockBottom.visible()

        }
        binding.tvEdit.gone()
        binding.ivNoContact.visible()
        binding.textNoContact.visible()


    }

    private fun setStateHasData() {
        binding.textView18.visible()
//        binding.tvContactCount.visible()

        binding.ivNoContact.gone()
        binding.textNoContact.gone()

        if (viewModel.editMode.value != true) {
            binding.tvEdit.visible()
        }

        if(viewModel.disableEditMode.value != true){
            binding.btnAddContact.visible()
            binding.textAddContact.gone()
            binding.btnAddStockBottom.gone()

        }


    }


    private fun updateWidgetCount() {
        val dataCount = adapter.itemCount
//        binding.tvContactCount.text = "($dataCount/${viewModel.maxCount})"

        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }


    private fun setWidgets(dataList: List<Widget>) {
        adapter.setDataSet(dataList)

//        if (viewModel.syncWithDevice) {
//            syncDataWithDevice()
//        }
    }
}