package com.noisefit.ui.reward.coin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.model.CouponList
import com.noisefit.databinding.ItemEarningToUseBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.common.clearDrawables

class CoinEarningAdapter(val listener: OnBannerClickListener) :
    RecyclerView.Adapter<CoinEarningAdapter.ViewHolder>() {
    private var allCouponListResult = ArrayList<CouponList>()

    inner class ViewHolder(val binding: ItemEarningToUseBinding) :
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
                binding.tvEarnedCoins.text = resultData.points.toString()
                binding.root.setOnClickListener {
                    listener.onBannerClick(resultData)
                }
                binding.view2.setOnClickListener {
                    listener.onBannerClick(resultData)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEarningToUseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return allCouponListResult.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(allCouponListResult[position])
    }

    fun setDataSet(allTaskResult: List<CouponList>) {
        allCouponListResult.clear()
        allCouponListResult.addAll(allTaskResult)
        notifyDataSetChanged()
    }

    interface OnBannerClickListener {
        fun onBannerClick(resultData: CouponList)
    }

}