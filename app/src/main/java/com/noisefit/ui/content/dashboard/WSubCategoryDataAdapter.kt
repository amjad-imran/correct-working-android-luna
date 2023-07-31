package com.noisefit.ui.content.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.VideosList
import com.noisefit.luna.databinding.ItemWorkoutSubcategoryBinding
import com.noisefit.ui.common.*
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible

class WSubCategoryDataAdapter(val listener: OnItemVideoClickListener) :
    RecyclerView.Adapter<WSubCategoryDataAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<VideosList>()

    inner class ViewHolder(val binding: ItemWorkoutSubcategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: VideosList) {
            binding.ivBanner.loadImage(binding.ivBanner.context, resultData.thumbnailUrl)
            binding.tvTitle.text = resultData.title
            if (resultData.views != null) {
                if (resultData.views == 0) {
                    binding.tvViewsCount.invisible()
                } else {
                    binding.tvViewsCount.visible()
                    binding.tvViewsCount.text =
                        resultData.views.toString() + " view${ if (resultData.views > 1) "s" else "" }"
                }
            }

            tryCatch {
                binding.lytTrackTime.tvTime.text =
                    ApplicationUtils.formatTimeMinuteSec(resultData.duration ?: 0)
                if (resultData.tags.isNullOrEmpty()) return@tryCatch
                val temSplitTag = resultData.tags.split(",").toTypedArray()
                if (temSplitTag.isNotEmpty()) {
                    if (temSplitTag.size == 1) {
                        binding.tvTag1.text = temSplitTag[0]
                        binding.imageView1.gone()
                        binding.tvTag2.gone()
                    } else {
                        binding.tvTag1.text = temSplitTag[0]
                        binding.imageView1.visible()
                        binding.tvTag2.text = temSplitTag[1]
                    }
                }
            }


            binding.root.setOnClickListener {
                listener.onVideoItemClick(resultData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemWorkoutSubcategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(result: List<VideosList>) {
        mDataSet.clear()
        mDataSet.addAll(result)
        notifyDataSetChanged()
    }

    interface OnItemVideoClickListener {
        fun onVideoItemClick(resultData: VideosList)
    }
}