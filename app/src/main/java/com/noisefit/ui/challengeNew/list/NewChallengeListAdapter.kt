package com.noisefit.ui.challengeNew.list

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.R

import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.databinding.RowNewChallengeBinding
import com.noisefit.ui.common.*
import com.noisefit_commans.ui.*
import java.util.*


class NewChallengeListAdapter(
    private val listener: OnChallengeClickListener
) : RecyclerView.Adapter<NewChallengeListAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<com.noisefit_commans.data.response.ChallengeModel>()

    inner class ViewHolder(private val binding: RowNewChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(challengeModel: com.noisefit_commans.data.response.ChallengeModel) {

            binding.textViewTitle.text = challengeModel.title

            binding.imageViewBack.loadImage(binding.imageViewBack.context, challengeModel.image_url)

            binding.textViewStatus.text = challengeModel.getChallengeDisplayStatus()

            if (challengeModel.status.equals("ongoing"))
                binding.textViewStatus.setBackgroundResource(R.drawable.back_ongoing_challenge)
            else
                binding.textViewStatus.setBackgroundResource(R.drawable.back_upcoming_challenge)


            when (challengeModel.type?.lowercase()) {
                "step" -> {
                    binding.imageViewType.setImageResource(R.drawable.ic_steps)
                    binding.imageViewType.contentDescription = "ic_steps"
                    binding.imageViewType1.setImageResource(R.drawable.ic_steps)
                    binding.imageViewType1.contentDescription = "ic_steps"
                }
                "distance" -> {
                    binding.imageViewType.setImageResource(R.drawable.ic_distance)
                    binding.imageViewType.contentDescription = "ic_distance"
                    binding.imageViewType1.setImageResource(R.drawable.ic_distance)
                    binding.imageViewType1.contentDescription = "ic_distance"
                }
                "calories" -> {
                    binding.imageViewType.setImageResource(R.drawable.ic_calories)
                    binding.imageViewType.contentDescription = "ic_calories"
                    binding.imageViewType1.setImageResource(R.drawable.ic_calories)
                    binding.imageViewType1.contentDescription = "ic_calories"
                }
                else -> binding.imageViewType.setImageResource(0)
            }

            val dateStartData = challengeModel.getStartsInData()

            if (dateStartData.second.isEmpty()) {
                if (dateStartData.first.equals("0")) {//Show Ends in instead
                    /*val dateEndsData = challengeModel.getEndsInData()
                    var endText = "Ends in ${dateEndsData.first} ${dateEndsData.second}"
                    if (dateEndsData.second.isEmpty()) {
                        endText = "Ends ${dateEndsData.first}"
                    }*/
                    binding.textViewStartInDay.text = "Started Today"
                } else {
                    binding.textViewStartInDay.text = "Starts ${dateStartData.first}"
                }
            } else {
                binding.textViewStartInDay.text =
                    "Starts in ${dateStartData.first} ${dateStartData.second}"
            }

            setParticipants(binding, challengeModel)

            binding.root.setOnClickListener {
                listener.onChallengeClicked(challengeModel.challenge_id,challengeModel)
            }
        }
    }

    private fun setParticipants(binding: RowNewChallengeBinding, challengeModel: com.noisefit_commans.data.response.ChallengeModel) {
        if (challengeModel.participants != null && (challengeModel.participants?:0) > 0) {
            binding.tvParticipantValue.visible()
            binding.tvParticipantValue.text =
                "${
                    (challengeModel.participants?:0L).numberFormatter()
                } ${if (challengeModel.participants == 1L) "Participant" else "Participants"}"
            if (challengeModel.display_images.isNotEmpty()) {
                binding.lytImageSlider.visible()
                binding.imageViewType1.invisible()
                binding.imageViewType.visible()
                if (challengeModel.display_images.size == 1) {
                    binding.imageViewCircle1.visible()
                    binding.imageViewCircle2.gone()
                    binding.imageViewCircle3.gone()
                    loadImage(challengeModel.display_images[0], binding.imageViewCircle1)
                }
                if (challengeModel.display_images.size == 2) {
                    binding.imageViewCircle1.visible()
                    binding.imageViewCircle2.visible()
                    binding.imageViewCircle3.gone()
                    loadImage(challengeModel.display_images[0], binding.imageViewCircle1)
                    loadImage(challengeModel.display_images[1], binding.imageViewCircle2)
                }
                if (challengeModel.display_images.size >= 3) {
                    binding.imageViewCircle1.visible()
                    binding.imageViewCircle2.visible()
                    binding.imageViewCircle3.visible()
                    loadImage(challengeModel.display_images[0], binding.imageViewCircle1)
                    loadImage(challengeModel.display_images[1], binding.imageViewCircle2)
                    loadImage(challengeModel.display_images[2], binding.imageViewCircle3)
                }
            } else binding.lytImageSlider.invisible()
        } else {
            binding.imageViewType1.visible()
            binding.imageViewType.gone()
            binding.lytImageSlider.gone()
            binding.tvParticipantValue.gone()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowNewChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size
    fun loadImage(url: String, view: ImageView) {
        Glide.with(view.context)
            .load(url)
            .placeholder(R.drawable.ic_default_profile_image)
            .error(R.drawable.ic_default_profile_image)
            .into(view)
    }

    fun setDataSet(dataSet: List<com.noisefit_commans.data.response.ChallengeModel>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

}

interface OnChallengeClickListener {
    fun onChallengeClicked(challengeId: Int,challengeModel: com.noisefit_commans.data.response.ChallengeModel)
}