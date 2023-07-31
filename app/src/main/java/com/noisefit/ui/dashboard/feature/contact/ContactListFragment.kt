package com.noisefit.ui.dashboard.feature.contact

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
import com.noisefit.databinding.FragmentContactListBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.SDKWatchType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.Contact
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ContactListFragment :
    BaseFragment<FragmentContactListBinding>(FragmentContactListBinding::inflate),
    ContactListListener {

    private val viewModel: ContactListViewModel by viewModels()


    private val maxContactCount: Int by lazy {
        viewModel.watchesSDK.getMaxContactsToAdd(viewModel.getLocalDataStore().getConnectedDevice())
    }

    lateinit var touchHelper: ItemTouchHelper
    private val contactAdapter by lazy { ContactListAdapter(this,false) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setContactTextCount()
        setRecycler()
        if (viewModel.getContactList().isEmpty()) {
            viewModel.getSessionManager().sendQueryAction(QueryAction.GetContactList)
            viewModel.setLoading(true)
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
            viewModel.sessionManagers.logInsiderAppEvent(InsiderAppEvents.MY_CONTACT_EDIT_CLICK)
        }
        binding.btnAddContact.setOnClickListener {
            invokeAdd()
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.setEditMode(false)
            viewModel.sessionManagers.logInsiderAppEvent(InsiderAppEvents.MY_CONTACT_ADD_CONTACT_SAVE_CLICK)
            syncContactWithDevice()
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


        setFragmentResultListener(ADD_CONTACT_KEY) { key, bundle ->
            val contactList = bundle.getParcelableArrayList<Contact>("contacts")
            contactList?.let {
                viewModel.setContactList(it)
                viewModel.setEditMode(true)
                if (viewModel.getContactList().isNullOrEmpty()) {
                    syncContactWithDevice()
                }
            }
        }

        viewModel.sessionManagers.logInsiderAppEvent(InsiderAppEvents.MY_CONTACT_ADD_CLICK)
        navigate(
            ContactListFragmentDirections.actionContactListFragmentToAddContactListFragment(
                viewModel.getContactList().toTypedArray(),
                maxContactCount
            )
        )
    }

    private fun syncContactWithDevice() {
        if (viewModel.watchesSDK.getWatchType(
                viewModel.getLocalDataStore().getConnectedDevice()
            ) == SDKWatchType.SDK_QUBE
        ) {
            viewModel.contactsToSend(viewModel.getContactList())
        } else {
            viewModel.getSessionManager().sendUpdateQueryAction(
                UpdateDeviceAction.SetContactList(
                    viewModel.getContactList()
                )
            )
        }

    }

    private fun setContactTextCount() {
        binding.textAddContact.text = getString(R.string.text_add_to_add_contact, maxContactCount)
        val countText = "(${contactAdapter.getCurrentDataSet().size}/$maxContactCount)"
        binding.tvContactCount.text = countText
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
        viewModel.getSessionManager().deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.ContactListObtained -> {
                    if (!viewModel.syncWithDevice) {
                        viewModel.setLoading(false)
                        viewModel.setContactList(ArrayList(it.contactData))
                        viewModel.syncWithDevice = true
                    }
                }

                else -> {}
            }
        }

        viewModel.sendContact.observe(this) {
            it.getContent()?.let { data ->
                binding.progressBar.root.visible()
                val list = ArrayList<Contact>()
                list.add(data)
                viewModel.getSessionManager().sendUpdateQueryAction(
                    UpdateDeviceAction.SetContactList(
                        list
                    )
                )
            }
        }

        viewModel.getSessionManager().updateDeviceCallback.observe(this) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.ContactListUpdated -> {
                        binding.progressBar.root.gone()
                        if (value.success) {
                            updateContactCount()
                            if (viewModel.watchesSDK.getWatchType(viewModel.getSessionManager().connectedDevice.value) == SDKWatchType.SDK_QUBE) {
                                viewModel.getNextContact(false) {
                                    context.showShortToast(getString(R.string.text_contact_update_success))
                                }
                            } else {
                                context.showShortToast(getString(R.string.text_contact_list_update_success))
                            }
                        }
                    }

                    else -> {}
                }
            }
        }

        viewModel.contactsLiveData.observe(this) {
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
        // viewModel.deleteStockPosition = position
        //sessionManager.sendUpdateQueryAction(UpdateDeviceAction.DeleteStock(stock.symbol))
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
        contactAdapter.setEditMode(true)
    }

    private fun setStateDefault() {
        binding.tvEdit.visible()
        binding.btnSaveChanges.gone()
        /*binding.layoutToolbar.root.visible()
        binding.toolbarEdit.gone()
        binding.bEdit.visible()*/
        contactAdapter.setEditMode(false)
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
        val dataCount = contactAdapter.itemCount
        binding.tvContactCount.text = "($dataCount/$maxContactCount)"

        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }


    private fun setContact(contact: List<Contact>) {
        contactAdapter.setDataSet(contact)
//        if (viewModel.syncWithDevice) {
//            syncContactWithDevice()
//        }
    }
}