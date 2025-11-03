package com.noisefit.ui.settings.dataSharingVendors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemDataSharingSettingsBinding
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.setVisibilityByCondition
import com.oreo.data.model.dataSharingVendorModels.DataSharingVendorListResponseItem

class DataSharingVendorsAdapter(
    val clickListener: (DataSharingVendorListResponseItem) -> Unit
): RecyclerView.Adapter<DataSharingVendorsAdapter.MyViewHolder>() {

    private val mDataSet = ArrayList<DataSharingVendorListResponseItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding = ItemDataSharingSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        holder.onBind(mDataSet[position], clickListener, position != mDataSet.size-1)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    class MyViewHolder(
        private val binding: ItemDataSharingSettingsBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun onBind(
            item: DataSharingVendorListResponseItem,
            clickListener: (DataSharingVendorListResponseItem) -> Unit,
            displayBottomLine: Boolean
        ) {
            binding.tvGoogleFit.text = item.vendorName

            binding.root.setOnClickListener {
                clickListener.invoke(item)
            }

            binding.line241.setVisibilityByCondition(displayBottomLine)
            binding.icon.loadImage(binding.root.context, item.vendorIcon)
        }

    }

    fun updateItems(newItems: List<DataSharingVendorListResponseItem>) {
        mDataSet.clear()
        mDataSet.addAll(newItems)
        notifyDataSetChanged()
    }
}