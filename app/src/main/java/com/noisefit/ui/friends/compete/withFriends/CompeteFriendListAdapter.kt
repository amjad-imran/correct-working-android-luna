package com.noisefit.ui.friends.compete.withFriends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.response.CompeteFriend
import com.noisefit.luna.databinding.ItemCompeteFriendListBinding
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.StringUtils.capitalizeWords

class CompeteFriendListAdapter(val action: OnCompeteFriendInteractionListener) :
    RecyclerView.Adapter<CompeteFriendListAdapter.ViewHolder>() {

    val mDataSet = ArrayList<CompeteFriend>()

    inner class ViewHolder(val binding: ItemCompeteFriendListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CompeteFriend, position: Int) {
            binding.ivFriendProfile.apply {
                loadCircleImage(
                    this.context,
                    data.url,
                    R.drawable.ic_default_profile_image
                )
            }

            binding.tvFriendName.text = data.name
            binding.tvRequestStatus.text = data.status?.capitalizeWords()

            val joinedInterests = data.interest?.take(2)?.joinToString() ?: ""

            binding.tvInterestName.text =
                "${if (joinedInterests.isNotEmpty()) "$joinedInterests..." else ""}"
            if (joinedInterests.isNullOrEmpty())
                binding.tvInterestName.gone()
            else
                binding.tvInterestName.visible()
            binding.tvRequestStatus.setOnClickListener {
                action.onSendRequest(data, position)
            }
            if (data.status.equals("compete")) {
                binding.tvRequestStatus.enable()
                binding.tvRequestStatus.setTextColor(
                    binding.tvRequestStatus.resources.getColor(
                        R.color.accent_color_purple,
                        null
                    )
                )
            } else {
                binding.tvRequestStatus.disable()
                binding.tvRequestStatus.setTextColor(
                    binding.tvRequestStatus.resources.getColor(
                        R.color.white_50,
                        null
                    )
                )
            }
        }

    }

    private fun getInterest(data: List<String>?): String {
        return data?.joinToString(",") ?: ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCompeteFriendListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<CompeteFriend>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }

    fun setStatusPending(position: Int) {
        tryCatch {
            mDataSet[position].status = "pending"
            notifyItemChanged(position)
        }
    }

    interface OnCompeteFriendInteractionListener {
        fun onSendRequest(competeFriend: CompeteFriend, position: Int)
    }
}