package com.noisefit.ui.reward.voucher.active

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.VoucherList
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveVoucherViewModel @Inject constructor(val rewardsRepository: RewardsRepository) :
    BaseViewModel() {
    private val _voucherList = MutableLiveData<List<VoucherList>>()
    val voucherList: LiveData<List<VoucherList>>
        get() = _voucherList


    fun getVoucherListData() {
        viewModelScope.launch {
            rewardsRepository.getVoucherList("active",false)
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
                                        getVoucherListData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let {
                                _voucherList.postValue(
                                    (it?.userVoucher
                                        ?: ArrayList<VoucherList>())
                                )
                            }

                        }
                    }
                }
        }
    }



}