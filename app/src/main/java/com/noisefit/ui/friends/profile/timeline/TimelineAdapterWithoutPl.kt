package com.noisefit.ui.friends.profile.timeline


import android.annotation.SuppressLint
import android.net.Uri
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.noisefit.luna.R
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.timeline.ReactionData
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.luna.databinding.LayoutOriginalPostBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.profile.UserType
import com.noisefit.util.*
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadCircleEmoji
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS


class TimelineAdapterWithoutPl(val listener: OnTimelineInteractionListener) :
    RecyclerView.Adapter<TimelineAdapterWithoutPl.ViewHolder>() {


    val mDataSet = ArrayList<TimelineData>()
    var loginUserId: Int? = null
    var mLastPlayer: ExoPlayer? = null


    inner class ViewHolder(val binding: LayoutOriginalPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var player: ExoPlayer? = null
        fun bind(data: TimelineData, position: Int) {


            binding.apply {
                tvName.text = data.name

                if (data.caption.isNullOrEmpty()) {
                    tvContent.gone()
                    binding.tvReadMore.gone()
                } else {
                    tvContent.visible()
                    setContextText(tvContent, data.caption ?: "", data.taggedUser)
                    tvContent.doOnPreDraw {
                        val lines = TextMeasurementUtil.getTextLines(tvContent).size
                        if (lines > AppConstants.MIN_LINES) {
                            tvReadMore.visible()
                        } else {
                            tvReadMore.gone()
                        }
                    }

                }

                profileImv.loadImage(profileImv.context, data.imageUrl,R.drawable.ic_default_profile_image)


                if (data.mediaType == "video" && (data.mediaUrl?.size ?: 0) > 0) {
                    //video related
                    contentImv.gone()
                    lytSvPlayer.root.visible()
                    player = ExoPlayer.Builder(binding.lytSvPlayer.videoPlayer.context).build()
                        .also { exoPlayer ->
                            binding.lytSvPlayer.videoPlayer.player = exoPlayer
                            exoPlayer.playWhenReady = false
                            exoPlayer.addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(playbackState: Int) {
                                    //do some code
                                    if (playbackState == ExoPlayer.STATE_ENDED) {
                                        exoPlayer.seekTo(0)
                                        exoPlayer.pause()
                                        binding.lytSvPlayer.ivPlay.visible()
                                        binding.lytSvPlayer.ivPlay.setImageResource(R.drawable.ic_video_play)
                                    }
                                }
                            })
                        }
                    val mediaItem: MediaItem =
                        MediaItem.fromUri(
                            Uri.parse(
                                data.mediaUrl?.firstOrNull()
                            )
                        )
                    player?.setMediaItem(mediaItem)
                    player?.prepare()

                    binding.lytSvPlayer.videoPlayer.setControllerVisibilityListener { visibility ->
                        if (visibility == View.VISIBLE) {
                            LOGS.d("Visible")
                            binding.lytSvPlayer.ivPlay.visible()
                        } else {
                            LOGS.d("Hide")
                            binding.lytSvPlayer.ivPlay.gone()
                        }
                    }
                } else {
                    if (data.mediaUrl.isNullOrEmpty()) {
                        contentImv.gone()
                    } else {
                        contentImv.visible()
                        contentImv.loadImage(
                            contentImv.context,
                            data.mediaUrl?.firstOrNull(),
                            R.drawable.image_placeholder_voucher
                        )
                    }
                }
                var time =
                    data.createdAt?.let {
                        DateFormats.convertDateTimeToTimeStampUTC(
                            it,
                            DateFormats.dateTimeFormat2
                        )
                    }
                tvTime.text = time

                if (!data.location.isNullOrEmpty()) {
                    vCircle.visible()
                    tvLocation.visible()
                    tvLocation.text = data.location
                }


                when (data.userType) {
                    UserType.Influencer.type -> {
                        binding.imvVerified.visible()
                        if (loginUserId?.toLong() == data.userId) {
                            binding.btnAction.visible()
                        } else {
                            binding.btnAction.gone()
                        }
                    }

                    UserType.Admin.type -> {
                        binding.imvVerified.visible()
                        binding.btnAction.gone()
                    }

                    UserType.User.type -> {
                        binding.btnAction.visible()
                        binding.imvVerified.gone()
                    }
                }
                data.commentsCount.let {
                    tvComments.visible()
                    var commentText = "View $it comments"
                    if (it == 0) {
                        commentText = ""
                    } else if (it == 1) {
                        commentText = "View $it comment"
                    }
                    tvComments.text = commentText

                }

            }

            data.userReaction?.let { loadImage(it, binding.ivEmojiImage) }.run {
                binding.ivEmojiImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.ivEmojiImage.context,
                        R.drawable.ic_post_emoji
                    )
                )
            }

            data.reaction?.let { reactionData ->
                if (reactionData.size == 1) {
                    binding.imageViewCircle1.visible()
                    binding.imageViewCircle2.gone()
                    binding.imageViewCircle3.gone()
                    binding.imageViewCircle4.gone()
                    reactionData[0].reactionType?.let { loadImage(it, binding.imageViewCircle1) }
                }
                if (reactionData.size == 2) {
                    binding.imageViewCircle1.visible()
                    binding.imageViewCircle2.visible()
                    binding.imageViewCircle3.gone()
                    binding.imageViewCircle4.gone()
                    reactionData[0].reactionType?.let { loadImage(it, binding.imageViewCircle1) }
                    reactionData[1].reactionType?.let { loadImage(it, binding.imageViewCircle2) }
                }
                if (reactionData.size == 3) {
                    binding.imageViewCircle1.visible()
                    binding.imageViewCircle2.visible()
                    binding.imageViewCircle3.visible()
                    binding.imageViewCircle4.gone()
                    reactionData[0].reactionType?.let { loadImage(it, binding.imageViewCircle1) }
                    reactionData[1].reactionType?.let { loadImage(it, binding.imageViewCircle2) }
                    reactionData[2].reactionType?.let { loadImage(it, binding.imageViewCircle3) }
                }
                if (reactionData.size == 4) {
                    binding.imageViewCircle1.visible()
                    binding.imageViewCircle2.visible()
                    binding.imageViewCircle3.visible()
                    binding.imageViewCircle4.visible()
                    reactionData[0].reactionType?.let { loadImage(it, binding.imageViewCircle1) }
                    reactionData[1].reactionType?.let { loadImage(it, binding.imageViewCircle2) }
                    reactionData[2].reactionType?.let { loadImage(it, binding.imageViewCircle3) }
                    reactionData[3].reactionType?.let { loadImage(it, binding.imageViewCircle4) }
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
                        val time =
                            it.updatedAt?.let {
                                DateFormats.convertDateTimeToTimeStampUTC(
                                    it,
                                    DateFormats.dateTimeFormat2
                                )
                            }
                        tvName.text = it.name
                        tvTime.text = time
                        if (it.comment.isNullOrEmpty()) {
                            tvContent.gone()
                            tvReadMore.gone()
                        } else {
                            tvContent.visible()
                            tvContent.text = it.comment
                            tvContent.doOnPreDraw {
                                val lines = TextMeasurementUtil.getTextLines(tvContent).size
                                if (lines > AppConstants.MIN_LINES_COMMENT) {
                                    tvReadMore.visible()
                                } else {
                                    tvReadMore.gone()
                                }
                            }
                        }

                        userImv.loadImage(userImv.context, it.imageUrl,R.drawable.ic_default_profile_image)

                    }

                }
            } ?: run {

                binding.include18.root.gone()
                binding.lytComment.root.gone()
            }



            if (UserType.Influencer.type.lowercase() == data.userType?.lowercase()) {
                if (loginUserId?.toLong() == data.userId) {
                    binding.btnAction.visible()
                } else {
                    binding.btnAction.gone()
                }
            }


            if (data.reaction.isNullOrEmpty()) {
                binding.emojiView.gone()
                binding.tvLikeCount.gone()
            } else {
                binding.emojiView.visible()
                binding.tvLikeCount.visible()
                binding.tvLikeCount.text = data.likesCount.toString()
            }

            //comment read more
            binding.lytComment.tvReadMore.setOnClickListener { its ->
                binding.lytComment.tvContent.visible()
                binding.lytComment.tvReadMore.visible()
                if (binding.lytComment.tvReadMore.text == binding.lytComment.tvReadMore.context.getString(
                        R.string.text_read_more
                    )
                ) {
                    binding.lytComment.tvReadMore.text =
                        binding.lytComment.tvReadMore.context.getText(R.string.text_read_less)
                    binding.lytComment.tvContent.maxLines = AppConstants.MAX_LINES_COMMENT
                } else {
                    binding.lytComment.tvReadMore.text =
                        binding.lytComment.tvReadMore.context.getText(R.string.text_read_more)
                    binding.lytComment.tvContent.maxLines = AppConstants.MIN_LINES_COMMENT
                }
                binding.lytComment.tvContent.text = data.comment?.comment
            }

            binding.ivEmoji.setOnClickListener {
                listener.onEmojiClick(binding.ivEmoji, position, binding.ivEmoji.context, data)
            }


            binding.tvComments.setOnClickListener {
                listener.onCommentsClick(data, position,false)
            }

            binding.emojiView.setOnClickListener {
                listener.onReactionClick(data.postId)
            }
            binding.tvLikeCount.setOnClickListener {
                listener.onReactionClick(data.postId)
            }
            binding.ivComments.setOnClickListener {
                listener.onCommentsClick(data, position,true)
            }
            binding.btnAction.setOnClickListener {

                listener.onProfileActionClick(data, position)
            }
            binding.lytComment.btnAction.invisible()

            binding.lytComment.btnAction.setOnClickListener {
                listener.onCommentActionClick(data, position)
            }
            binding.profileImv.setOnClickListener {
                listener.onUserProfileActionClick(data)
            }
            binding.tvName.setOnClickListener {
                listener.onUserProfileActionClick(data)
            }

            binding.lytComment.userImv.setOnClickListener {
                listener.onCommentUserProfileActionClick(data)
            }
            binding.lytComment.tvName.setOnClickListener {
                listener.onCommentUserProfileActionClick(data)
            }
            binding.tvReadMore.setOnClickListener {
                binding.tvContent.visible()
                binding.tvReadMore.visible()
                if (binding.tvReadMore.text == binding.tvReadMore.context.getString(R.string.text_read_more)) {
                    binding.tvReadMore.text =
                        binding.tvReadMore.context.getText(R.string.text_read_less)
                    binding.tvContent.maxLines = AppConstants.MAX_LINES
                } else {
                    binding.tvReadMore.text =
                        binding.tvReadMore.context.getText(R.string.text_read_more)
                    binding.tvContent.maxLines = AppConstants.MIN_LINES
                }
                setContextText(
                    binding.tvContent,
                    data.caption ?: "", data.taggedUser
                )
                listener.scrollTOPosition(bindingAdapterPosition)
            }


            //video player actions
            binding.lytSvPlayer.ivPlay.setOnClickListener {
                if (player == null)
                    return@setOnClickListener

                if (player?.isPlaying == true) {
                    binding.lytSvPlayer.ivPlay.visible()
                    binding.lytSvPlayer.ivPlay.setImageResource(R.drawable.ic_video_play)
                    player?.pause()
                } else {
                    binding.lytSvPlayer.ivPlay.invisible()
                    binding.lytSvPlayer.ivPlay.setImageResource(R.drawable.ic_video_pause)
                    player?.play()

                    if (mLastPlayer != null && mLastPlayer != player) {
                        mLastPlayer?.pause()
                    }

                    if (mLastPlayer != player) {
                        mLastPlayer = player
                    }

                }
            }

        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setContextText(
            tvContent: TextView,
            caption: String,
            taggedUser: List<MentionUser>?
        ) {
            tvContent.text = caption.generatePostSpan()
            tvContent.setOnTouchListener { v, event ->


                if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener false

                if (taggedUser.isNullOrEmpty()) return@setOnTouchListener false

                val layout = (v as TextView).layout
                val x = event.x.toInt()
                val y = event.y.toInt()
                if (layout != null) {
                    val line = layout.getLineForVertical(y)
                    val offset = layout.getOffsetForHorizontal(line, x.toFloat())

                    if (offset != -1) {
                        val start: Int = caption.findWordStart(offset)
                        val user = taggedUser.findUser(start)
                        if (user != null) {
                            listener.onTaggedUserClicked(user)
                        } else {
                            try {
                                val char = caption[start - 1]
                                if (char.toString().equals("@", true)) {
                                    tvContent.context.showShortToast("User not found")
                                }
                            } catch (exp: Exception) {
                            }
                        }
                    }

                }
                true
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutOriginalPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    fun loadImage(type: String, imgView: ImageView) {
        imgView.loadCircleEmoji(imgView.context, type)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<TimelineData>, userId: Int?) {
        loginUserId = userId
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }

    fun removePost(lastClickedPost: TimelineData?) {
        if (lastClickedPost == null) return
        val postId = lastClickedPost.postId
        val index = mDataSet.indexOfFirst {
            it.postId == postId
        }
        if (index != -1) {
            mDataSet.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun addNewData(it: List<TimelineData>?) {
        if (it == null) return
        mDataSet.addAll(it)
        notifyDataSetChanged()//TODO replace with range added

    }

    fun stopPlayer() {
        mLastPlayer?.stop()
        mLastPlayer = null
    }

    fun pausePlayer() {
        mLastPlayer?.pause()
    }

    fun resumePlayer() {
        mLastPlayer?.play()
    }

    fun updateEmojiData(triple: Triple<Emoji?, Int, List<ReactionData>>) {
        tryCatch {
            val position = triple.second
            mDataSet[position].let {
                updateEmojiView(it, triple.first, position, triple.third)
            }
        }

    }


    private fun updateEmojiView(
        timeLineData: TimelineData,
        emoji: Emoji?,
        position: Int,
        reactionList: List<ReactionData>
    ) {

        if (emoji != null) {
            timeLineData.userReaction = emoji.emoji
        } else {
            timeLineData.userReaction = null

        }
        var likeCount = 0
        reactionList.forEach {
            likeCount += it.count
        }
        timeLineData.likesCount = likeCount
        timeLineData.reaction = reactionList


        notifyItemChanged(position)
    }
}



