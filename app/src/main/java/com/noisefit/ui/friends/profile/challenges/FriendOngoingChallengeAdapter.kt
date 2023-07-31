package com.noisefit.ui.friends.profile.challenges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.databinding.RowFriendChallengeBinding

class FriendOngoingChallengeAdapter :
    RecyclerView.Adapter<FriendOngoingChallengeAdapter.ViewHolder>() {

    var mDataSet = ArrayList<com.noisefit_commans.data.response.ChallengeModel>()

    inner class ViewHolder(val binding: RowFriendChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(challengeModel: com.noisefit_commans.data.response.ChallengeModel) {
            binding.textViewTitle.text = challengeModel.title
            binding.imageViewProfile.loadImage(
                binding.imageViewProfile.context,
                challengeModel.image_url
            )

            if (challengeModel.user_rank == null || challengeModel.user_rank == 0) {
                val dateStartData = challengeModel.getStartsInData()
                binding.textViewDays.gone()
                if (dateStartData.second.isEmpty()) {
                    if (dateStartData.first.equals("0")) {
                        binding.textViewRank.gone()
                    } else {
                        binding.textViewRank.visible()
                        binding.textViewRank.text = "Starts ${dateStartData.first}"
                    }
                } else {
                    binding.textViewRank.visible()
                    binding.textViewRank.text =
                        "Starts in ${dateStartData.first} ${dateStartData.second}"
                }
            } else {
                binding.textViewDays.visible()
                binding.textViewRank.visible()
                binding.textViewRank.text = "${challengeModel.user_rank}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowFriendChallengeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<com.noisefit_commans.data.response.ChallengeModel>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }
}