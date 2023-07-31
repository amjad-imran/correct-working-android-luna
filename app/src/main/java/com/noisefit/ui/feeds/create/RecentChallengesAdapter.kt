package com.noisefit.ui.feeds.create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowRecentChallengeBinding
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit_commans.ui.loadImage


class RecentChallengesAdapter(private val listener: ChallengeActions) :
    RecyclerView.Adapter<RecentChallengesAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<ChallengeModel>()

    inner class ViewHolder(private val binding: RowRecentChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(challenge: ChallengeModel) {

            binding.tvChallengeTitle.text = challenge.title
            binding.tvRank.text = "${challenge.user_rank ?: 0}"

            binding.ivChallengeBanner.loadImage(binding.ivChallengeBanner.context,challenge.image_url,
                R.drawable.placeholder_banner)

            binding.root.setOnClickListener {
                listener.onChallengeSelected(challenge)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowRecentChallengeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }


    fun setDataSet(data: List<ChallengeModel>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size

}

interface ChallengeActions {
    fun onChallengeSelected(challenge: ChallengeModel)
}