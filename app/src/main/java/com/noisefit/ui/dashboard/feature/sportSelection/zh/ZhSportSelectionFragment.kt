package com.noisefit.ui.dashboard.feature.sportSelection.zh

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.databinding.FragmentZhSportSelectionFramgentBinding
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

@AndroidEntryPoint
class ZhSportSelectionFragment :
    BaseFragment<FragmentZhSportSelectionFramgentBinding>(FragmentZhSportSelectionFramgentBinding::inflate),
    SportSelectionListener {

    private val TAG = "ZhSportSelectionFragment"
    private val viewModel: ZhSportSelectionViewModel by viewModels()
    lateinit var touchHelper: ItemTouchHelper
    private val sportSelectionAdapter by lazy { ZhSportSelectionAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setContactTextCount()
        setRecycler()
        if (!viewModel.fetchData) {
            LOGS.d("$TAG inside")
            viewModel.sessionManager.sendQueryAction(QueryAction.GetSportWidgetSortList)
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
        val count = viewModel.getSelectedSport().size

        if (count >= viewModel.maxCount) {
            showMaxDialog(
                getString(R.string.text_max_sport_message, viewModel.maxCount),
                getString(R.string.text_max_sport_reached)
            )

            return
        }


        setFragmentResultListener(ADD_SPORT_REQUEST_KEY) { _, bundle ->
            val allSports = bundle.getParcelableArrayList<Widget>("allSports")
            allSports?.let {
                viewModel.setEditMode(true)
                viewModel.setAllSports(allSports)
            }
        }

        navigate(
            ZhSportSelectionFragmentDirections.actionZhSportSelectionFragmentToZhAddSportSelectionFragment(
                viewModel.getAllSportList().toTypedArray()
            )
        )
    }

    private fun syncDataWithDevice() {
        viewModel.setLoading(true)
        
        val sportList =  viewModel.getSyncSportList()

        sportList.forEach {
            LOGS.d("$TAG 12 $it")
        }

        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetSportWidgetSortList(
                sportList
            )
        )

    }

    private fun setContactTextCount() {
        binding.textAddContact.text =
            getString(R.string.text_add_to_add_sport, viewModel.maxCount)
        val countText = "(${sportSelectionAdapter.getCurrentDataSet().size}/${viewModel.maxCount})"
        binding.tvContactCount.text = countText

    }

    private fun setRecycler() {
        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(sportSelectionAdapter)
        touchHelper = ItemTouchHelper(callback)
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sportSelectionAdapter
            touchHelper.attachToRecyclerView(this)
        }
    }

    override fun subscribeObservers() {
        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.SportWidgetSortList -> {
                    viewModel.fetchData = true
                    viewModel.setLoading(false)
                    viewModel.setAllSports(it.sportWidgetSortList)

                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.SportWidgetSortDataUpdated -> {
                        binding.progressBar.root.gone()
                        if (value.success) {
                            updateContactCount()
                            context.showShortToast(getString(R.string.text_sports_mode_update_success))
                            // viewModel.sessionManager.sendQueryAction(QueryAction.GetSportModeInfo)
                        }
                    }
                    else -> {}
                }
            }
        }

        viewModel.selectedSports.observe(this) {
            setSport(it)
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

    override fun onLongPressed(position: Int) {
        setStateEdit()
    }

    override fun onItemRemoved(position: Int, widget: Widget) {
        //LOGS.d("onItemRemoved ${sportSelectionAdapter.itemCount} --  ${viewModel.selectedSports.value?.size}")

        if(sportSelectionAdapter.itemCount == 1){
            showMaxDialog(
                getString(R.string.text_atleast_one_sport_required),
                getString(R.string.text_alert)
            )
            return
        }

        viewModel.removeSelectedFromAllSport(widget)
        sportSelectionAdapter.removeItem(position)
        updateContactCount()
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
        sportSelectionAdapter.setEditMode(true)
    }

    private fun setStateDefault() {
        binding.tvEdit.visible()
        binding.btnSaveChanges.gone()
        /*binding.layoutToolbar.root.visible()
        binding.toolbarEdit.gone()
        binding.bEdit.visible()*/
        sportSelectionAdapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.textView18.gone()
        binding.btnSaveChanges.gone()
        binding.tvContactCount.gone()
        binding.btnAddContact.gone()
        binding.ivNoContact.visible()
        binding.textNoContact.visible()
        binding.textAddContact.visible()
        binding.btnAddStockBottom.visible()
        binding.tvEdit.gone()
    }

    private fun setStateHasData() {
        binding.textView18.visible()
        binding.tvContactCount.visible()
        binding.btnAddContact.visible()
        binding.ivNoContact.gone()
        binding.textNoContact.gone()
        binding.textAddContact.gone()
        binding.btnAddStockBottom.gone()
        if (viewModel.editMode.value != true) {
            binding.tvEdit.visible()
        }
    }


    private fun updateContactCount() {
        val dataCount = sportSelectionAdapter.itemCount
        binding.tvContactCount.text = "($dataCount/${viewModel.maxCount})"

        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }


    private fun setSport(dataList: List<Widget>) {
        sportSelectionAdapter.setDataSet(dataList)
//        if (viewModel.syncWithDevice) {
//            syncDataWithDevice()
//        }
    }
}