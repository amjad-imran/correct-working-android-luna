package com.noisefit.ui.friends.profile.challenges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.databinding.RowFriendChallengeBinding
import com.noisefit_commans.ui.loadImage

class FriendBestPerformChallengeAdapter :
    RecyclerView.Adapter<FriendBestPerformChallengeAdapter.ViewHolder>() {
    val mDataSet = ArrayList<com.noisefit_commans.data.response.ChallengeModel>()

    inner class ViewHolder(val binding: RowFriendChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(challengeModel: com.noisefit_commans.data.response.ChallengeModel) {

            binding.textViewTitle.text = challengeModel.title
            binding.imageViewProfile.loadImage(
                binding.imageViewProfile.context,
                challengeModel.image_url
            )
            binding.textViewRank.text = "${challengeModel.user_rank}"

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