package com.noisefit.ui.reward.deals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.CouponList
import com.noisefit.luna.databinding.ItemAllDealsBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.common.clearDrawables


class AllDealsAdapter(val listener: OnDealsItemClickListener) :
    RecyclerView.Adapter<AllDealsAdapter.ViewHolder>() {
    private var allTasksResult = ArrayList<CouponList>()

    inner class ViewHolder(val binding: ItemAllDealsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: CouponList) {
            if (resultData.imageUrl.isNullOrEmpty()) {
                binding.view3.visible()
                binding.imageView21.visible()
                binding.ivBanner.gone()
                binding.tvEarnedCoins.clearDrawables()
                binding.tvEarnedCoins.text =
                    binding.tvEarnedCoins.context.getString(R.string.text_coming_soon)
            } else {
                binding.ivBanner.visible()
                binding.view3.gone()
                binding.imageView21.gone()
                binding.ivBanner.loadImage(binding.ivBanner.context, resultData.imageUrl,R.drawable.image_placeholder_voucher)
                binding.tvEarnedCoins.text = "Avail for ${resultData.points}"
                binding.root.setOnClickListener {
                    listener.onDealsClick(resultData)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAllDealsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return allTasksResult.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(allTasksResult[position])
    }

    fun setDataSet(allTaskResult: List<CouponList>) {
        allTasksResult.clear()
        allTasksResult.addAll(allTaskResult)
        notifyDataSetChanged()
    }

    interface OnDealsItemClickListener {
        fun onDealsClick(resultData: CouponList)
    }

}