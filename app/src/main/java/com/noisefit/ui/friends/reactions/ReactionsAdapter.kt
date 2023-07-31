package com.noisefit.ui.friends.reactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.UserFriendData
import com.noisefit.luna.databinding.ItemReactionsLayoutBinding
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.loadImage


class ReactionsAdapter(val listener: ReactionActions) :
    RecyclerView.Adapter<ReactionsAdapter.ViewHolder>() {

    val mDataSet = ArrayList<UserFriendData>()

    inner class ViewHolder(val binding: ItemReactionsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: UserFriendData, position: Int) {
            if (!data.firstName.isNullOrEmpty()) {
                binding.tvName.text = data.firstName
            } else {
                binding.tvName.text = data.name
            }

            binding.img.loadCircleImage(
                binding.img.context,
                data.imageUrl,
                R.drawable.ic_default_profile_image
            )

            when (data.emojiType) {
                Emoji.EmojiHand.emoji -> {
                    binding.ImvEmoji.loadImage(binding.ImvEmoji.context, com.noisefit_commans.R.drawable.ic_strong_emoji)
                }
                Emoji.EmojiFire.emoji -> {
                    binding.ImvEmoji.loadImage(binding.ImvEmoji.context, com.noisefit_commans.R.drawable.ic_fire_emoji)
                }
                Emoji.Emoji100.emoji -> {
                    binding.ImvEmoji.loadImage(binding.ImvEmoji.context, com.noisefit_commans.R.drawable.ic_100_emoji)
                }
                Emoji.EmojiHeart.emoji -> {
                    binding.ImvEmoji.loadImage(binding.ImvEmoji.context, com.noisefit_commans.R.drawable.ic_heart_emoji)
                }
            }

            binding.container.setOnClickListener {
                data.user_id?.let { userId ->
                    listener.onUserClicked(userId)
                }
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemReactionsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<UserFriendData>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }


}

interface ReactionActions {
    fun onUserClicked(userId: Long)
}