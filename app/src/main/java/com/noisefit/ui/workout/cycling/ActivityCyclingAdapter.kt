package com.noisefit.ui.workout.cycling

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowCyclingDetailsItemBinding
import com.noisefit_commans.data.model.DetailData


class ActivityCyclingAdapter :
    RecyclerView.Adapter<ActivityCyclingAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<DetailData>()

    inner class ViewHolder(val binding: RowCyclingDetailsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detailData: DetailData) {

            binding.tvTitle.text = detailData.title
            binding.tvSubtitle.text = detailData.value
            binding.tvUnitValue.text = detailData.unit
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowCyclingDetailsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(data: List<DetailData>) {
        mDataSet = data as ArrayList<DetailData>
        notifyDataSetChanged()
    }
}