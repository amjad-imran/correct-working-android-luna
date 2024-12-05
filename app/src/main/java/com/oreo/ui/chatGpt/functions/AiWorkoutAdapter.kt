package com.oreo.ui.chatGpt.functions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowAiWorkoutBinding

class AiWorkoutAdapter : RecyclerView.Adapter<AiWorkoutAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<String>()

    inner class ViewHolder(val binding: RowAiWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowAiWorkoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }


}