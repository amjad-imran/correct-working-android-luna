package com.oreo.ui.sleep

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoItemSleepContributorBinding
import com.oreo.data.model.Contributors

class OreoSleepContributorAdapter(val mListener: ContributorItemClickListener) :
    RecyclerView.Adapter<OreoSleepContributorAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Contributors>()
    private var mVersion: Int = 1

    inner class ViewHolder(val binding: OreoItemSleepContributorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: Contributors) {
            binding.tvTitle.text = resultData.title
            binding.backContributors.setBackgroundResource(resultData.backgroundRes)
            binding.tvPrgValue.text = resultData.leftText
            binding.tvPrgValue.setTextColor(
                ContextCompat.getColor(
                    binding.tvPrgValue.context,
                    resultData.leftTextColor
                )
            )

             if (resultData.barPercent == 0) {
                binding.backContributors.alpha = .5f
            }else{
                 binding.backContributors.alpha = 1f
             }

            val progressColor = ContextCompat.getColor(
                binding.pbSteps.context,
                resultData.barColor
            )

            binding.pbSteps.progress = resultData.barPercent
            binding.pbSteps.setIndicatorColor(
                progressColor
            )

            binding.root.setOnClickListener {
                mListener.onItemClick(mDataSet, bindingAdapterPosition,mVersion)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoItemSleepContributorBinding.inflate(
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

    fun setData(resultData: List<Contributors>, version: Int) {
        mVersion = version
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

    interface ContributorItemClickListener {
        fun onItemClick(resultData: ArrayList<Contributors>, position: Int,version: Int)
    }
}

