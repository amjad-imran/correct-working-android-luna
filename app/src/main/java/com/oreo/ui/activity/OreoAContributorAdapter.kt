package com.oreo.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoItemSleepContributorBinding
import com.oreo.data.model.Contributors

class OreoAContributorAdapter(val mListener:ContributorItemClickListener) :
    RecyclerView.Adapter<OreoAContributorAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Contributors>()

    inner class ViewHolder(val binding: OreoItemSleepContributorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: Contributors) {

            binding.backContributors.setBackgroundResource(resultData.backgroundRes)

            binding.tvTitle.text = resultData.title
            binding.tvPrgValue.text = resultData.leftText
            binding.tvPrgValue.setTextColor(
                ContextCompat.getColor(
                    binding.tvPrgValue.context,
                    resultData.leftTextColor
                )
            )
            val progressValue: Int = if (resultData.barPercent == 0) {
                1
            } else {
                resultData.barPercent
            }
            binding.pbSteps.progress = progressValue
            val progressColor = ContextCompat.getColor(
                binding.pbSteps.context,
                resultData.barColor
            )
            binding.pbSteps.setIndicatorColor(
                progressColor
            )
            binding.root.setOnClickListener {
                mListener.onItemClick(mDataSet,bindingAdapterPosition)
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

    fun setData(resultData: ArrayList<Contributors>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }
    interface ContributorItemClickListener {
        fun onItemClick(resultData: ArrayList<Contributors>,position: Int)
    }
}

