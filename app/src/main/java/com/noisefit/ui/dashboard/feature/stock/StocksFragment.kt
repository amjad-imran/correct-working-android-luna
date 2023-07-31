package com.noisefit.ui.dashboard.feature.stock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentStocksBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.StockInfoList
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible


const val MAX_STOCK = 10
const val ADD_STOCK_KEY = "ADD_STOCK_KEY"

@AndroidEntryPoint
class StocksFragment : BaseFragment<FragmentStocksBinding>(FragmentStocksBinding::inflate),
    StockRowAction {

    private val viewModel: StockViewModel by viewModels()
    lateinit var touchHelper: ItemTouchHelper
    private val adapter by lazy { StockAdapter(this) }

    private var mIntentFilter: IntentFilter? = null


    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mIntentFilter = IntentFilter()
        mIntentFilter?.addAction(ACTION_STOCK)

        binding.textAddStock.text = getString(R.string.text_add_to_add_stock, MAX_STOCK)

        setRecycler()
        if (viewModel.stockList.value == null) {
            sessionManager.sendQueryAction(QueryAction.GetStockList)
            viewModel.setLoading(true)
        }
    }


    override fun initListener() {

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAddStockBottom.setOnClickListener {
            invokeAddStock()
        }
        binding.tvEdit.setOnClickListener {
            viewModel.setEditMode(true)
            sessionManager.logInsiderAppEvent(
                InsiderAppEvents.STOCKS_EDIT_CLICK)
        }
        binding.btnAddStock.setOnClickListener {
            invokeAddStock()
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.setEditMode(false)
            syncStockWithDevice()
        }

    }

    override fun subscribeObservers() {
        sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.DeleteStock -> {
                        stockRemoved()
                    }
                    else -> {}
                }
            }
        }

        sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.StockListDataObtained -> {
                    if (viewModel.syncWithDevice) {
                        viewModel.setLoading(false)
//                        it.stockSymbolList.stockSymbolList.forEach { stock ->
//                            LOGS.d("Stock : ${stock.symbol}")
//                        }

                        it.stockSymbolList.stockSymbolList.let { it1 ->
                            if (it1.isEmpty()) {
                                setStateNoData()
                                return@observe
                            }
                            setStateHasData()
                            viewModel.getStocks(it1)
                        }
                    }
                }
                is QueryCallback.DeleteStock -> {
                    stockRemoved()
                }
                else -> {}
            }
        }

        viewModel.editMode.observe(viewLifecycleOwner) {
            if (it) {
                setStateEdit()
            } else {
                setStateDefault()
            }
        }

        viewModel.stockList.observe(viewLifecycleOwner) {
            setStocks(it)
            updateStockCount()
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

    }

    private fun setRecycler() {
        binding.rvStocks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStocks.adapter = adapter

        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.rvStocks)

    }

    private fun invokeAddStock() {
        val count =
            if (viewModel.stockList.value == null) {
                0
            } else {
                viewModel.stockList.value!!.size
            }
        if (count >= MAX_STOCK) {
            val alertMessage = getString(R.string.text_max_stock_message, MAX_STOCK)
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.InfoAlertDialog(
                        getString(
                            R.string.text_max_stock_reached
                        ), alertMessage, getString(R.string.text_close)
                    )
                )
            )
            return
        }


        setFragmentResultListener(ADD_STOCK_KEY) { key, bundle ->
            val stockSymbol = bundle.getString("stockSymbol")

            stockSymbol?.let { it1 ->
                viewModel.setEditMode(true)
                if (!viewModel.hasStock(it1)) {
                    viewModel.getAndAddStock(it1)
                } else {
                    context.showShortToast("$it1 is already present")
                }
            }
        }
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.STOCKS_ADD_CLICK)
        navigate(
            StocksFragmentDirections.actionStocksFragmentToAddStockFragment()
        )
    }

    override fun onLongPressed(position: Int) {
        setStateEdit()
    }

    override fun onItemRemoved(position: Int, stock: StockInfoList.Stock) {
        binding.progressBar.root.visible()
        viewModel.deleteStockPosition = position
        sessionManager.sendUpdateQueryAction(UpdateDeviceAction.DeleteStock(stock.symbol))
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    override fun onResume() {
        super.onResume()
        context?.registerReceiver(stockRefreshReceiver, mIntentFilter)
    }

    override fun onPause() {
        context?.unregisterReceiver(stockRefreshReceiver)
        super.onPause()
    }

    private val stockRefreshReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (viewModel.editMode.value == false) {
                    val data = intent?.getParcelableArrayListExtra<StockInfoList.Stock>("stockList")
                    if (data != null) {
                        viewModel.setStockList(data)
                    }
                }
            } catch (exp: Exception) {
            }
        }
    }

    fun syncStockWithDevice() {
        val stockList = StockInfoList(
            stockInfoList = adapter.getCurrentDataSet() as ArrayList<StockInfoList.Stock>
        )

        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SyncStockInfoList(
                stockList
            )
        )
    }

    private fun setStateEdit() {
        /* binding.layoutToolbar.root.gone()
         binding.toolbarEdit.visible()
         binding.bEdit.gone()*/
        binding.tvEdit.gone()
        binding.btnSaveChanges.visible()
        binding.btnAddStockBottom.gone()
        adapter.setEditMode(true)
    }

    private fun setStateDefault() {
        binding.tvEdit.visible()
        binding.btnSaveChanges.gone()
        /*binding.layoutToolbar.root.visible()
        binding.toolbarEdit.gone()
        binding.bEdit.visible()*/
        adapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.btnAddStock.gone()
        binding.textView18.gone()
        binding.btnSaveChanges.gone()
        binding.tvStockCount.gone()
        binding.ivNoStock.visible()
        binding.textNoStock.visible()
        binding.textAddStock.visible()
        binding.btnAddStockBottom.visible()
        binding.tvEdit.gone()
    }

    private fun setStateHasData() {
        binding.btnAddStock.visible()
        binding.textView18.visible()
        binding.tvStockCount.visible()
        binding.ivNoStock.gone()
        binding.textNoStock.gone()
        binding.textAddStock.gone()
        binding.btnAddStockBottom.gone()
        if (viewModel.editMode.value != true) {
            binding.tvEdit.visible()
        }
    }

    private fun stockRemoved() {
        viewModel.deleteStockPosition?.let {
            val position = it
            adapter.removeItem(position)
            viewModel.deleteStockPosition = null
        }
        binding.progressBar.root.gone()
        updateStockCount()
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.STOCKS_DELETE_CLICK)
    }

    fun updateStockCount() {
        val dataCount = adapter.itemCount
        binding.tvStockCount.text = "($dataCount/$MAX_STOCK)"

        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }


    fun setStocks(stocks: List<StockInfoList.Stock>) {
        adapter.setDataSet(stocks)
//        if (viewModel.syncWithDevice) {
//            syncStockWithDevice()
//        }
    }

    companion object {
        var ACTION_STOCK = "com.noisefit.stock"
    }
}