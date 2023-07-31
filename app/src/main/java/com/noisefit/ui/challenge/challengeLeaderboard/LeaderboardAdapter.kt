package com.noisefit.ui.challenge.challengeLeaderboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.luna.R
import com.noisefit_commans.data.response.Leadership
import com.noisefit.luna.databinding.RowAllLeaderBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_ADD
import com.noisefit_commans.utils.AppConstants
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.models.Units


class LeaderboardAdapter(
    private val listener: AllCardClickListener?, private val context: Context?
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Leadership>()
    private var unit: Units = Units.METRIC
    private var challengeType: String = ""

    inner class ViewHolder(private val binding: RowAllLeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(leadership: Leadership, position: Int) {
            binding.ivAdd.setOnClickListener {
                listener?.onCardClicked(leadership, position)
            }
            binding.tvTeam.gone()
            binding.tvRank.text = leadership.user_rank.toString()
            binding.tvProgress.text =
                leadership.progress?.let { ApplicationUtils.getNumberAsPerUnit(challengeType, it, unit) }
            binding.tvName.text = leadership.first_name
            leadership.teamName?.let {
                if (it != "") {
                    binding.tvTeam.visible()
                    binding.tvTeam.text = "/ " + it
                }
            }

            Glide.with(binding.iv.context).load(leadership.image_url)
                .placeholder(R.drawable.ic_default_profile_image)
                .error(R.drawable.ic_default_profile_image).into(binding.iv)

            binding.tvUnit.text = ApplicationUtils.getChallengeTypeUnit(challengeType, unit)
            var bgId = R.drawable.back_leader
            var clickable = true
            var buttonVisible = true
            var buttonImageId = R.drawable.add_buddy
            if (leadership.status != null) {
                if (leadership.status == AppConstants.BUDDY_STATUS_ADD || leadership.status == AppConstants.BUDDY_STATUS_REJECTED) {
                    clickable = false
                    buttonImageId = R.drawable.add_buddy_disabled
                }
                if (leadership.status == AppConstants.BUDDY_STATUS_ACCEPT) {
                    buttonVisible = false
                    bgId = R.drawable.back_buddy_leader
                }
            } else {
                buttonVisible = false
                bgId = R.drawable.back_self_leader
            }
            if (leadership.mobile == null) {
                buttonVisible = false
            } else if (leadership.mobile == "") {
                buttonVisible = false
            }
            if (ApplicationUtils.isOutSideIndia()) {
                buttonVisible = false
            }
//            if (buttonVisible) {
//                binding.ivAdd.visible()
//            } else {
//                binding.ivAdd.gone()
//            }
            binding.ivAdd.gone()
            binding.ivAdd.isClickable = clickable
            binding.ivAdd.setImageDrawable(context?.let {
                ContextCompat.getDrawable(
                    it, buttonImageId
                )
            })
            binding.llMain.background = context?.let { ContextCompat.getDrawable(it, bgId) }


            var showMedal = false
            var medalImage: Int? = null
            if (bindingAdapterPosition == 0) {
                showMedal = true
                medalImage = R.drawable.ic_medal_gold
            } else if (bindingAdapterPosition == 1) {
                showMedal = true
                medalImage = R.drawable.ic_medal_silver
            } else if (bindingAdapterPosition == 2) {
                showMedal = true
                medalImage = R.drawable.ic_medal_bronze
            }

            if (showMedal) {
                binding.ivMedal.visible()
                binding.tvRank.gone()
                if (medalImage != null) {
                    binding.ivMedal.setImageResource(medalImage)
                }
            } else {
                binding.ivMedal.gone()
                binding.tvRank.visible()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowAllLeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount(): Int = mDataSet.size

    fun updateStatus(position: Int) {
        mDataSet[position].status = FRIEND_STATUS_ADD
        notifyItemChanged(position)
    }

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

interface AllCardClickListener {
    fun onCardClicked(leadership: Leadership, position: Int)
}