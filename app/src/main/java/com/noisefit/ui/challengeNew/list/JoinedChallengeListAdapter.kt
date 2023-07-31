package com.noisefit.ui.challengeNew.list

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.noisefit.luna.R

import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.luna.databinding.RowAwaitedChallengeBinding
import com.noisefit.luna.databinding.RowJoinedChallengeBinding
import com.noisefit.luna.databinding.RowNewChallengeBinding
import com.noisefit.ui.common.*
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import java.util.*
import kotlin.collections.ArrayList


class JoinedChallengeListAdapter(val unit: Units) :
    RecyclerView.Adapter<JoinedChallengeListAdapter.HomeRecycler>() {
    val mDataSet = ArrayList<com.noisefit_commans.data.response.ChallengeModel>()

    companion object {
        private const val LAYOUT_ONE = 0
        private const val LAYOUT_TWO = 1
        private const val LAYOUT_THREE = 3

    }

    var itemClickListener: ((position: Int, challengeModel: ChallengeModel) -> Unit)? = null

    sealed class HomeRecycler(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        var itemClickListener: ((position: Int, challengeModel: ChallengeModel) -> Unit)? = null
        fun loadImage(url: String, view: ImageView) {
            Glide.with(view.context).load(url).into(view)
        }


        class ViewHolderJoined(val joinedBinding: RowJoinedChallengeBinding, val unit: Units) :
            HomeRecycler(joinedBinding) {
            fun bind(challengeModel: com.noisefit_commans.data.response.ChallengeModel) {

                joinedBinding.textViewTitle.text = challengeModel.title
                joinedBinding.textViewStatus.text =
                    if (challengeModel.status.equals("upcoming", true)) {
                        "Exclusive"
                    } else {
                        challengeModel.status?.capitalizeWords()
                    }

                joinedBinding.imageViewBack.loadImage(
                    joinedBinding.imageViewBack.context, challengeModel.image_url
                )

                val challengeGoal = challengeModel.goal ?: 0.0
                var todayMsg = ""
                if (challengeModel.status.equals("ongoing")) todayMsg = "(Today)"

                joinedBinding.textViewProgress.text = challengeModel.getFormattedProgress()
                joinedBinding.textViewGoal.text =
                    "/" + ApplicationUtils.getNumberAsPerUnit(
                        challengeModel.type.toString(),
                        challengeGoal,
                        unit
                    ) + " " + ApplicationUtils.getChallengeTypeUnit(
                        challengeModel.type.toString(),
                        unit
                    ) + "  " + todayMsg

                if ((challengeModel.user_rank ?: 0) > 0) {
                    joinedBinding.textViewMyRank.visible()
                    joinedBinding.textViewMyRankValue.visible()
                    joinedBinding.textViewMyRankValue.text = challengeModel.user_rank.toString()
                } else {
                    joinedBinding.textViewMyRank.gone()
                    joinedBinding.textViewMyRankValue.gone()
                }

                joinedBinding.layoutProgress.pbSteps.setIndicatorColor1(R.color.accent_color)
                var userProgress = challengeModel.progress ?: 0f

                if (userProgress > challengeGoal) {
//                    userProgress =
//                        userProgress.challengeCompletePercentage(challengeGoal.toFloat())
                    userProgress = 100f
//                    joinedBinding.layoutProgress.pbSteps.setTrackColor1(R.color.challenge_completed_pg)
                } else {
                    joinedBinding.layoutProgress.pbSteps.setTrackColor1(R.color.progress_track_color)
                    userProgress = userProgress.calculatePercentage(challengeGoal.toFloat())

                }

                joinedBinding.layoutProgress.pbSteps.progress = userProgress.toInt()
                joinedBinding.root.setOnClickListener {
                    itemClickListener?.invoke(challengeModel.challenge_id, challengeModel)
                }
            }


        }

        class ViewHolderUpcoming(val binding: RowNewChallengeBinding) :
            HomeRecycler(binding) {
            fun bind(challengeModel: com.noisefit_commans.data.response.ChallengeModel) {
                binding.textViewTitle.text = challengeModel.title
                Glide.with(binding.imageViewBack.context).load(challengeModel.image_url)
                    .into(binding.imageViewBack)
                binding.textViewStatus.text =  if (challengeModel.status.equals("upcoming", true)) {
                    "Exclusive"
                } else {
                    challengeModel.status?.capitalizeWords()
                }
                if (challengeModel.type.equals(
                        "step", true
                    )
                ) {
                    binding.imageViewType.setImageResource(R.drawable.ic_steps)
                    binding.imageViewType1.setImageResource(R.drawable.ic_steps)
                } else if (challengeModel.type.equals("distance")) {
                    binding.imageViewType.setImageResource(R.drawable.ic_distance)
                    binding.imageViewType1.setImageResource(R.drawable.ic_distance)
                } else {
                    binding.imageViewType.setImageResource(R.drawable.ic_calories)
                    binding.imageViewType1.setImageResource(R.drawable.ic_calories)
                }


                val dateStartData = challengeModel.getStartsInData()

                if (dateStartData.second.isEmpty()) {
                    if (dateStartData.first.equals("0")) {//Show Ends in instead
                        val dateEndsData = challengeModel.getEndsInData()
                        var endText = "Ends in ${dateEndsData.first} ${dateEndsData.second}"
                        if (dateEndsData.second.isEmpty()) {
                            endText = "Ends ${dateEndsData.first}"
                        }
                        binding.textViewStartInDay.text = endText
                    } else {
                        binding.textViewStartInDay.text = "Starts ${dateStartData.first}"
                    }
                } else {
                    binding.textViewStartInDay.text =
                        "Starts in ${dateStartData.first} ${dateStartData.second}"
                }


                setParticipants(binding, challengeModel)


                binding.root.setOnClickListener {
                    itemClickListener?.invoke(challengeModel.challenge_id, challengeModel)
                }


            }

            private fun setParticipants(
                binding: RowNewChallengeBinding,
                challengeModel: com.noisefit_commans.data.response.ChallengeModel
            ) {
                if (challengeModel.participants != null && (challengeModel.participants
                        ?: 0L) > 0
                ) {
                    binding.tvParticipantValue.text =
                        "${challengeModel.participants} ${if (challengeModel.participants == 1L) "Participant" else "Participants"}"
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

        }


        class ViewHolderAwaited(val binding: RowAwaitedChallengeBinding) :
            HomeRecycler(binding) {
            fun bind(challengeModel: com.noisefit_commans.data.response.ChallengeModel) {
                binding.textViewTitle.text = challengeModel.title
                Glide.with(binding.imageViewBack.context).load(challengeModel.image_url)
                    .into(binding.imageViewBack)

                binding.textViewStartInDay.text =
                    binding.textViewStartInDay.context.getString(R.string.text_chg_completed)
                binding.tvParticipantValue.text =
                    binding.tvParticipantValue.context.getString(R.string.text_result_pending)


                binding.root.setOnClickListener {
                    itemClickListener?.invoke(challengeModel.challenge_id, challengeModel)
                }


            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecycler {
        return when (viewType) {
            LAYOUT_ONE -> {
                val bindingJoin = RowJoinedChallengeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HomeRecycler.ViewHolderJoined(bindingJoin, unit)
            }
            LAYOUT_TWO -> {
                val bindingUpcoming = RowNewChallengeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HomeRecycler.ViewHolderUpcoming(bindingUpcoming)
            }
            LAYOUT_THREE -> {
                val bindingUpcoming = RowAwaitedChallengeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HomeRecycler.ViewHolderAwaited(bindingUpcoming)
            }
            else -> throw IllegalArgumentException("Invalid ViewType Provided")

        }
    }

    override fun onBindViewHolder(holder: HomeRecycler, position: Int) {
        holder.itemClickListener = itemClickListener
        when (holder) {
            is HomeRecycler.ViewHolderJoined -> holder.bind(mDataSet[position])
            is HomeRecycler.ViewHolderUpcoming -> holder.bind(mDataSet[position])
            is HomeRecycler.ViewHolderAwaited -> holder.bind(mDataSet[position])
        }
    }

    override fun getItemCount(): Int = mDataSet.size

    override fun getItemViewType(position: Int): Int {
        return if (mDataSet[position].status.equals("ongoing")) LAYOUT_ONE
        else if (mDataSet[position].status.equals("upcoming")) LAYOUT_TWO
        else LAYOUT_THREE
    }


    fun setDataSet(dataSet: List<com.noisefit_commans.data.response.ChallengeModel>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }

}