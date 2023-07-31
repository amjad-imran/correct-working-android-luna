package com.noisefit.ui.myDevice.warranty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowMarketPlaceBinding

class WarrantyMarketAdapter(val listener: WarrantyMarketAction) :
    RecyclerView.Adapter<WarrantyMarketAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<String>()
    var selectedPosition = 0

    inner class ViewHolder(val binding: RowMarketPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(place: String) {
            binding.rbMain.text = place

            binding.rbMain.isChecked = selectedPosition == bindingAdapterPosition

            binding.rbMain.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    listener.onPlaceSelected(place)
                    selectedPosition = bindingAdapterPosition
                    notifyDataSetChanged()
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowMarketPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(data: Array<String>) {
        this.mDataSet.clear()
        this.mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    fun setSelected(selectedValue: String?) {
        mDataSet.forEachIndexed { index, s ->
            if (s.equals(selectedValue, true)) {
                selectedPosition = index
                notifyDataSetChanged()
                return@forEachIndexed
            }
        }
    }


}

interface WarrantyMarketAction {
    fun onPlaceSelected(selectedPlace: String)
}