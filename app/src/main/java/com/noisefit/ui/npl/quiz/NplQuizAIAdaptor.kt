package com.noisefit.ui.npl.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemQuizAnswerIndicatorBinding

class NplQuizAIAdaptor : RecyclerView.Adapter<NplQuizAIAdaptor.ViewHolder>() {
    private var mDataSet = ArrayList<Int>()

    inner class ViewHolder(val binding: ItemQuizAnswerIndicatorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(statusValue: Int) = when (statusValue) {
            0 -> {
                binding.ivIndicator.setImageResource(R.drawable.ic_default_circle)
            }
            1 -> {
                binding.ivIndicator.setImageResource(R.drawable.ic_correct_qq_circle)
            }
            2 -> {
                binding.ivIndicator.setImageResource(R.drawable.ic_wrong_qq_circle)
            }
            else -> binding.ivIndicator.setImageResource(R.drawable.ic_skip_qq_circle)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = ItemQuizAnswerIndicatorBinding.inflate(
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

    fun setData(data: ArrayList<Int>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, value: Int) {
        mDataSet[position] = value
        notifyItemChanged(position)
    }
}