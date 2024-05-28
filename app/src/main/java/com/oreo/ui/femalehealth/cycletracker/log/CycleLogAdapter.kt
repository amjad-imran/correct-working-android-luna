package com.oreo.ui.femalehealth.cycletracker.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemCycleLogBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.oreo.data.model.FHFlowIconsModel

class CycleLogAdapter(val mListener: OnLogItemClick) :
    RecyclerView.Adapter<CycleLogAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<FHFlowIconsModel>()
    var lastSelectedPos = -1

    inner class ViewHolder(val binding: ItemCycleLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FHFlowIconsModel) {
            binding.ivItem.loadImage(binding.ivItem.context, data.icon)
            binding.tvTitle.text = data.symptomName
            if (lastSelectedPos == bindingAdapterPosition) {
                binding.ivTick.visible()
            } else
                binding.ivTick.gone()
            binding.ivItem.setOnClickListener {
                lastSelectedPos = bindingAdapterPosition
                mListener.onItemClick(data, bindingAdapterPosition)
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemCycleLogBinding.inflate(
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

    fun getSelectedValue():String{
        mDataSet.forEach {
            if(it.isChecked){
                return it.symptomShortName ?: ""
            }
        }
        return ""
    }

    fun setData(resultData: List<FHFlowIconsModel>?) {
        mDataSet.clear()
        notifyDataSetChanged()
        if (resultData != null) {
            mDataSet.addAll(resultData)
        }
        notifyDataSetChanged()
    }

    fun updateItem(data: FHFlowIconsModel, position: Int) {
        mDataSet[position].isChecked = !data.isChecked
        notifyItemChanged(position)
    }


}

interface OnLogItemClick {
    fun onItemClick(data: FHFlowIconsModel, position: Int)
}

