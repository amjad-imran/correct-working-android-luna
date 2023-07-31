package com.noisefit.ui.reward.voucher.voucherdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemHtRedeemAdapterBinding

class TCRedeemAdapter : RecyclerView.Adapter<TCRedeemAdapter.ViewHolder>() {
    private var resultList = ArrayList<String>()

    inner class ViewHolder(val binding: ItemHtRedeemAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: String) {
            binding.tvMessage.text = msg
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemHtRedeemAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return resultList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(resultList[position])
    }

    fun setDataSet(result: List<String>) {
        resultList.clear()
        resultList.addAll(result)
        notifyDataSetChanged()
    }

}