package com.noisefit.ui.challengeNew.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.response.Rewards
import com.noisefit.luna.databinding.RowChallengesRewardBinding
import com.noisefit_commans.ui.loadImage

class RewardsAdapter : RecyclerView.Adapter<RewardsAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<com.noisefit_commans.data.response.Rewards>()

    inner class ViewHolder(private val binding: RowChallengesRewardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rewards: com.noisefit_commans.data.response.Rewards) {
            binding.ivPrize.loadImage(binding.ivPrize.context, rewards.reward_url)
            binding.tvDescription.text = rewards.message
            binding.tvPosition.text = rewards.reward_title
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowChallengesRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(data: List<com.noisefit_commans.data.response.Rewards>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }
}