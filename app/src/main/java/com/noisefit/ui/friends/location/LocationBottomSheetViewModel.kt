package com.noisefit.ui.friends.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.User
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.UserLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONException
import javax.inject.Inject

@HiltViewModel
class LocationBottomSheetViewModel
@Inject
constructor(
    val friendsRepository: FriendsRepository,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    private val _userLocationUpdateResult = MutableLiveData<Boolean>()
    val userLocationUpdateResult: LiveData<Boolean>
        get() = _userLocationUpdateResult

    var userLocation = UserLocation()


    var user: User? = null


    init {
        user = localDataStore.getUser()
        user?.location?.let {
            userLocation = it
        }

        sessionManager.tempUserLocation?.let { uLocation ->
            uLocation.stateId?.let {
                userLocation.stateId = it
            }
            uLocation.cityId?.let {
                userLocation.cityId = it
            }
            uLocation.state?.let {
                userLocation.state = it
            }
            uLocation.city?.let {
                userLocation.city = it
            }

            if (uLocation.stateChanged) {

                userLocation.cityId = null
                userLocation.city = null
            }

        }

    }


    fun updateUserLocation() {

//        "state_id":1,
//        "city_id":12
        val jsonObject = JsonObject()
        try {
            jsonObject.addProperty("city_id", userLocation.cityId)
            jsonObject.addProperty("state_id", userLocation.stateId)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        viewModelScope.launch {
            friendsRepository.saveUserLocation(jsonObject).collect { resource ->
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
                                    updateUserLocation()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.isMapped) {
                                user!!.location = userLocation
                                localDataStore.saveUserInfo(user!!)
                                _userLocationUpdateResult.postValue(true)
                            } else {
                                sendMessage("Something went wrong!!")
                            }

                        }
                    }
                }
            }
        }

    }
}
