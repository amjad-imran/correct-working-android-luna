package com.oreo.ui.heartrate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.LayoutOHrInfoItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OHRInfoDataModel

class OHRInfoAdapter :
    RecyclerView.Adapter<OHRInfoAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OHRInfoDataModel>()

    inner class ViewHolder(val binding: LayoutOHrInfoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: OHRInfoDataModel) {
            if (resultData.title == null) {
                binding.tvHeader.gone()
            } else {
                binding.tvHeader.visible()
                binding.tvHeader.text = resultData.title
            }
            if (resultData.description == null) {
                binding.tvDescription.gone()
            } else {
                binding.tvDescription.visible()
                binding.tvDescription.text = resultData.description
            }
            if (resultData.banner == null) {
                binding.ivBanner.gone()
            } else {
                binding.ivBanner.loadImage(binding.ivBanner.context, resultData.banner)
            }
            if (bindingAdapterPosition == mDataSet.size - 1) {
                binding.divider1.root.gone()
            } else
                binding.divider1.root.visible()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutOHrInfoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: List<OHRInfoDataModel>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

}

