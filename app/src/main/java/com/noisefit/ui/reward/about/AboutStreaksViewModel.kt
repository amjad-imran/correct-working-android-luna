package com.noisefit.ui.reward.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.RewardAboutSubCategoryList
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutStreaksViewModel
@Inject
constructor(val rewardsRepository: RewardsRepository) :
    BaseViewModel() {

    private val _aboutStreaks = MutableLiveData<List<RewardAboutSubCategoryList>>()
    val aboutStreaks: LiveData<List<RewardAboutSubCategoryList>>
        get() = _aboutStreaks

    private val _termsAndCondition = MutableLiveData<List<String>>()
    val termsAndCondition: LiveData<List<String>>
        get() = _termsAndCondition

    fun getStreaksAboutData() {
        viewModelScope.launch {
            rewardsRepository.getStreaksAboutData()
                .collect { resource ->
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
                                        getStreaksAboutData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                _aboutStreaks.postValue(it.about_streak)
                                _termsAndCondition.postValue(it.terms_and_condition)
                            }

                        }
                    }
                }
        }
    }
}