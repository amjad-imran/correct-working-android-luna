package com.oreo.ui.workout.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowActivityDetailBinding
import com.noisefit.luna.databinding.RowOwdItemBinding
import com.noisefit.ui.workout.adapter.ActivityDetailAdapter
import com.noisefit_commans.data.model.DetailData
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OWDActivityData

class OWorkoutDetailslAdapter : RecyclerView.Adapter<OWorkoutDetailslAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OWDActivityData>()

    inner class ViewHolder(val binding: RowOwdItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detailData: OWDActivityData) {

            binding.tvTitle.text = detailData.title
            binding.tvValue.text = detailData.value
            binding.tvUnitValue.text = detailData.unit


            //Change code if workout params added
            if (bindingAdapterPosition < 2 && itemCount > 2) {
                binding.viewDivider.root.visible()
            } else {
                binding.viewDivider.root.invisible()
            }

            //Change code if workout params added
            if (bindingAdapterPosition == 0) {
                binding.dividerVertical.visible()
            } else {
                binding.dividerVertical.gone()
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OWorkoutDetailslAdapter.ViewHolder {
        val binding =
            RowOwdItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OWorkoutDetailslAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(data: List<OWDActivityData>) {
        mDataSet = data as ArrayList<OWDActivityData>
        notifyDataSetChanged()
    }
}