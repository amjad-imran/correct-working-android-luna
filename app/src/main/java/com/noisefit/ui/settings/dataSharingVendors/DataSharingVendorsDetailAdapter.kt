package com.noisefit.ui.settings.dataSharingVendors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowGoogleFitItemsBinding
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.dataSharingVendorModels.Feature

class DataSharingVendorsDetailAdapter(): RecyclerView.Adapter<DataSharingVendorsDetailAdapter.MyViewHolder>() {

    var isToggleOn: Boolean ?= false
    private val mDataSet = ArrayList<Feature>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding = RowGoogleFitItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        holder.onBind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    inner class MyViewHolder(
        private val binding: RowGoogleFitItemsBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun onBind(
            item: Feature
        ) {
            binding.tvName.text = item.name
            binding.switchMain.isChecked = isToggleOn ?: false
            binding.switchMain.disable()
            binding.switchMain.isClickable = false
            binding.imageView.loadImage(binding.root.context, item.icon)
        }

    }

    fun updateItems(newItems: List<Feature>) {
        mDataSet.clear()
        mDataSet.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateToggles(isChecked: Boolean){
        isToggleOn = isChecked
        notifyDataSetChanged()
    }
}