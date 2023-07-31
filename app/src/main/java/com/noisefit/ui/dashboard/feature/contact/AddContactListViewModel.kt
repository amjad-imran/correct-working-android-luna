package com.noisefit.ui.dashboard.feature.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddContactListViewModel
@Inject
constructor(
    private val localDataStore: DataStoredInterface,
    private val deviceRepository: DeviceRepository,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {

    var contactCount: Int = 0
    private val _noiseFitSearchQuery = MutableLiveData<String>()
    val noiseFitSearchQuery: LiveData<String>
        get() = _noiseFitSearchQuery

    private val _contactsLiveData = MutableLiveData<ArrayList<Contact>>()
    val contactsLiveData: LiveData<ArrayList<Contact>>
        get() = _contactsLiveData


    private val selectedContact = ArrayList<Contact>()


    private fun isSupportedSTDCall(): Boolean {
        val watchType = watchesSDK.getWatchType()
        if (watchType == SDKWatchType.SDK_ZH) {
            return true
        }
        return false
    }
    fun getLocalDataStore(): DataStoredInterface {
        return localDataStore
    }

    fun fetchPhoneContacts() {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            deviceRepository.getSaveContactInfo(selectedContact, isSupportedSTDCall()).collect { contactList ->
                _contactsLiveData.postValue(ArrayList(contactList))
                setLoading(false)
            }
        }
    }

    fun getContact(): List<Contact> {
        return selectedContact
    }

    fun updateContact(contact: Contact) {
        val contact1 = Contact()
        contact1.id = contact.id
        contact1.number = contact.number
        contact1.selected = contact.selected
        contact1.name = contact.name
        contact1.photoUri = contact.photoUri
        selectedContact.add(contact1)
    }

    fun setContact(contact: List<Contact>) {
        selectedContact.addAll(contact)
    }

    fun removeContact(contact: Contact) {
        selectedContact.forEachIndexed { index, sContact ->
            if (sContact.id == contact.id) {
                selectedContact.removeAt(index)
                return
            }
        }
        selectedContact.remove(contact)
    }
}

