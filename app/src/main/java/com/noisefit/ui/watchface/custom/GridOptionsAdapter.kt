package com.noisefit.ui.watchface.custom

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.CustomGrid
import com.noisefit.databinding.RowGridOptionBinding

class GridOptionsAdapter(val listener: GridOptionSelectAction) : RecyclerView.Adapter<GridOptionsAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<CustomGrid>()
    inner class ViewHolder(val binding: RowGridOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data : CustomGrid) {

            (binding.root as ImageView).setImageResource(data.image)

            binding.root.setOnClickListener {
                listener.onGridSelected(data,bindingAdapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowGridOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(gridOptions: List<CustomGrid>) {
        mDataSet.clear()
        mDataSet.addAll(gridOptions)
        notifyDataSetChanged()
    }
}

interface GridOptionSelectAction {
    fun onGridSelected(gridData : CustomGrid,position: Int)
}