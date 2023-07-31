package com.noisefit.ui.reward.voucher.expired

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.model.VoucherList
import com.noisefit.databinding.ItemExpireVouchersBinding
import com.noisefit_commans.ui.loadImage

class ExpiredVoucherAdapter : RecyclerView.Adapter<ExpiredVoucherAdapter.ViewHolder>() {
    private var voucherList = ArrayList<VoucherList>()

    inner class ViewHolder(val binding: ItemExpireVouchersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: VoucherList) {
            binding.ivBanner.loadImage(
                binding.ivBanner.context, resultData.imageUrl, R.drawable.image_placeholder_voucher
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemExpireVouchersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

}