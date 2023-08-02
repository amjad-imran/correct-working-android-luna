package com.oreo.ui.helpsupport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoHsChildItemBinding
import com.oreo.data.model.OHSModel

class OHSChildAdapter(val listener: OHSParentAdapter.OHSClickListener) :
    RecyclerView.Adapter<OHSChildAdapter.ViewHolder>() {
    val mDataSet = ArrayList<OHSModel>()

    inner class ViewHolder(val binding: OreoHsChildItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: OHSModel) {

            binding.root.setOnClickListener {
                listener.onItemClickListener(resultData.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoHsChildItemBinding.inflate(
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

    fun setData(listData: ArrayList<OHSModel>) {
        mDataSet.clear()
        mDataSet.addAll(listData)
        notifyDataSetChanged()
    }


}