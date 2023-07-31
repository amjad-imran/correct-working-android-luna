package com.noisefit.ui.watchface.custom

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowGridColorBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class GridColorAdapter(val listener: GridColorAction) :
    RecyclerView.Adapter<GridColorAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<String>()

    var selected = -1

    inner class ViewHolder(val binding: RowGridColorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(color: String) {
            binding.ivMain.setBackgroundColor(Color.parseColor("#$color"))
            binding.ivMain.setOnClickListener {
                selected = bindingAdapterPosition
                listener.onColorSelected(color)
                notifyDataSetChanged()
            }
            if(selected==bindingAdapterPosition){
                binding.ivSelection.visible()
            }else{
                binding.ivSelection.gone()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowGridColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(gridColors: Array<String>) {
        mDataSet.clear()
        mDataSet.addAll(gridColors)
        notifyDataSetChanged()
    }

    fun setSelected(selectedColor: String) {
        mDataSet.forEachIndexed{ index,it->
            if(selectedColor.contains(it,true)){
                selected = index
                notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }
}

interface GridColorAction {
    fun onColorSelected(color: String)
}