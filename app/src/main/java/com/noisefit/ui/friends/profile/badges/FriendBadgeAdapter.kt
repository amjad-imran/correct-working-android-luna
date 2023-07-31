package com.noisefit.ui.friends.profile.badges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.FriendBadge
import com.noisefit.luna.databinding.RowFriendsBadgesBinding
import com.noisefit_commans.ui.gone
import com.noisefit.ui.trophies.TrophiesType
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.StringUtils.convertDoubleStringToInt
import com.noisefit_commans.utils.prettyCountDecimal

class FriendBadgeAdapter : RecyclerView.Adapter<FriendBadgeAdapter.ViewHolder>() {
    val mDataSet = ArrayList<FriendBadge>()

    inner class ViewHolder(val binding: RowFriendsBadgesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friendBadge: FriendBadge) {


            val trophyType =
                if (friendBadge.type.equals("steps")) TrophiesType.STEPS else TrophiesType.DISTANCE

            binding.ivNotification.gone()
            if (trophyType == TrophiesType.STEPS) {
                val displayValue = (friendBadge.value ?: 0.0).prettyCountDecimal()
                binding.tvGoalShort.text = displayValue
                binding.tvGoalData.text = "$displayValue Steps"

                when (friendBadge.badge_type) {
                    "daily" -> {
                        binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_milestones)
                    }
                    "lifetime" -> {
                        binding.ivTrophyImage.setImageResource(R.drawable.ic_steps_lifetime)
                    }
                }
            } else {
                val displayValue = DistanceUtil.getDistanceFromMetres(
                    (friendBadge.value?:0.0).toInt(),
                    Units.METRIC
                ).convertDoubleStringToInt().prettyCountDecimal()


                binding.tvGoalShort.text = displayValue
                binding.tvGoalData.text = "$displayValue KM"
                when (friendBadge.badge_type) {
                    "daily" -> {
                        binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_milestones)
                    }
                    "lifetime" -> {
                        binding.ivTrophyImage.setImageResource(R.drawable.ic_distance_lifettime)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowFriendsBadgesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<FriendBadge>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }
}