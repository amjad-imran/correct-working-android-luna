package com.noisefit.ui.dashboard.feature.sportSelection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.databinding.FragmentSportSelectionBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.SportsModeList
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SportSelectionFragment :
    BaseFragment<FragmentSportSelectionBinding>(FragmentSportSelectionBinding::inflate),
    SportSelectionListener {

    private val TAG = "SportSelectionFragment"
    private val viewModel: SportSelectionViewModel by viewModels()

    private val sportSelectionAdapter by lazy { SportSelectionAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setContactTextCount()
        setRecycler()
        if (viewModel.getSportList().isEmpty()) {
            LOGS.d("$TAG inside")
            viewModel.sessionManager.sendQueryAction(QueryAction.GetSportModeInfo)
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
        val count = viewModel.getSportList().size

        if (count >= viewModel.maxCount) {
            showMaxDialog(
                getString(R.string.text_max_sport_message, viewModel.maxCount),
                getString(R.string.text_max_sport_reached)
            )
            return
        }


        setFragmentResultListener(ADD_SPORT_REQUEST_KEY) { key, bundle ->
            val contactList = bundle.getParcelableArrayList<SportsModeList.SportsMode>("sports")
            contactList?.let {
                viewModel.setSportList(it)
                viewModel.setEditMode(true)
                if (viewModel.getSportList().isEmpty()) {
                    syncDataWithDevice()
                }
            }
        }

        navigate(
            SportSelectionFragmentDirections.actionSportSelectionFragmentToAddSportSelectionFragment(
                viewModel.getSportList().toTypedArray()
            )
        )
    }

    private fun syncDataWithDevice() {
        viewModel.setLoading(true)
        LOGS.d("$TAG sync")
        val list = SportsModeList().apply {
            this.sportsModes = viewModel.getSportList()
        }
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetSportModeInfo(
                list
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
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sportSelectionAdapter
        }

    }

    private fun addDefaultActivity() {
        viewModel.sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetSportModeInfo(
            SportsModeList().apply {
                this.sportsModes = arrayListOf(
                    SportsModeList.SportsMode(
                        index = 14,
                        name = "workout",
                        type = 8,
                        value = true
                    )
                )
            }
        ))
    }

    override fun subscribeObservers() {
        viewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.SportModeInfoObtained -> {
                    viewModel.setLoading(false)
                    if (it.sportsModeList?.sportsModes.isNullOrEmpty()) {
                        addDefaultActivity()
                    } else {
                        it.sportsModeList?.sportsModes?.let { modes ->
                            viewModel.setSportList(ArrayList(modes))
                        }
                    }

                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.SportModeDataUpdated -> {
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

        viewModel.sportsLiveData.observe(viewLifecycleOwner) {
            setSport(it)
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

    override fun onLongPressed(position: Int) {
        setStateEdit()
    }

    override fun onItemRemoved(position: Int, sport: SportsModeList.SportsMode) {
        // viewModel.setLoading(true)
        if(sportSelectionAdapter.itemCount == 1){
            showMaxDialog(
                getString(R.string.text_atleast_one_sport_required),
                getString(R.string.text_alert)
            )
            return
        }

        sportSelectionAdapter.removeItem(position)
        updateContactCount()
//        viewModel.getSportList().toMutableList().removeAt(position - 1)
//        syncDataWithDevice()
        // viewModel.deleteStockPosition = position
        //sessionManager.sendUpdateQueryAction(UpdateDeviceAction.DeleteStock(stock.symbol))
    }


    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
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


    private fun setSport(contact: List<SportsModeList.SportsMode>) {
        sportSelectionAdapter.setDataSet(contact)
//        if (viewModel.syncWithDevice) {
//            syncDataWithDevice()
//        }
    }
}