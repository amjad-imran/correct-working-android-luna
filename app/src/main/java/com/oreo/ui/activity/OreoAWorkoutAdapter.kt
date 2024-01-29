package com.oreo.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoItemRecentWorkoutActivityBinding
import com.noisefit.luna.databinding.OreoItemWorkoutActivityBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OActivityListModal

class OreoAWorkoutAdapter(val mListener: OnItemClickListener) :
    RecyclerView.Adapter<OreoAWorkoutAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OActivityListModal>()

    inner class ViewHolder(val binding: OreoItemRecentWorkoutActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: OActivityListModal) {

            binding.tvName.text = activity.getFormattedActivityName()
            binding.view16.root.invisible()
            binding.ivWorkoutImage.loadImage(binding.imageView8.context, activity.iconUrl)

            val time = DateFormats.convert24HourTo12(activity.startTime)
            if (time.isNotEmpty()) {
                binding.tvStart.text = "${time.lowercase()}"
            } else {
                binding.tvStart.text = "-"
            }

            binding.tvMin.text = if (activity.duration == null) {
                "- min"
            } else {
                "${activity.duration} min"
            }

            binding.tvCalories.text = "${activity.calories} kcal"


            if (activity.type.equals("apple", true)) {
                binding.tvImportedFrom.apply {
                    text = "Imported from Health"
                    visible()
                }
            } else if (activity.type.equals("google", true)) {
                binding.tvImportedFrom.apply {
                    text = "Imported from Google Fit"
                    visible()
                }
            } else {
                binding.tvImportedFrom.gone()
            }

            if (bindingAdapterPosition == mDataSet.size - 1) {
                binding.view16.root.invisible()
            } else
                binding.view16.root.visible()


            binding.root.setOnClickListener {
                mListener.onItemClick(activity, bindingAdapterPosition)
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

    interface OnItemClickListener {
        fun onItemClick(data: OActivityListModal, position: Int)
    }
}

