package com.noisefit.ui.npl.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.data.model.PredictionHistoryData
import com.noisefit.databinding.ItemPredictionHistoryBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.DateFormats

class PredictionHistoryAdapter(val listener: OnCollectRewardListener) :
    RecyclerView.Adapter<PredictionHistoryAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<PredictionHistoryData>()

    inner class ViewHolder(val binding: ItemPredictionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: PredictionHistoryData) {
            binding.tvTitle.text = AppConstants.teamNameMapping(null,resultData.teamName?:"")
            val result: String
            val winPoint: String
            if (resultData.result == 1) {
                binding.tvResult.setTextColor(
                    ContextCompat.getColor(
                        binding.tvResult.context,
                        R.color.steps_arc
                    )
                )
                result = "Won"
                winPoint = "+${resultData.points.toString()}"
            } else if (resultData.result == 2) {
                binding.tvResult.setTextColor(
                    ContextCompat.getColor(
                        binding.tvResult.context,
                        R.color.white_64
                    )
                )
                result = "Lost"
                winPoint = "0"
            } else {
                binding.tvResult.setTextColor(
                    ContextCompat.getColor(
                        binding.tvResult.context,
                        R.color.white_64
                    )
                )
                result = "No result"
                winPoint = resultData.points.toString()
            }
            binding.tvResult.text = result
            binding.tvTransAmount.text = winPoint

            if (!resultData.isCollect && resultData.result == 1) {
                binding.tvTransAmount.gone()
                binding.tvCollect.visible()
            } else {
                binding.tvTransAmount.visible()
                binding.tvCollect.gone()
            }

            binding.tvTransDate.text = resultData.date?.let {
                DateFormats.convertTimeStampToDateOnly(
                    it
                )
            }
            binding.ivUserProfile.loadImage(binding.ivUserProfile.context, resultData.imageUrl)
            binding.tvCollect.setOnClickListener {
                resultData.id?.let { it1 -> listener.onCollectReward(it1) }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemPredictionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: ArrayList<PredictionHistoryData>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

}

interface OnCollectRewardListener {
    fun onCollectReward(predictionId: Int)
}