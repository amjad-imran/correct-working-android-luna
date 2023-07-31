package com.noisefit.ui.dashboard.feature.widget

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentWidgetSelectionBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.feature.sportSelection.ADD_SPORT_REQUEST_KEY
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.Widget
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint


private const val TAG = "WidgetSelectionFragment"

@AndroidEntryPoint
class WidgetSelectionFragment :
    BaseFragment<FragmentWidgetSelectionBinding>(FragmentWidgetSelectionBinding::inflate),
    WidgetSelectionListener {


    private val viewModel: WidgetSelectionViewModel by viewModels()
    lateinit var touchHelper: ItemTouchHelper
    private val widgetSelectionAdapter by lazy { WidgetSelectionAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCount()
        setRecycler()
        if (!viewModel.fetchData) {
            LOGS.d("$TAG inside")
            viewModel.sessionManager.sendQueryAction(QueryAction.GetWidgetSortList)
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
                    title,
                    alertMessage,
                    getString(R.string.text_close)
                )
            )
        )

    }

    private fun invokeAdd() {
        val count = viewModel.getSelectedWidgetSort().size

        if (count >= viewModel.maxCount) {
            showMaxDialog(
                getString(R.string.text_max_widget_message, viewModel.maxCount),
                getString(R.string.text_max_widget_reached)
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
            WidgetSelectionFragmentDirections.actionWidgetSelectionFragmentToAddWidgetSelectionFragment(
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
            UpdateDeviceAction.UpdateWidgetSortList(
                sportList
            )
        )

    }

    private fun setCount() {
        binding.textAddContact.text =
            getString(R.string.text_add_to_add_widget, viewModel.maxCount)
//        val countText = "(${widgetSelectionAdapter.getCurrentDataSet().size}/${viewModel.maxCount})"
//        binding.tvContactCount.gone()

    }

    private fun setRecycler() {
        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(widgetSelectionAdapter)
        touchHelper = ItemTouchHelper(callback)
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = widgetSelectionAdapter
            touchHelper.attachToRecyclerView(this)
        }
    }

    override fun subscribeObservers() {
        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.WidgetSortListObtained -> {
                    viewModel.fetchData = true
                    viewModel.setLoading(false)
                    viewModel.setWidgetList(ArrayList(it.widgetList))

                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.WidgetSortListUpdated -> {
                        binding.progressBar.root.gone()
                        if (value.success) {
                            updateWidgetCount()
                            context.showShortToast(getString(R.string.text_widget_update_success))
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
        //LOGS.d("onItemRemoved ${widgetSelectionAdapter.itemCount} --  ${viewModel.selectedSports.value?.size}")

        if (widgetSelectionAdapter.itemCount == 3) {
            showMaxDialog(
                getString(R.string.text_atleast_one_widget_required),
                getString(R.string.text_alert)
            )
            return
        }

        viewModel.removeSelectedFromAllWidgetSort(widget)
        widgetSelectionAdapter.removeItem(position)
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
        widgetSelectionAdapter.setEditMode(true)
    }

    private fun setStateDefault() {
        binding.tvEdit.visible()
        binding.btnSaveChanges.gone()
        /*binding.layoutToolbar.root.visible()
        binding.toolbarEdit.gone()
        binding.bEdit.visible()*/
        widgetSelectionAdapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.textView18.gone()
        binding.btnSaveChanges.gone()
//        binding.tvContactCount.gone()
        binding.btnAddContact.gone()
        binding.ivNoContact.visible()
        binding.textNoContact.visible()
        binding.textAddContact.visible()
        binding.btnAddStockBottom.visible()
        binding.tvEdit.gone()
    }

    private fun setStateHasData() {
        binding.textView18.visible()
//        binding.tvContactCount.visible()
        binding.btnAddContact.visible()
        binding.ivNoContact.gone()
        binding.textNoContact.gone()
        binding.textAddContact.gone()
        binding.btnAddStockBottom.gone()
        if (viewModel.editMode.value != true) {
            binding.tvEdit.visible()
        }
    }


    private fun updateWidgetCount() {
        val dataCount = widgetSelectionAdapter.itemCount
//        binding.tvContactCount.text = "($dataCount/${viewModel.maxCount})"

        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }


    private fun setWidgets(dataList: List<Widget>) {
        widgetSelectionAdapter.setDataSet(dataList)
//        if (viewModel.syncWithDevice) {
//            syncDataWithDevice()
//        }
    }
}