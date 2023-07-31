package com.noisefit.ui.workout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.ui.BaseViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ActivityShareViewModel @Inject constructor(
    val userRepository: UserRepository,
    val rewardsRepository: RewardsRepository
) : BaseViewModel() {


    private val _imagesList = MutableLiveData<List<String>>()
    val imagesList: LiveData<List<String>> = _imagesList



    fun getWorkoutShareImages() {
        viewModelScope.launch {
            userRepository.getWorkoutShareImages().collect { resource ->
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
                                    getWorkoutShareImages()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _imagesList.postValue(it)
                        }
                    }
                }
            }
        }
    }

    fun earnRewardsPoints() {
        viewModelScope.launch {
            val request = JsonObject().apply {
                this.addProperty("task_enum","1st_workout_share")
            }
            rewardsRepository.earnRewardsPoints(request).collect { resource ->

            }
        }
    }
}