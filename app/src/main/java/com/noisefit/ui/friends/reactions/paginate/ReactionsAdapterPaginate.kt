package com.noisefit.ui.friends.reactions.paginate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.databinding.ItemReactionsLayoutBinding
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.UserFriendData
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.loadImage
import javax.annotation.Nullable


class ReactionsAdapterPaginate(val listener: ReactionActions) :
    RecyclerView.Adapter<ReactionsAdapterPaginate.ViewHolder>() {

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
                binding.img.context, data.imageUrl, R.drawable.ic_default_profile_image
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

    fun insertData(dataSet: List<UserFriendData>) {
        val oldDataRange = mDataSet.size
        mDataSet.addAll(dataSet)
        notifyItemRangeInserted(oldDataRange, mDataSet.size)
    }

    /*fun submitList(dataSet: List<UserFriendData>) {
        val diffCallback = DiffCallback(this.mDataSet, dataSet)
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(diffCallback)
        this.mDataSet.clear()
        this.mDataSet.addAll(dataSet)
        diffResult.dispatchUpdatesTo(this)
    }*/


    internal class DiffCallback(
        oldList: List<UserFriendData>, newList: List<UserFriendData>
    ) : DiffUtil.Callback() {
        private val mOldList: List<UserFriendData>
        private val mNewList: List<UserFriendData>

        init {
            mOldList = oldList
            mNewList = newList
        }

        override fun getOldListSize(): Int {
            return mOldList.size
        }

        override fun getNewListSize(): Int {
            return mNewList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return mOldList[oldItemPosition].user_id === mNewList[newItemPosition].user_id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldData: UserFriendData = mOldList[oldItemPosition]
            val newData: UserFriendData = mNewList[newItemPosition]
            return (oldData.user_id ?: "").equals(newData.user_id)
        }

        @Nullable
        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }

}

interface ReactionActions {
    fun onUserClicked(userId: Long)
}