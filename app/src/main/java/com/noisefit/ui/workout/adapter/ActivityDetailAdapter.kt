package com.noisefit.ui.workout.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.DetailData
import com.noisefit.databinding.RowActivityDetailBinding

class ActivityDetailAdapter : RecyclerView.Adapter<ActivityDetailAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<DetailData>()

    inner class ViewHolder(val binding: RowActivityDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detailData: DetailData) {

            binding.tvTitle.text = detailData.title
            binding.tvValue.text = detailData.value
            binding.tvUnitValue.text = detailData.unit
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowActivityDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(data : List<DetailData>) {
        mDataSet = data as ArrayList<DetailData>
        notifyDataSetChanged()
    }
}