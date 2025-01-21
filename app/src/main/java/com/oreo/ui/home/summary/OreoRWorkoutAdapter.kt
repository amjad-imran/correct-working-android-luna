package com.oreo.ui.home.summary

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemOActivityListDetailBinding
import com.noisefit.luna.databinding.OreoItemRecentWorkoutActivityBinding
import com.noisefit.luna.databinding.OreoItemWorkoutActivityBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OActivityListModal

class OreoRWorkoutAdapter(val mListener: OnItemClickListener) :
    RecyclerView.Adapter<OreoRWorkoutAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OActivityListModal>()

    inner class ViewHolder(val binding: OreoItemRecentWorkoutActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: OActivityListModal) {

            binding.tvName.text = activity.getTranslatedActivityName()
            binding.view16.root.invisible()
            binding.ivWorkoutImage.loadImage(binding.imageView8.context, activity.iconUrl)

            val time = DateFormats.convert24HourTo12(activity.startTime)
            if (time.isNotEmpty()) {
                binding.tvStart.text = "${time.lowercase()}"
            } else {
                binding.tvStart.text = "-"
            }

            binding.tvMin.text = if (activity.duration == null) {
                binding.root.context.getString(R.string.text_hypen_min)
            } else {
                binding.root.context.getString(R.string.text_value_min, "${activity.duration}")
            }

            try {
                val calories = activity.calories?.toIntOrNull()
                if (calories == null || calories == 0) {
                    binding.lineCalories.root.gone()
                    binding.tvCalories.gone()
                } else {
                    binding.lineCalories.root.visible()
                    binding.tvCalories.visible()
                    binding.tvCalories.text = binding.root.context.getString(
                        R.string.text_value_kcal,
                        "${activity.calories}"
                    )
                }
            } catch (exp: Exception) {
                binding.tvCalories
            }


            if (activity.type.equals("apple", true)) {
                binding.tvImportedFrom.apply {
                    text = binding.root.context.getString(R.string.text_imported_from_health)
                    visible()
                }
            } else if (activity.type.equals("google", true)) {
                binding.tvImportedFrom.apply {
                    text = this.context.getString(R.string.text_imported_from_google_fit)
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

