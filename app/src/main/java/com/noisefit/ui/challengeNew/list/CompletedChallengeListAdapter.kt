package com.noisefit.ui.challengeNew.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.luna.R
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.luna.databinding.RowCompletedChallengeBinding


class CompletedChallengeListAdapter(
    val listener: ChallengeCompleteRewardInteractionListener
) :
    RecyclerView.Adapter<CompletedChallengeListAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<com.noisefit_commans.data.response.ChallengeModel>()

    inner class ViewHolder(private val binding: RowCompletedChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(challengeModel: com.noisefit_commans.data.response.ChallengeModel) {
            binding.textViewTitle.text = challengeModel.title
            if (challengeModel.user_rank == null) {
                binding.textViewDays.text = "Goal Missed"
                binding.textViewRank.text = "You were so close"
            } else {
                binding.textViewDays.text =
                    binding.textViewDays.context.getString(R.string.text_my_rank)
                binding.textViewRank.text = challengeModel.user_rank.toString()
            }
            Glide.with(binding.imageViewProfile.context)
                .load(challengeModel.image_url)
                .into(binding.imageViewProfile)

            binding.root.setOnClickListener {
                listener.onItemClicked(challengeModel.challenge_id, challengeModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowCompletedChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size


    fun setDataSet(dataSet: List<com.noisefit_commans.data.response.ChallengeModel>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

}

interface ChallengeCompleteRewardInteractionListener {
    fun onItemClicked(challengeId: Int, challengeModel: com.noisefit_commans.data.response.ChallengeModel)
}