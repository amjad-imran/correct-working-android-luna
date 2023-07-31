package com.noisefit.ui.friends.compete

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieDrawable
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.noisefit.R
import com.noisefit_commans.data.response.Competitions
import com.noisefit.databinding.ItemCompeteListBinding
import com.noisefit.databinding.LayoutAddCompetitorBinding
import com.noisefit.databinding.LayoutWinnerBinding
import com.noisefit.ui.common.calculatePercentage
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.numberFormatter
import com.noisefit_commans.ui.playAnimation
import com.noisefit.ui.dashboard.summary.RING_ANIMATION
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.DateFormats
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class CompeteListAdapter :
    RecyclerView.Adapter<CompeteListAdapter.HomeRecycler>() {
    companion object {
        private const val LAYOUT_COMPLETED = 1
        private const val LAYOUT_OTHER = 2
        private const val LAYOUT_ME = 3
        private const val LAYOUT_DISQUALIFIED = 4

    }

    private var currentTime: String? = null
    val mDataSet = ArrayList<Competitions>()

    //change string with respective data model
    var itemClickListener: ((position: Int, string: Competitions) -> Unit)? = null

    sealed class HomeRecycler(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        var itemClickListener: ((position: Int, string: Competitions) -> Unit)? = null


        class ViewHolderWinner(val bindingWinner: LayoutWinnerBinding) :
            HomeRecycler(bindingWinner) {

            private fun setWinnerData(
                bindingWinner: LayoutWinnerBinding,
                name: String,
                progress: String,
                image: String,
                percentage: Float,
                goal: Long?,
                friendPerc: String,
                oppName: String,
                type: String
            ) {
                bindingWinner.apply {
                    dynamicArcView.deleteAll()
                    tvFriendName.text = name
                    tvFriendStepsValue.text = progress.toLong().numberFormatter()
                    ivFriendProfile.apply {
                        loadCircleImage(
                            this.context,
                            image,
                            R.drawable.ic_default_profile_image
                        )
                    }
                    tvVs.text = oppName



                    dynamicArcView.addSeries(
                        ApplicationUtils.seriesItemWithoutInset(
                            binding.root.context, 100f, 100f, R.color.steps_arc_bg, 16f
                        )
                    )
                    val stepIndex: Int = dynamicArcView.addSeries(
                        ApplicationUtils.seriesItemWithoutInset(
                            dynamicArcView.context, 0f, 100f, R.color.steps_arc, 16f
                        )
                    )
                    dynamicArcView.addEvent(
                        DecoEvent.Builder(percentage).setDuration(RING_ANIMATION)
                            .setIndex(stepIndex).build()
                    )

                    tvActivityValue.text = (goal?:0L).numberFormatter()
                    tvEndActivity.text = friendPerc.toLong().numberFormatter()
                    tvActivityTitle.text = type

                    animationView.playAnimation(
                        LottieDrawable.INFINITE,
                        R.raw.anim_friends_winner
                    )
                }


            }

            fun bind(data: Competitions, currentTime: String?) {

                if (data.isWinner) {
                    val opName = "${data.friendName?.split(" ")?.get(0)}'s Progress"
                    val userPercentage = data.userProgress?.toFloat()
                        ?.calculatePercentage(data.goal?.toFloat()) ?: 0f
                    setWinnerData(
                        bindingWinner,
                        "Me",
                        data.userProgress.toString(),
                        data.userImage ?: "",
                        userPercentage,
                        data.goal,
                        data.friendProgress.toString(),
                        opName,
                        data.type ?: ""
                    )
                } else {
                    val opName = "${data.userName?.split(" ")?.get(0)}'s Progress"
                    val userPercentage = data.friendProgress?.toFloat()
                        ?.calculatePercentage(data.goal?.toFloat()) ?: 0f
                    setWinnerData(
                        bindingWinner,
                        data.friendName ?: "",
                        data.friendProgress.toString(),
                        data.friendImage ?: "",
                        userPercentage,
                        data.goal,
                        data.userProgress.toString(),
                        opName,
                        data.type ?: ""
                    )
                }

            }
        }

        class ViewHolderMainContent(val bindingMainContent: ItemCompeteListBinding) :
            HomeRecycler(bindingMainContent) {
            fun bind(data: Competitions, currentTime: String?) {
                bindingMainContent.userCircleProgress.deleteAll()
                bindingMainContent.friendCircleProgress.deleteAll()



                bindingMainContent.tvActivityTitle.text = data.type
                bindingMainContent.tvFriendName.text = data.friendName
                bindingMainContent.tvActivityValue.text = (data.goal?:0L).numberFormatter()
                bindingMainContent.tvFriendStepsValue.text = (data.friendProgress?:0L).numberFormatter()
                bindingMainContent.tvUserStepsValue.text = (data.userProgress?:0L).numberFormatter()
                bindingMainContent.ivFriendProfile.apply {
                    loadCircleImage(
                        this.context,
                        data.friendImage,
                        R.drawable.ic_default_profile_image
                    )
                }

                bindingMainContent.ivUserProfile.apply {
                    loadCircleImage(
                        this.context,
                        data.userImage,
                        R.drawable.ic_default_profile_image
                    )
                }

                val userPercentage = data.userProgress?.toFloat()
                    ?.calculatePercentage(data.goal?.toFloat()) ?: 0f


                bindingMainContent.userCircleProgress.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        binding.root.context, 100f, 100f, R.color.steps_arc_bg, 16f
                    )
                )
                val stepIndex: Int = bindingMainContent.userCircleProgress.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        bindingMainContent.userCircleProgress.context,
                        0f,
                        100f,
                        R.color.steps_arc,
                        16f
                    )
                )
                bindingMainContent.userCircleProgress.addEvent(
                    DecoEvent.Builder(
                        if (userPercentage >= 100) {
                            100f
                        } else {
                            userPercentage
                        }
                    ).setIndex(stepIndex).setDuration(RING_ANIMATION).build()
                )


                val friendPercentage = data.friendProgress?.toFloat()
                    ?.calculatePercentage(data.goal?.toFloat()) ?: 0f



                bindingMainContent.friendCircleProgress.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        binding.root.context, 100f, 100f, R.color.steps_arc_bg, 16f
                    )
                )
                val friendIndex: Int = bindingMainContent.friendCircleProgress.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        bindingMainContent.friendCircleProgress.context,
                        0f,
                        100f,
                        R.color.steps_arc,
                        16f
                    )
                )
                bindingMainContent.friendCircleProgress.addEvent(
                    DecoEvent.Builder(
                        if (friendPercentage >= 100) {
                            100f
                        } else {
                            friendPercentage
                        }
                    ).setIndex(friendIndex).setDuration(RING_ANIMATION).build()
                )

                when (data.status) {
                    "upcoming" -> {
                        bindingMainContent.tvEndActivity.text =
                            bindingMainContent.tvEndActivity.resources.getString(R.string.text_starts_tomorrow)
                    }
                    "ongoing" -> {
                        if (currentTime != null && data.endAt != null) {
                            val endInDays =
                                DateFormats.getEndsInData1(currentTime, data.endAt!!)

                            val endIn = if (endInDays.second.isEmpty()) {
                                "Ends ${endInDays.first}"
                            } else {
                                "Ends in ${endInDays.first} ${endInDays.second}"
                            }
                            bindingMainContent.tvEndActivity.text = endIn
                        }

                    }
                }
            }
        }

        class ViewHolderDisqualifiedContent(val bindingMainContent: ItemCompeteListBinding) :
            HomeRecycler(bindingMainContent) {
            fun bind(data: Competitions, currentTime: String?) {
                bindingMainContent.userCircleProgress.deleteAll()
                bindingMainContent.friendCircleProgress.deleteAll()

                bindingMainContent.tvFriendName.alpha = 0.3f
                bindingMainContent.tvFriendStepsValue.alpha = 0.3f
                bindingMainContent.tvUserStepsValue.alpha = 0.3f
                bindingMainContent.ivFriendProfile.alpha = 0.3f
                bindingMainContent.ivUserProfile.alpha = 0.3f
                bindingMainContent.userCircleProgress.alpha = 0.3f
                bindingMainContent.friendCircleProgress.alpha = 0.3f
                bindingMainContent.tvUserName.alpha = 0.3f

                bindingMainContent.tvActivityTitle.text = data.type
                bindingMainContent.tvFriendName.text = data.friendName
                bindingMainContent.tvActivityValue.text = (data.goal?:0L).numberFormatter()
                bindingMainContent.tvFriendStepsValue.text = (data.friendProgress?:0L).numberFormatter()
                bindingMainContent.tvUserStepsValue.text = (data.userProgress?:0L).numberFormatter()
                bindingMainContent.ivFriendProfile.apply {
                    loadCircleImage(
                        this.context,
                        data.friendImage,
                        R.drawable.ic_default_profile_image
                    )
                }

                bindingMainContent.ivUserProfile.apply {
                    loadCircleImage(
                        this.context,
                        data.userImage,
                        R.drawable.ic_default_profile_image
                    )
                }

                val userPercentage = data.userProgress?.toFloat()
                    ?.calculatePercentage(data.goal?.toFloat()) ?: 0f


                bindingMainContent.userCircleProgress.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        binding.root.context, 100f, 100f, R.color.steps_arc_bg, 16f
                    )
                )
                val stepIndex: Int = bindingMainContent.userCircleProgress.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        bindingMainContent.userCircleProgress.context,
                        0f,
                        100f,
                        R.color.steps_arc,
                        16f
                    )
                )
                bindingMainContent.userCircleProgress.addEvent(
                    DecoEvent.Builder(
                        if (userPercentage >= 100) {
                            100f
                        } else {
                            userPercentage
                        }
                    ).setIndex(stepIndex).setDuration(RING_ANIMATION).build()
                )


                val friendPercentage = data.friendProgress?.toFloat()
                    ?.calculatePercentage(data.goal?.toFloat()) ?: 0f


                bindingMainContent.friendCircleProgress.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        binding.root.context, 100f, 100f, R.color.steps_arc_bg, 16f
                    )
                )
                val friendIndex: Int = bindingMainContent.friendCircleProgress.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        bindingMainContent.friendCircleProgress.context,
                        0f,
                        100f,
                        R.color.steps_arc,
                        16f
                    )
                )
                bindingMainContent.friendCircleProgress.addEvent(
                    DecoEvent.Builder(
                        if (friendPercentage >= 100) {
                            100f
                        } else {
                            friendPercentage
                        }
                    ).setIndex(friendIndex).setDuration(RING_ANIMATION).build()
                )


                if (currentTime != null && data.endAt != null) {
                    val endInDays =
                        DateFormats.getEndsInData1(currentTime, data.endAt!!)

                        val endIn = when (val endedMod = abs(endInDays.first.toIntOrNull() ?: 0)) {
                            0 -> "Ended today\nNo result"
                            1 -> "Ended yesterday\nNo result"
                            else -> "Ended $endedMod days ago\nNo result"
                        }
                        bindingMainContent.tvEndActivity.text = endIn
                }
            }
        }

        class ViewHolderAddFriend(val bindingAddCompetitor: LayoutAddCompetitorBinding) :
            HomeRecycler(bindingAddCompetitor) {
            fun bind(data: Competitions) {
                bindingAddCompetitor.circleProgress.deleteAll()
                bindingAddCompetitor.circleProgressFriend.deleteAll()

                bindingAddCompetitor.circleProgress.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        binding.root.context, 100f, 100f, R.color.steps_arc_bg, 16f
                    )
                )

                bindingAddCompetitor.circleProgressFriend.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        binding.root.context, 100f, 100f, R.color.steps_arc_bg, 16f
                    )
                )

                bindingAddCompetitor.ivUserProfile.apply {
                    loadCircleImage(
                        this.context,
                        data.userImage,
                        R.drawable.ic_default_profile_image
                    )
                }




                binding.root.setOnClickListener {
                    itemClickListener?.invoke(0, data)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecycler {
        return when (viewType) {
            LAYOUT_COMPLETED -> {
                val bindingWinner = LayoutWinnerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HomeRecycler.ViewHolderWinner(bindingWinner)
            }
            LAYOUT_OTHER -> {
                val bindingMainContent = ItemCompeteListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HomeRecycler.ViewHolderMainContent(bindingMainContent)
            }
            LAYOUT_DISQUALIFIED -> {
                val bindingMainContent = ItemCompeteListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HomeRecycler.ViewHolderDisqualifiedContent(bindingMainContent)
            }
            LAYOUT_ME -> {
                val bindingAddFriend = LayoutAddCompetitorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HomeRecycler.ViewHolderAddFriend(bindingAddFriend)
            }
            else -> throw IllegalArgumentException("Invalid ViewType Provided")

        }
    }

    override fun onBindViewHolder(holder: HomeRecycler, position: Int) {
        holder.itemClickListener = itemClickListener
        when (holder) {
            is HomeRecycler.ViewHolderWinner -> holder.bind(mDataSet[position], currentTime)
            is HomeRecycler.ViewHolderMainContent -> holder.bind(mDataSet[position], currentTime)
            is HomeRecycler.ViewHolderDisqualifiedContent -> holder.bind(
                mDataSet[position],
                currentTime
            )
            is HomeRecycler.ViewHolderAddFriend -> holder.bind(mDataSet[position])
        }
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataSet[position].status.equals("completed"))
            LAYOUT_COMPLETED
        else if (mDataSet[position].status.equals("me"))
            LAYOUT_ME
        else if (mDataSet[position].status.equals("disqualified"))
            LAYOUT_DISQUALIFIED
        else
            LAYOUT_OTHER
    }

    fun setDataSet(dataSet: List<Competitions>, currentTime: String?) {
        mDataSet.clear()
        this.currentTime = currentTime
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }

}