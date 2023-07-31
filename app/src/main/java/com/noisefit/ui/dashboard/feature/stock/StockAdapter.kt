package com.noisefit.ui.dashboard.feature.stock

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.R
import com.noisefit.databinding.RowStockBinding
import com.noisefit_commans.models.StockInfoList
import com.noisefit_commans.ui.gone
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import java.lang.StringBuilder
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

class StockAdapter(private val action: StockRowAction) :
    RecyclerView.Adapter<StockAdapter.ViewHolder>(), ItemMoveCallbackListener.Listener {

    private var mDataSet = ArrayList<StockInfoList.Stock>()

    private var mEditMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowStockBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(stock: StockInfoList.Stock) {
            binding.tvStocksName.text = stock.name
            binding.tvStockSymbol.text = stock.market
            binding.tvStockSymbolOther.text = stock.symbol


            val status = getStockStatus(stock)


            Glide.with(binding.ivStockStatus.context)
                .load(status.first)
                .into(binding.ivStockStatus)


            binding.tvPrice.text = status.second



            if (mEditMode) {
                binding.ivRemove.visible()
            } else {
                binding.ivRemove.gone()
            }
            binding.root.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    action.onLongPressed(bindingAdapterPosition)
                    return true
                }
            })
            binding.ivDrag.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    action.onStartDrag(this)
                }
                return@setOnTouchListener true
            }


            binding.ivRemove.setOnClickListener {
                action.onItemRemoved(bindingAdapterPosition,stock)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])


    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mDataSet, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mDataSet, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }


    fun setDataSet(stocks: List<StockInfoList.Stock>) {
        mDataSet = stocks as ArrayList<StockInfoList.Stock>
        notifyDataSetChanged()
    }

    fun getCurrentDataSet(): List<StockInfoList.Stock> {
        return mDataSet
    }

    fun removeItem(position: Int) {
        try {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        }catch (exp : ArrayIndexOutOfBoundsException){
            exp.printStackTrace()
            //CASE : when Swap is in progress
        }

    }

    private fun getStockStatus(stock: StockInfoList.Stock): Pair<Int?, String> {

        if (stock.latestPrice == null || stock.previousClose == null) return Pair(null, "")

        val change = (stock.latestPrice!! - stock.previousClose!!)

        val changePercent = getChangeValue((change / stock.previousClose!!) * 100)

        val changeImage = if (changePercent < 0) {
            R.drawable.ic_stock_down
        } else {
            R.drawable.ic_stock_up
        }




        val statusString = StringBuilder()

        statusString.append(String.format("%.2f", stock.latestPrice))
        statusString.append("  ")
        if(change>=0){
            statusString.append("+")
        }
        statusString.append(String.format("%.2f", change))
        statusString.append("  ")
        if(changePercent>=0){
            statusString.append("+")
        }
        statusString.append(String.format("%.2f", changePercent))
        statusString.append("%")

        return Pair(changeImage,statusString.toString())
    }

    private fun getChangeValue(number : Float)  :Double{
        return try {
            number.toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
        }catch (exp : Exception){
            0.0
        }
    }
}

interface StockRowAction {
    fun onLongPressed(position: Int)
    fun onItemRemoved(position: Int, stock: StockInfoList.Stock)
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}