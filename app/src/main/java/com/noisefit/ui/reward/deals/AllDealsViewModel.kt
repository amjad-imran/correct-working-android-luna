package com.noisefit.ui.reward.deals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.CouponList
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.AllDealsResponse
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllDealsViewModel @Inject constructor(val rewardsRepository: RewardsRepository) :
    BaseViewModel() {

    private val _dealsList = MutableLiveData<com.noisefit_commans.data.response.AllDealsResponse>()
    val dealsList: LiveData<com.noisefit_commans.data.response.AllDealsResponse>
        get() = _dealsList


    fun getAllDeals() {
        viewModelScope.launch {
            rewardsRepository.getDealsList()
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
                                        getAllDeals()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let {
                                _dealsList.postValue(it)
                            }

                        }
                    }
                }
        }

    }

    fun getEmptyCoinData(): ArrayList<CouponList> {
        val resultList = ArrayList<CouponList>()
        for (it in 0..4) {
            resultList.add(
                CouponList(
                    id = null,
                    title = null,
                    points = null,
                    imageUrl = null,
                    isEligible = false
                )
            )
        }
        return resultList
    }
}