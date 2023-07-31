package com.noisefit.ui.content.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.ItemNeedForWorkoutBinding

class WContentNeedAdapter :
    RecyclerView.Adapter<WContentNeedAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<String>()

    inner class ViewHolder(val binding: ItemNeedForWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: String) {
            binding.tvTitle.text = "${bindingAdapterPosition + 1}. $result"

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemNeedForWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(result: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(result)
        notifyDataSetChanged()
    }


}