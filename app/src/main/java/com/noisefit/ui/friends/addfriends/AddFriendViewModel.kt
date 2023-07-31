package com.noisefit.ui.friends.addfriends

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.BuddiesUserNew
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@HiltViewModel
class AddFriendViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val friendsRepository: FriendsRepository,
    var localDataStore: DataStoredInterface,
    val mApplication: Application,
    var sessionManager: SessionManager
) : BaseViewModel() {
    var text =
        "Exercising is more fun with others. Join me on NoiseFit to follow each other's fitness goals. Start" +
                " tracking your daily steps and activity- https://play.google.com/store/apps/details?id=com.noisefit"
    private val _noiseFitAppUser = MutableLiveData<List<BuddiesUserNew>>()

    private val _commonInterestList = MutableLiveData<List<BuddiesUserNew>>()
    val commonInterestListData: LiveData<List<BuddiesUserNew>>
        get() = _commonInterestList

    private val _pastWinnerList = MutableLiveData<List<BuddiesUserNew>>()
    val pastWinnerList: LiveData<List<BuddiesUserNew>>
        get() = _pastWinnerList

    private val _nearByList = MutableLiveData<List<BuddiesUserNew>>()
    val nearByList: LiveData<List<BuddiesUserNew>>
        get() = _nearByList

    val noiseFitAppUsers: LiveData<List<BuddiesUserNew>>
        get() = _noiseFitAppUser

    private var mContactsList: HashSet<String>? = null

    fun fetchPhoneContacts() {
        if(mContactsList!=null){
            getNoiseFitContacts(mContactsList!!)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            deviceRepository.getOnlyNumber(ArrayList(), false)
                .collect { contactList ->
                    mContactsList = contactList
                    getNoiseFitContacts(contactList)
                }
        }

    }

    fun getNoiseFitContacts(contactList: HashSet<String>) {


        val contactJsonArray: JsonArray = Gson().toJsonTree(contactList).asJsonArray

        val jsonObject = JsonObject().apply {
            addProperty("type", "contacts")
            add("mobile", contactJsonArray)
        }
        viewModelScope.launch {
            friendsRepository.getNoiseFitContactsList(jsonObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        print("Error in getting Response")
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getNoiseFitContacts(contactList)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data.let {

                            if (it == null) {
                                _noiseFitAppUser.postValue(ArrayList())
                            } else {
                                _noiseFitAppUser.postValue(it)
                            }
                        }
                    }
                }
            }
        }
    }


    fun getInterestListData() {
        val request = JsonObject()
        request.addProperty("type", "commonInterests")

        viewModelScope.launch {
            friendsRepository.getInterestList(request).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getInterestListData()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { it ->
                            _commonInterestList.postValue(it)
                        }
                    }
                }
            }
        }
    }

    fun getPastWinnerListData() {
        val request = JsonObject()
        request.addProperty("type", "pastWinners")

        viewModelScope.launch {
            friendsRepository.getPastWinnerList(request).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getInterestListData()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { it ->
                            _pastWinnerList.postValue(it)
                        }
                    }
                }
            }
        }
    }

    fun getNearByListData() {
        val request = JsonObject()
        request.addProperty("type", "nearMe")


        viewModelScope.launch {
            friendsRepository.getNearBy(request).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getInterestListData()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { it ->
                            _nearByList.postValue(it)
                        }
                    }
                }
            }
        }
    }

    fun addFriendRequest(receiverId: Int, status: String, updateSuccess: () -> Unit) {

        val requestObject = JsonObject().apply {
            addProperty("request_status", status)
            addProperty("friend_id", receiverId)
        }
        viewModelScope.launch {
            friendsRepository.setFriendRequestStatus(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    addFriendRequest(
                                        receiverId, status, updateSuccess
                                    )
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            updateSuccess()
                        }
                    }
                }
            }
        }

    }

    fun removeFriendRequest(receiverId: Int, status: String, updateSuccess: () -> Unit) {
        val requestObject = JsonObject().apply {
            this.addProperty("request_status", status)
            this.addProperty("friend_id", receiverId)
        }
        viewModelScope.launch {
            friendsRepository.setFriendRequestStatus(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    removeFriendRequest(
                                        receiverId, status, updateSuccess
                                    )
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            updateSuccess()
                        }
                    }
                }
            }
        }

    }

}