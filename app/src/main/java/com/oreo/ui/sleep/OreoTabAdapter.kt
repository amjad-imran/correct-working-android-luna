package com.oreo.ui.sleep

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.OreoItemTabSelectorBinding
import com.noisefit_commans.ui.getColor


class OreoTabAdapter(val listener: OnTabActions) :
    RecyclerView.Adapter<OreoTabAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<String>()
    var selectedPosition: Int = 0

    inner class ViewHolder(val binding: OreoItemTabSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            if (selectedPosition == bindingAdapterPosition) {
                binding.tvTabTitle.setTextColor(binding.tvTabTitle.context.getColor(R.color.white))
//                binding.vBottom.visible()
            } else {
                binding.tvTabTitle.setTextColor(R.color.white_64.getColor())
//                binding.vBottom.invisible()
            }
            binding.tvTabTitle.text = date

            /* binding.tvTabTitle.setOnClickListener {
                 listener.onDateSelected(date)
                 selectedPosition = bindingAdapterPosition
                 notifyDataSetChanged()
             }*/

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoItemTabSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        selectedPosition = if (resultData.isNotEmpty()) resultData.size - 1 else 0
        notifyDataSetChanged()
    }

    fun getDate(position: Int): String? {
        return try {
            mDataSet[position]
        } catch (exp: Exception) {
            null
        }
    }

    fun updateSelectedPosition(position: Int) {
        val lastPos = selectedPosition
        selectedPosition = position
        notifyItemChanged(lastPos)
        notifyItemChanged(position)

    }
}

interface OnTabActions {
    fun onDateSelected(date: String, position: Int)
}