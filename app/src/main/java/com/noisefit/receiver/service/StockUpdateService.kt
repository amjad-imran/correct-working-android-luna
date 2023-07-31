package com.noisefit.receiver.service

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.StockRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.StockInfoList
import com.noisefit_commans.models.StockSymbol
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject



@AndroidEntryPoint
class StockUpdateService @Inject
constructor() : LifecycleService() {


    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var stockRepository: StockRepository


    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {



        sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.StockListDataObtained -> {

                    it.stockSymbolList.stockSymbolList.forEach { stock ->
                        LOGS.d("Stock : ${stock.symbol}")
                    }

                    it.stockSymbolList.stockSymbolList.let { it1 ->
                        getStocks(it1)
                    }
                }
                else -> {}
            }
        }

        sessionManager.sendQueryAction(QueryAction.GetStockList)
        return super.onStartCommand(intent, flags, startId)
    }

    fun getStocks(symbols: java.util.ArrayList<StockSymbol>) {
        val symbolArray = ArrayList<String>()
        symbols.forEach {
            symbolArray.add(it.symbol)
        }

        val symbolString = symbolArray.joinToString(",")

        scope.launch {
            stockRepository.getStocksInfo(symbolString)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
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
                                if (stockList.isNotEmpty()) {
                                    syncStockWithDevice(stockList)
                                    val broadcastIntent = Intent().apply {
//                                        action = StockFragment.ACTION_STOCK
                                    }
                                    broadcastIntent.putExtra("stockList", stockList)
                                    sendBroadcast(broadcastIntent)
                                }
                            }
                        }
                        else -> {}
                    }
                }
        }
    }

    fun syncStockWithDevice(stockList: ArrayList<StockInfoList.Stock>) {
        val stocks = StockInfoList(
            stockInfoList = stockList
        )

        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SyncStockInfoList(
                stocks
            )
        )

        LOGS.d("Stocks Synced")
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


}