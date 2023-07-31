package com.noisefit.ui.reward.voucher.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.VoucherList
import com.noisefit.luna.databinding.ItemActiveVoucherBinding
import com.noisefit_commans.ui.loadImage

class ActiveVoucherAdapter(val listener: OnRedeemClickListener) :
    RecyclerView.Adapter<ActiveVoucherAdapter.ViewHolder>() {
    private var voucherList = ArrayList<VoucherList>()

    inner class ViewHolder(val binding: ItemActiveVoucherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: VoucherList) {

            binding.ivBanner.loadImage(binding.ivBanner.context, resultData.imageUrl, R.drawable.image_placeholder_voucher)
            binding.root.setOnClickListener {
                resultData.id?.let { it1 -> listener.onItemClick(it1) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemActiveVoucherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return voucherList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(voucherList[position])
    }

    fun setDataSet(voucherResult: List<VoucherList>) {
        voucherList.clear()
        voucherList.addAll(voucherResult)
        notifyDataSetChanged()
    }

    interface OnRedeemClickListener {
        fun onItemClick(voucherId:Int)
    }
}