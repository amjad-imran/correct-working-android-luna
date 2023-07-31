package com.noisefit.ui.friends.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterestsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    val localDataStore: DataStoredInterface,
    var sessionManager: SessionManager,
    val authenticationRepository: AuthenticationRepository
) : BaseViewModel() {

    private val _interests = MutableLiveData<List<Interest>>()
    var interests = _interests

    val interestUpdated = MutableLiveData<Event<Boolean>>()

    var selectedInterests = ArrayList<Int>()
    var lastSelectedInterest = ArrayList<Interest>()


//    init {
//
//    }
    fun prepareSelectedInterestData(){
        if (lastSelectedInterest == null || lastSelectedInterest.isEmpty()) {
            this.selectedInterests.clear()
            val interest = localDataStore.getUser()?.interests ?: ArrayList()

            interest.forEach {
                if (it.id != null) {
                    this.selectedInterests.add(it.id!!.toInt())
                }
            }
        }
        else{
            val interest = lastSelectedInterest

            interest.forEach {
                if (it.id != null) {
                    this.selectedInterests.add(it.id!!.toInt())
                }
            }
        }
    }

    fun getInterestList() {
        viewModelScope.launch {
            userRepository.getInterests().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getInterestList()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _interests.postValue(it)
                        }
                    }
                }
            }
        }

    }

    fun updateInterest(selectedInterests: List<Interest>) {
        viewModelScope.launch {


            val requestObject = JsonObject().apply {
                this.add("interest_id", JsonArray().apply {
                    selectedInterests.forEach { selectedId ->
                        if (selectedId.id != null) {
                            this.add(selectedId.id!!.toInt())
                        }
                    }
                })
            }
            userRepository.updateInterests(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getInterestList()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            val updatedUser = authenticationRepository.getUser()?.apply {
                                this.interests = it
                            }
                            if (updatedUser != null) {
                                authenticationRepository.saveUserInfo(updatedUser)
                            }
                            interestUpdated.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }

}