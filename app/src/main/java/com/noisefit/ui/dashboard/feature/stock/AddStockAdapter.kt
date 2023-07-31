package com.noisefit.ui.dashboard.feature.stock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.response.stock.StockNetwork
import com.noisefit.databinding.RowAddStockBinding

class AddStockAdapter(val listener: AddStockListAction) :
    RecyclerView.Adapter<AddStockAdapter.ViewHolder>() {

    var mDataSet = ArrayList<StockNetwork>()

    inner class ViewHolder(val binding: RowAddStockBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(stock: StockNetwork) {
            binding.tvStocksName.text = stock.getDisplayName()
            binding.tvStockCountry.text = stock.country
            binding.tvStockExchange.text = stock.exchange
            binding.tvStockSymbol.text = stock.symbol

            binding.root.setOnClickListener {
                listener.onStockSelected(stock)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowAddStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<StockNetwork>) {
        mDataSet = dataSet as ArrayList<StockNetwork>
        notifyDataSetChanged()
    }
}

interface AddStockListAction {
    fun onStockSelected(stock: StockNetwork)
}