package com.noisefit.ui.friends.profile.timeline

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.databinding.LayoutOriginalPostBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.profile.UserType
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.loadImage


class TimelineAdapter(val listener: OnTimelineInteractionListener) :
    PagingDataAdapter<TimelineData, TimelineAdapter.TimelineViewHolder>(TimelineDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        return TimelineViewHolder(
            listener, LayoutOriginalPostBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class TimelineViewHolder(
        val listener: OnTimelineInteractionListener, val binding: LayoutOriginalPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: TimelineData) {
            binding.apply {
                tvName.text = data.name

                if (data.caption.isNullOrEmpty()) {
                    tvContent.gone()
                } else {
                    tvContent.visible()
                    tvContent.text = data.caption
                }

                profileImv.loadImage(
                    profileImv.context,
                    data.imageUrl,
                    R.drawable.ic_default_profile_image
                )
                if (data.mediaUrl.isNullOrEmpty()) {
                    contentImv.gone()
                } else {
                    contentImv.visible()
                    contentImv.loadImage(contentImv.context, data.mediaUrl!![0])
                }
                var time = ""

                if (!data.location.isNullOrEmpty()) {
                    time += " . ${data.location}"
                }
                tvTime.text = time

                when (data.userType) {
                    UserType.Influencer.type -> {
                        binding.imvVerified.visible()
                    }
                    UserType.Admin.type -> {
                        binding.imvVerified.visible()
                    }
                    UserType.User.type -> {
                        binding.imvVerified.gone()
                    }
                }
                data.commentsCount.let {
                    var commentText = "View $it comments"
                    if (it == 0) {
                        commentText = ""
                    } else if (it == 1) {
                        commentText = "View $it comment"
                    }
                    tvComments.text = commentText

                }

            }

            data.comment?.let {
                if (it.comment.isNullOrEmpty()) {
                    binding.include18.root.gone()
                    binding.lytComment.root.gone()
                } else {
                    binding.include18.root.visible()
                    binding.lytComment.apply {
                        root.visible()
                        val time = ""
                        val name = "${it.name} . $time"
                        tvName.text = name
                        tvContent.text = it.comment
                        userImv.loadCircleImage(userImv.context, it.imageUrl)

                    }

                }
            } ?: run {

                binding.include18.root.gone()
                binding.lytComment.root.gone()
            }


            if (data.likesCount == 0) {
                binding.emojiView.gone()
                binding.tvLikeCount.gone()
            } else {
                binding.emojiView.visible()
                binding.tvLikeCount.visible()
                binding.tvLikeCount.text = data.likesCount.toString()
            }


            binding.ivEmoji.setOnClickListener {
                listener.onEmojiClick(
                    binding.ivEmoji, bindingAdapterPosition, binding.ivEmoji.context, data
                )
            }
            binding.tvComments.setOnClickListener {
                listener.onCommentsClick(data, bindingAdapterPosition, false)
            }
            binding.ivComments.setOnClickListener {
                listener.onCommentsClick(data, bindingAdapterPosition, true)
            }
            binding.btnAction.setOnClickListener {
                listener.onProfileActionClick(data, bindingAdapterPosition)
            }
            binding.lytComment.btnAction.setOnClickListener {
                listener.onCommentActionClick(data, bindingAdapterPosition)
            }
        }
    }

}


class TimelineDiffCallBack : DiffUtil.ItemCallback<TimelineData>() {
    override fun areItemsTheSame(oldItem: TimelineData, newItem: TimelineData): Boolean {
        return oldItem.postId == newItem.postId
    }

    override fun areContentsTheSame(oldItem: TimelineData, newItem: TimelineData): Boolean {
        return oldItem == newItem
    }
}

interface OnTimelineInteractionListener {
    fun onCommentsClick(timeLineData: TimelineData, position: Int, showKeyboard: Boolean)
    fun onReactionClick(postId: Long)
    fun onTaggedUserClicked(user: MentionUser)
    fun onProfileActionClick(timeLineData: TimelineData, position: Int)
    fun onCommentActionClick(timeLineData: TimelineData, position: Int)
    fun onEmojiClick(view: View, pos: Int, context: Context, timeLineData: TimelineData)
    fun onUserProfileActionClick(timeLineData: TimelineData)
    fun onCommentUserProfileActionClick(timeLineData: TimelineData)
    fun scrollTOPosition(position: Int)
}