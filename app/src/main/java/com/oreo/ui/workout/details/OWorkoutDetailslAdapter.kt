package com.oreo.ui.workout.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowActivityDetailBinding
import com.noisefit.luna.databinding.RowOwdItemBinding
import com.noisefit.ui.workout.adapter.ActivityDetailAdapter
import com.noisefit_commans.data.model.DetailData
import com.oreo.data.model.OWDActivityData

class OWorkoutDetailslAdapter : RecyclerView.Adapter<OWorkoutDetailslAdapter.ViewHolder> (){
    private var mDataSet = ArrayList<OWDActivityData>()

    inner class ViewHolder(val binding: RowOwdItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detailData: OWDActivityData) {

            binding.tvTitle.text = detailData.title
            binding.tvValue.text = detailData.value
            binding.tvUnitValue.text = detailData.unit
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OWorkoutDetailslAdapter.ViewHolder {
        val binding =
            RowOwdItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OWorkoutDetailslAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(data : List<OWDActivityData>) {
        mDataSet = data as ArrayList<OWDActivityData>
        notifyDataSetChanged()
    }
}