package com.noisefit.ui.reward.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.TransactionHistory
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(private val rewardRepository: RewardsRepository) :
    BaseViewModel() {
    private val _transHistoryList = MutableLiveData<List<TransactionHistory>>()
    val transHistoryList: LiveData<List<TransactionHistory>>
        get() = _transHistoryList

    fun getTransactionHistoryData() {
        viewModelScope.launch {
            rewardRepository.getTransactionHistory()
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
                                        getTransactionHistoryData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let {
                                var listData: java.util.ArrayList<TransactionHistory>
                                it?.transactionHistory.apply {
                                    listData =
                                        it?.transactionHistory as ArrayList<TransactionHistory>
                                }
                                _transHistoryList.postValue(listData)
//                                _transHistoryList.postValue(testUIDummyData())
                            }

                        }
                    }
                }
        }
    }



    //use for test ui with real values
    private fun testUIDummyData(): ArrayList<TransactionHistory> {
        return arrayListOf(
            TransactionHistory(
                title = "watch pairing",
                points = 20,
                status = "credited",
                transaction_at = 1676275089594
            )
        )
    }
}