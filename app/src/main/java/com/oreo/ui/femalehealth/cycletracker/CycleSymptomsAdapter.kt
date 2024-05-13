package com.oreo.ui.femalehealth.cycletracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemCycleLogBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.oreo.data.model.FlowLog

class CycleSymptomsAdapter(val mListener: OnSymptomsItemClick) :
    RecyclerView.Adapter<CycleSymptomsAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<FlowLog>()

    inner class ViewHolder(val binding: ItemCycleLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FlowLog) {
            binding.ivItem.loadImage(binding.ivItem.context, data.image)
            binding.tvTitle.text = data.title
            if (data.isChecked) {
                binding.ivTick.visible()
            } else
                binding.ivTick.gone()
            binding.ivItem.setOnClickListener {
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

    fun setData(resultData: List<FlowLog>?) {
        mDataSet.clear()
        if (resultData != null) {
            mDataSet.addAll(resultData)
        }
        notifyDataSetChanged()
    }

    fun updateItem(data: FlowLog, position: Int) {
        mDataSet[position].isChecked = !data.isChecked
        notifyItemChanged(position)
    }

    fun getUpdatedSelectedListData(): ArrayList<String> {
        val selectedList = ArrayList<String>()
        mDataSet.forEach {
            if (it.isChecked) {
                selectedList.add(it.title ?: "")
            }
        }
        return selectedList
    }

}

interface OnSymptomsItemClick {
    fun onItemClick(data: FlowLog, position: Int)
}

