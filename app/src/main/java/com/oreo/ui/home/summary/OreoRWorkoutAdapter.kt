package com.oreo.ui.home.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoItemRecentWorkoutActivityBinding
import com.noisefit.luna.databinding.OreoItemWorkoutActivityBinding
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OActivityListModal

class OreoRWorkoutAdapter(val mListener:OnItemClickListener) :
    RecyclerView.Adapter<OreoRWorkoutAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OActivityListModal>()

    inner class ViewHolder(val binding: OreoItemRecentWorkoutActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: OActivityListModal) {
            binding.tvActivityName.text = resultData.getFormattedActivityName()
            binding.tvCalories.text = resultData.calories
            if (bindingAdapterPosition == mDataSet.size - 1) {
                binding.divider1.root.invisible()
            } else
                binding.divider1.root.visible()

            binding.root.setOnClickListener {
                mListener.onItemClick(resultData,bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoItemRecentWorkoutActivityBinding.inflate(
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

    fun setData(resultData: List<OActivityListModal>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClick(data: OActivityListModal,position: Int)
    }
}

