package com.noisefit.ui.dashboard.feature.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel
@Inject
constructor(
    val sessionManagers: SessionManager,
    private val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {


    private val _editMode = MutableLiveData<Boolean>()

    var contactsToSend = ArrayList<Contact>()
    val sendContact = MutableLiveData<Event<Contact>>()


    fun getLocalDataStore(): DataStoredInterface {
        return localDataStore
    }

    fun getSessionManager(): SessionManager {
        return sessionManagers
    }


    fun getNextContact(sendFakeContact: Boolean, allContactSent: () -> Unit) {
        if (contactsToSend.isEmpty()) {
            if (sendFakeContact) {
                //sending fake data to delete last contact
                sendContact.postValue(Event(Contact()))
            }
            allContactSent.invoke()
            return
        }

        val contactData = contactsToSend.first()
        sendContact.value = (Event(contactData))
        contactsToSend.removeFirstOrNull()
    }

    fun contactsToSend(contactList: List<Contact>) {
        contactsToSend.clear()

        //hack overriding id with indexes and expecting id as number in return
        contactList.forEachIndexed { index, contact ->
            contact.id = index.toString()
            contactsToSend.add(contact)
        }
        getNextContact(true) {}
    }


    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    var syncWithDevice = false

    private val _contactsLiveData = MutableLiveData<ArrayList<Contact>>()

    val contactsLiveData: LiveData<ArrayList<Contact>>
        get() = _contactsLiveData

    fun setContactList(contactList: ArrayList<Contact>) {
        _contactsLiveData.value = ArrayList()
        _contactsLiveData.value?.clear()
        _contactsLiveData.value = contactList
    }

    fun getContactList(): List<Contact> {
        return _contactsLiveData.value ?: ArrayList()
    }


}

