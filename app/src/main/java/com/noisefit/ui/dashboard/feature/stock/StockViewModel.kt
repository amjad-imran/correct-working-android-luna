package com.noisefit.ui.dashboard.feature.stock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.models.StockInfoList
import com.noisefit_commans.models.StockSymbol
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.StockRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
    val stockRepository: StockRepository
) : BaseViewModel() {

    var deleteStockPosition:Int? = null

    private val _editMode = MutableLiveData<Boolean>()

    private val _stockList = MutableLiveData<List<StockInfoList.Stock>>()


    val stockList: LiveData<List<StockInfoList.Stock>>
        get() = _stockList

    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    var syncWithDevice = true


    fun setStockList(stockList: List<StockInfoList.Stock>) {
        _stockList.postValue(stockList)
    }

    /**
     * Return true if stock already present
     */
    fun hasStock(symbol: String): Boolean {
        _stockList.value?.let {
            it.forEach { stock ->
                if (stock.symbol.equals(symbol, true)) {
                    return true
                }
            }

        } ?: return false
        return false
    }

    fun getStocks(symbols: java.util.ArrayList<StockSymbol>) {
        syncWithDevice = true
        setLoading(true)
        val symbolArray = ArrayList<String>()
        symbols.forEach {
            symbolArray.add(it.symbol)
        }

        val symbolString = symbolArray.joinToString(",")

        viewModelScope.launch {
            stockRepository.getStocksInfo(symbolString)
                .collect { resource ->
                    when (resource) {
                        is Resource.NetworkError -> {
                            setLoading(false)
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getStocks(symbols)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            setLoading(false)
                            resource.data?.let { responseObject ->
                                val stockList = ArrayList<StockInfoList.Stock>()


                                if (symbolArray.size > 1) {

                                    symbolArray.forEach {

                                        if (responseObject.has(it)) {

                                            val symbolObject = responseObject.getAsJsonObject(it)

                                            try {
                                                stockList.add(
                                                    StockInfoList.Stock(
                                                        symbol = symbolObject.get("symbol").asString,
                                                        name = symbolObject.get("name").asString,
                                                        market = symbolObject.get("exchange").asString,
                                                        latestPrice = (symbolObject.get("close").asString).toFloat(),
                                                        previousClose = (symbolObject.get("open").asString).toFloat(),
                                                        change = (symbolObject.get("change").asString).toFloat(),
                                                        changePercent = (symbolObject.get("percent_change").asString).toFloat(),
                                                        timestamp = 0,
                                                        halted = 0,
                                                        delayMintue = 0
                                                    )
                                                )
                                            } catch (ignored: Exception) {
                                            }
                                        }
                                    }
                                } else {
                                    val hasSymbol = try {
                                        responseObject.get("symbol").asString
                                    } catch (exp: Exception) {
                                        null
                                    }

                                    if (hasSymbol != null) {

                                        try {
                                            stockList.add(
                                                StockInfoList.Stock(
                                                    symbol = responseObject.get("symbol").asString,
                                                    name = responseObject.get("name").asString,
                                                    market = responseObject.get("exchange").asString,
                                                    latestPrice = (responseObject.get("close").asString).toFloat(),
                                                    previousClose = (responseObject.get("open").asString).toFloat(),
                                                    change = (responseObject.get("change").asString).toFloat(),
                                                    changePercent = (responseObject.get("percent_change").asString).toFloat(),
                                                    timestamp = 0,
                                                    halted = 0,
                                                    delayMintue = 0
                                                )
                                            )

                                        } catch (ignored: Exception) {
                                        }
                                    }
                                }

                                if (symbols.isNotEmpty()) {
                                    if (stockList.isNotEmpty()) {
                                        _stockList.value = stockList
                                    }
                                } else {
                                    _stockList.value = stockList
                                }
                            }


                        }
                        else -> {}
                    }
                }
        }
    }

    fun getAndAddStock(stockSymbol: String) {
        syncWithDevice = false
        setLoading(true)

        viewModelScope.launch {
            stockRepository.getStocksInfo(stockSymbol)
                .collect { resource ->
                    when (resource) {
                        is Resource.NetworkError -> {
                            setLoading(false)
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getAndAddStock(stockSymbol)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            setLoading(false)
                            resource.data?.let { responseObject ->

                                var stock: StockInfoList.Stock? = null


                                val hasSymbol = try {
                                    responseObject.get("symbol").asString
                                } catch (exp: Exception) {
                                    null
                                }

                                if (hasSymbol != null) {

                                    try {
                                        stock =
                                            StockInfoList.Stock(
                                                symbol = responseObject.get("symbol").asString,
                                                name = responseObject.get("name").asString,
                                                market = responseObject.get("exchange").asString,
                                                latestPrice = (responseObject.get("close").asString).toFloat(),
                                                previousClose = (responseObject.get("open").asString).toFloat(),
                                                change = (responseObject.get("change").asString).toFloat(),
                                                changePercent = (responseObject.get("percent_change").asString).toFloat(),
                                                timestamp = 0,
                                                halted = 0,
                                                delayMintue = 0
                                            )

                                    } catch (ignored: Exception) {
                                    }
                                } else {
                                    sendMessage("Error Adding Stock $stockSymbol")
                                }

                                if (stock != null) {
                                    syncWithDevice = false

                                    if (_stockList.value == null) {
                                        _stockList.value = arrayListOf(stock)
                                    } else {
                                        val oldList = _stockList.value as ArrayList
                                        oldList.add(stock)
                                        _stockList.value = oldList
                                    }

                                }
                            }
                        }
                        else -> {}
                    }
                }
        }
    }

}