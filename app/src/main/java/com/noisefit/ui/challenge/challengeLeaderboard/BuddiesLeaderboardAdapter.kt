package com.noisefit.ui.challenge.challengeLeaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.luna.R
import com.noisefit_commans.data.response.Leadership
import com.noisefit.luna.databinding.RowBuddyLeaderBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.models.Units


class BuddiesLeaderboardAdapter() :
    RecyclerView.Adapter<BuddiesLeaderboardAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Leadership>()
    private var unit: Units = Units.METRIC
    private var challengeType: String = ""

    inner class ViewHolder(private val binding: RowBuddyLeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(leadership: Leadership) {
            binding.tvTeam.gone()
            binding.tvRank.text = if (leadership.user_rank != null) "${leadership.user_rank}" else ""
            binding.tvProgress.text =
                leadership.progress?.let { ApplicationUtils.getNumberAsPerUnit(challengeType, it, unit) }
            binding.tvName.text = leadership.first_name
            leadership.teamName?.let {
                if (it != "") {
                    binding.tvTeam.visible()
                    binding.tvTeam.text = it
                }
            }

            Glide.with(binding.iv.context)
                .load(leadership.image_url)
                .placeholder(R.drawable.ic_default_profile_image)
                .error(R.drawable.ic_default_profile_image)
                .into(binding.iv)
            binding.tvUnit.text = ApplicationUtils.getChallengeTypeUnit(challengeType, unit)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowBuddyLeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<Leadership>, unit: Units?, challengeType: String?) {
        if (unit != null) {
            this.unit = unit
        }
        if (challengeType != null) {
            this.challengeType = challengeType
        }
        mDataSet = dataSet as ArrayList<Leadership>
        notifyDataSetChanged()
    }
}