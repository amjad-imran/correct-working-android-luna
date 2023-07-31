package com.noisefit.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit_commans.data.model.ShopBanner
import com.noisefit.luna.databinding.ViewImageSliderBinding


class BannerSliderAdapter(val listener: BannerAction) :
    RecyclerView.Adapter<BannerSliderAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<ShopBanner>()

    inner class ViewHolder(private val binding: ViewImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: ShopBanner) {

            Glide.with(binding.ivSliderImage.context)
                .load(image.img)
                .into(binding.ivSliderImage)
            binding.ivSliderImage.setOnClickListener {
                listener.onBannerClicked(image, position = bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ViewImageSliderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<ShopBanner>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}

interface BannerAction {
    fun onBannerClicked(banner: ShopBanner,position: Int)
}