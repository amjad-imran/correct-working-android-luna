package com.noisefit.ui.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.noisefit.R
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.FriendProgress
import com.noisefit.databinding.ItemFriendListBinding
import com.noisefit.ui.common.calculatePercentage
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.summary.RING_ANIMATION
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.upTo2Decimal
import com.noisefit_commans.utils.prettyCount


class FriendListAdapter(val listener: OnFriendsItemClickListener) :
    RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {


    val mDataSet = ArrayList<FriendProgress>()

    inner class ViewHolder(val binding: ItemFriendListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friendProgress: FriendProgress) {

            binding.container.dynamicArcView.deleteAll()

            val isMe = friendProgress.firstName.equals("Me", true)
            binding.container.textViewTitle.text = friendProgress.firstName
            Glide.with(binding.container.imageViewProfile)
                .load(friendProgress.imageUrl)
                .placeholder(R.drawable.ic_default_profile_image)
                .into(binding.container.imageViewProfile)

            binding.container.tvCaloriesValue.text = friendProgress.calories.prettyCount()
            binding.container.tvStepsValue.text = friendProgress.steps.prettyCount()


            binding.container.tvDistanceValue.text =
                friendProgress.distance.upTo2Decimal().toString()


            var caloriesProgress = friendProgress.calories.toFloat()
            val caloriesGoal = friendProgress.caloriesGoal.toFloat()
            caloriesProgress = (if (caloriesProgress > caloriesGoal) 100f else
                caloriesProgress.calculatePercentage(
                    caloriesGoal
                ))

            binding.container.dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    binding.container.root.context, 100f, 100f, R.color.calories_arc_bg, 40f, 16f
                )
            )
            val caloriesIndex: Int = binding.container.dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    binding.container.root.context, 0f, 100f, R.color.calories_arc, 40f, 16f
                )
            )


            binding.container.dynamicArcView.addEvent(
                DecoEvent.Builder(caloriesProgress).setDuration(RING_ANIMATION)
                    .setIndex(caloriesIndex).build()
            )
//            binding.container.caloriesRing.animateProgress(caloriesProgress)

            var stepsProgress = friendProgress.steps.toFloat()
            val stepsGoal = friendProgress.stepGoal.toFloat()
            stepsProgress = (if (stepsProgress > stepsGoal) 100f else
                stepsProgress.calculatePercentage(
                    stepsGoal
                ))



            binding.container.dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithoutInset(
                    binding.container.root.context, 100f, 100f, R.color.steps_arc_bg, 16f
                )
            )
            val stepIndex: Int = binding.container.dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithoutInset(
                    binding.container.root.context,
                    0f,
                    100f,
                    R.color.steps_arc,
                    16f
                )
            )
            binding.container.dynamicArcView.addEvent(
                DecoEvent.Builder(stepsProgress).setIndex(stepIndex).setDuration(RING_ANIMATION)
                    .build()
            )
//            binding.container.stepRing.animateProgress(stepsProgress)

            var distanceProgress = friendProgress.distance.toFloat()
            val distanceGoal = friendProgress.distanceGoal.toFloat()
            distanceProgress = (if (distanceProgress > distanceGoal) 100f else
                distanceProgress.calculatePercentage(
                    distanceGoal
                ))
//            binding.container.distanceRing.animateProgress(distanceProgress)


            binding.container.dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    binding.container.root.context,
                    100f,
                    100f,
                    R.color.distance_arc_bg,
                    20f,
                    16f
                )
            )
            val distanceIndex: Int = binding.container.dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    binding.container.root.context,
                    0f,
                    100f,
                    R.color.distance_arc,
                    20f,
                    16f
                )
            )

            binding.container.dynamicArcView.addEvent(
                DecoEvent.Builder(distanceProgress)
                    .setDuration(RING_ANIMATION)
                    .setIndex(distanceIndex).build()
            )

            binding.emojiContainer.setOnClickListener {
                listener.onItemEmojiClick()
            }
            if (isMe) {
                handleEmojiView(friendProgress, binding)

            } else {
                binding.emojiContainer.gone()
            }
            binding.root.setOnClickListener {
                listener.onItemClick(friendProgress)
            }
            binding.root.setOnLongClickListener {
                if (isMe) {
                    return@setOnLongClickListener false
                }
                listener.onItemLongClick(
                    binding.container.container,
                    bindingAdapterPosition,
                    binding.container.container.context,
                    friendProgress
                )
                true
            }


        }
    }

    private fun handleEmojiView(friendProgress: FriendProgress, binding: ItemFriendListBinding) {
        var count = 0
        friendProgress.reactions?.forEach {
            count += it.count ?: 0
            when (it.emoji) {
                Emoji.EmojiHeart.emoji -> {
                    binding.ivGiveEmojiHeart.visible()
                }
                Emoji.EmojiHand.emoji -> {
                    binding.ivGiveEmojiStrong.visible()
                }
                Emoji.Emoji100.emoji -> {
                    binding.ivGiveEmoji100.visible()
                }
                Emoji.EmojiFire.emoji -> {
                    binding.ivGiveEmojiFire.visible()
                }
            }
        }

        if (count == 0) {
            binding.emojiContainer.gone()
        } else {
            binding.tvCount.text = count.toString()
            binding.emojiContainer.visible()
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemFriendListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<FriendProgress>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }
}

interface OnFriendsItemClickListener {
    fun onItemClick(friend: FriendProgress)
    fun onItemEmojiClick()
    fun onItemLongClick(view: View, pos: Int, context: Context, friend: FriendProgress)
}