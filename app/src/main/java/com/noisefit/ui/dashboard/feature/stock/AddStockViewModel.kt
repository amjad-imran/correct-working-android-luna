package com.noisefit.ui.dashboard.feature.stock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.stock.StockNetwork
import com.noisefit.data.repository.abstraction.StockRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStockViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : BaseViewModel() {

    private val _stocks = MutableLiveData<List<StockNetwork>>()

    private val _masterStockList = ArrayList<StockNetwork>()


    val stockList: LiveData<List<StockNetwork>>
        get() = _stocks


    fun getStockList() {
        if(_masterStockList.size>0){
            _stocks.postValue(_masterStockList)
            return
        }

        setLoading(true)
        viewModelScope.launch {
            stockRepository.getStockList().collect { resource ->
                when (resource) {
                    is Resource.NetworkError -> {
                        setLoading(false)
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getStockList()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        setLoading(false)
                        resource.data?.data?.let { stocks->
                            val filteredData = stocks.filter { it.name.isNotEmpty() }
                            _masterStockList.addAll(filteredData)
                            _stocks.postValue(filteredData)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun filterStockList(searchText: String) {
        if(searchText.isEmpty()){
            _stocks.postValue(_masterStockList)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _stocks.postValue(_masterStockList.filter {
                it.matches(searchText)
            })
        }
    }
}