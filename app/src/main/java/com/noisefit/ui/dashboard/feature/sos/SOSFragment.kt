package com.noisefit.ui.dashboard.feature.sos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSOSBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.feature.contact.ADD_CONTACT_KEY
import com.noisefit.ui.dashboard.feature.contact.ContactListAdapter
import com.noisefit.ui.dashboard.feature.contact.ContactListListener
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.SOSContact
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.loadImage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SOSFragment : BaseFragment<FragmentSOSBinding>(FragmentSOSBinding::inflate),
    ContactListListener {

    private val viewModel: SOSViewModel by viewModels()
    private val maxContactCount: Int by lazy {
        viewModel.watchesSDK.getMaxSOSContactsToAdd(viewModel.localDataStore.getConnectedDevice())
    }
    lateinit var touchHelper: ItemTouchHelper
    private val contactAdapter by lazy { ContactListAdapter(this,true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_sos)
            tvTitle.text = getString(R.string.text_sos)
            tvTitleDisc.gone()
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                viewModel.sosSwitch = isChecked
                if (isChecked) {
                    binding.btnTopManageApp.enable()
                    binding.btnManageApp.enable()
                    binding.btnSaveChanges.enable()
                } else {
                    viewModel.setEditMode(false)
                    binding.btnTopManageApp.disable()
                    binding.btnManageApp.disable()
                    binding.btnSaveChanges.disable()
                }

                syncContactWithDevice()
            }
        }

        setContactTextCount()
        setRecycler()
        if (viewModel.getContactList().isEmpty()) {
            viewModel.sessionManager.sendQueryAction(QueryAction.GetSOSContactList)
            viewModel.setLoading(true)
        }

        setSwitchState(viewModel.sosSwitch)

    }

    private fun setSwitchState(isChecked: Boolean) {

        binding.lytFeatureTile.llSwitch.isChecked = isChecked

        if (isChecked) {
            binding.btnTopManageApp.enable()
            binding.btnManageApp.enable()
            binding.btnSaveChanges.enable()
        } else {
            binding.btnTopManageApp.disable()
            binding.btnManageApp.disable()
            binding.btnSaveChanges.disable()
        }

    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnManageApp.setOnClickListener {
            invokeAdd()
        }

        binding.tvEdit.setOnClickListener {
            if (!binding.lytFeatureTile.llSwitch.isChecked) {
                return@setOnClickListener
            }
            viewModel.setEditMode(true)
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.setEditMode(false)
            syncContactWithDevice()
        }
        binding.btnTopManageApp.setOnClickListener {
            invokeAdd()
        }
    }

    private fun invokeAdd() {
        val count = viewModel.getContactList().size

        if (count >= maxContactCount) {
            val alertMessage = getString(R.string.text_max_contact_message, maxContactCount)
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.InfoAlertDialog(
                        getString(
                            R.string.text_max_contact_reached
                        ), alertMessage, getString(R.string.text_close)
                    )
                )
            )
            return
        }


        setFragmentResultListener(ADD_CONTACT_KEY) { _, bundle ->
            val contactList = bundle.getParcelableArrayList<Contact>("contacts")
            contactList?.let {
                viewModel.setContactList(it)
                viewModel.setEditMode(true)
                if (viewModel.getContactList().isEmpty()) {
                    syncContactWithDevice()
                }

            }
        }

        navigate(
            SOSFragmentDirections.actionSosFragmentToAddContactListFragment(
                viewModel.getContactList().toTypedArray(),
                maxContactCount
            )
        )
    }

    private fun syncContactWithDevice() {
        val sosContact = SOSContact()
        sosContact.sosSwitch = viewModel.sosSwitch
        sosContact.contactList.addAll(viewModel.getContactList())
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetSOSContact(
                sosContact
            )
        )

    }


    private fun setContactTextCount() {
        binding.textAddContact.text = getString(R.string.text_add_to_add_contact, maxContactCount)
      //  val countText = "(${contactAdapter.getCurrentDataSet().size}/$maxContactCount)"
        binding.tvContactCount.gone()
        binding.tvContactAddedText.text =
            getString(R.string.text_add_your_top_frequent_contacts_here, maxContactCount)
    }

    private fun setRecycler() {
        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(contactAdapter)
        touchHelper = ItemTouchHelper(callback)
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactAdapter
            touchHelper.attachToRecyclerView(this)
        }

    }

    override fun subscribeObservers() {
        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.SOSContactObtained -> {
                    if (!viewModel.syncWithDevice) {
                        viewModel.setLoading(false)
                        viewModel.sosSwitch = it.sosContact.sosSwitch
                        viewModel.setContactList(it.sosContact.contactList)
                        viewModel.syncWithDevice = true
                    }
                }

                else -> {}
            }
        }


        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.SOSContactUpdated -> {
                        binding.progressBar.root.gone()
                        if (value.success) {
                            updateContactCount()
                            if(viewModel.getContactList().isNotEmpty()){
                                context.showShortToast(getString(R.string.text_contact_list_update_success))
                            }

                        }
                    }

                    else -> {}
                }
            }
        }

        viewModel.sosContact.observe(this) {
            setContact(it)
            setSwitchState(viewModel.sosSwitch)
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

    override fun onItemRemoved(position: Int, contact: Contact) {
        viewModel.setLoading(true)
        contactAdapter.removeItem(position)
         syncContactWithDevice()

    }

    private fun setStateEdit() {
        /* binding.layoutToolbar.root.gone()
         binding.toolbarEdit.visible()
         binding.bEdit.gone()*/

        showEdit()
        binding.btnSaveChanges.visible()
        contactAdapter.setEditMode(true)
    }

    private fun setStateDefault() {
       showEdit()

        binding.btnSaveChanges.gone()
        /*binding.layoutToolbar.root.visible()
        binding.toolbarEdit.gone()
        binding.bEdit.visible()*/
        contactAdapter.setEditMode(false)
    }


    private fun showEdit(){
        if(viewModel.getContactList().isNotEmpty()){
            binding.tvEdit.visible()
        }

    }
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    private fun updateContactCount() {
        val dataCount = contactAdapter.itemCount
//        binding.tvContactCount.text = "($dataCount/$maxContactCount)"

        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }

    private fun setStateNoData() {
        binding.textView18.gone()
        binding.tvContactCount.gone()
        binding.btnTopManageApp.gone()
        binding.ivNoContact.visible()
        binding.textNoContact.visible()
        binding.textAddContact.visible()
        binding.btnManageApp.visible()
        binding.btnSaveChanges.gone()
        binding.tvEdit.gone()
    }

    private fun setStateHasData() {
        binding.textView18.visible()
        binding.tvContactCount.visible()
        binding.btnTopManageApp.visible()
        binding.ivNoContact.gone()
        binding.textNoContact.gone()
        binding.textAddContact.gone()
        binding.btnManageApp.gone()
        if (viewModel.editMode.value != true) {
            showEdit()
        }
    }


    private fun setContact(contact: List<Contact>) {
        contactAdapter.setDataSet(contact)

//        if (viewModel.syncWithDevice) {
//            syncContactWithDevice()
//        }
    }


}