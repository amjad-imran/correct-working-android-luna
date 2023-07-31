package com.oreo.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.databinding.OLayoutDmgraphItemBinding

class OreoDMAdapter : RecyclerView.Adapter<OreoDMAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Int>()

    inner class ViewHolder(val binding: OLayoutDmgraphItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: Int) {
            when (value) {
                120 -> {
                    binding.ivBars.setBackgroundResource(R.drawable.vertical_line)
                }
                100 -> {
                    binding.ivBars.setBackgroundResource(R.drawable.vertical_line_med)
                }
                60 -> {
                    binding.ivBars.setBackgroundResource(R.drawable.vertical_line_low)
                }
                else -> {
                    binding.ivBars.setBackgroundResource(R.drawable.vertical_line_inactive)
                }
            }
            val params: ViewGroup.LayoutParams = binding.ivBars.layoutParams
            params.height = value
            binding.ivBars.layoutParams = params

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OLayoutDmgraphItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: ArrayList<Int>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }
}