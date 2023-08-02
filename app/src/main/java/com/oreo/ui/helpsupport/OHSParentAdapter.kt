package com.oreo.ui.helpsupport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoHsParentItemBinding
import com.oreo.data.model.OHSModel

class OHSParentAdapter:RecyclerView.Adapter<OHSParentAdapter.ViewHolder>() {
    val mDataSet = ArrayList<OHSModel>()

    inner class ViewHolder(val binding: OreoHsParentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: OHSModel) {


        }
    }
    interface OHSClickListener {
        fun onItemClickListener(id: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoHsParentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: OHSParentAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(listData: ArrayList<OHSModel>) {
        mDataSet.clear()
        mDataSet.addAll(listData)
        notifyDataSetChanged()
    }
}