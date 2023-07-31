package com.noisefit.ui.content.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.LastWatchedList
import com.noisefit.luna.databinding.ItemLastWatchedBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible

class LastWatchedAdapter(val listener: OnLastItemClickListener) :
    RecyclerView.Adapter<LastWatchedAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<LastWatchedList>()

    inner class ViewHolder(val binding: ItemLastWatchedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: LastWatchedList) {
            binding.ivBanner.loadImage(binding.ivBanner.context, resultData.thumbnailUrl)
            binding.tvTitle.text = resultData.title
            binding.tvDuration.text = ApplicationUtils.formatTimeMinuteSec(resultData.duration ?: 0)
            binding.lytProgress.pbSteps.progress = resultData.duration?.let {
                resultData.progress?.let { it1 ->
                    ApplicationUtils.calculateProgress(
                        it.toInt(), it1
                    )
                }
            } ?: 0

            tryCatch {
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
                listener.onItemClick(resultData)
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemLastWatchedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(result: List<LastWatchedList>) {
        mDataSet.clear()
        mDataSet.addAll(result)
        notifyDataSetChanged()
    }

    interface OnLastItemClickListener {
        fun onItemClick(resultData: LastWatchedList)
    }
}