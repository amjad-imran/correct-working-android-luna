package com.noisefit.ui.dashboard.feature.sos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.models.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SOSViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {
    private val _editMode = MutableLiveData<Boolean>()
    var syncWithDevice = false
    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    var sosSwitch = false
    private val _sosContact = MutableLiveData<ArrayList<Contact>>()

    val sosContact: LiveData<ArrayList<Contact>>
        get() = _sosContact


    fun setContactList(contactList: ArrayList<Contact>) {
        _sosContact.value = ArrayList()
        _sosContact.value?.clear()
        _sosContact.value = contactList
    }

    fun getContactList(): List<Contact> {
        return _sosContact.value ?: ArrayList()
    }
}