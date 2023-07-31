package com.noisefit.ui.reward.voucher.voucherdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.AvailCouponData
import com.noisefit_commans.data.model.VoucherDetailsData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoucherDetailViewModel @Inject constructor(val rewardsRepository: RewardsRepository) :
    BaseViewModel() {

    var id: Int? = null
    var comeFrom: String? = null
    var couponCode: String? = null
    var points: Int? = null
    var redeemUrl: String? = null
    var title: String? = null
    var brand: String? = null

    private val _voucherDetailsData = MutableLiveData<VoucherDetailsData>()
    val voucherDetailsData: LiveData<VoucherDetailsData>
        get() = _voucherDetailsData

    private val _couponDetailsData = MutableLiveData<VoucherDetailsData>()
    val couponDetailsData: LiveData<VoucherDetailsData>
        get() = _couponDetailsData
    private val _availCouponData = MutableLiveData<com.noisefit_commans.data.model.AvailCouponData>()
    val availCouponData: LiveData<com.noisefit_commans.data.model.AvailCouponData>
        get() = _availCouponData


    fun getUserVoucherDetailsData() {
        viewModelScope.launch {
            id?.let {
                rewardsRepository.getVoucherDetails(it, false)
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
                                            getUserVoucherDetailsData()
                                        }

                                        override fun no() {}
                                    }
                                })
                            }
                            is Resource.Success -> {
                                resource.data?.data.let {
                                    _voucherDetailsData.postValue(it)
                                }

                            }
                        }
                    }
            }
        }
    }

    fun getCouponDetailsData() {
        viewModelScope.launch {
            id?.let {
                rewardsRepository.getCouponDetails(it)
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
                                            getCouponDetailsData()
                                        }

                                        override fun no() {}
                                    }
                                })
                            }
                            is Resource.Success -> {
                                resource.data?.data.let {
                                    _couponDetailsData.postValue(it)
                                }

                            }
                        }
                    }
            }
        }
    }

    fun availCoupon() {
        viewModelScope.launch {
            id?.let {
                rewardsRepository.availCoupon(it)
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
                                            availCoupon()
                                        }

                                        override fun no() {}
                                    }
                                })
                            }
                            is Resource.Success -> {
                                resource.data?.data.let {
                                    _availCouponData.postValue(it)
                                }

                            }
                        }
                    }
            }
        }
    }

}