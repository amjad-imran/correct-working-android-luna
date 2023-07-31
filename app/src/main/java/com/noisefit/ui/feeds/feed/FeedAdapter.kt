package com.noisefit.ui.feeds.feed

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.noisefit.luna.R
import com.noisefit.data.model.FeedOverView
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.timeline.ReactionData
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.luna.databinding.ItemAdsListBinding
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


sealed class FeedClickEnum {
    object ReadMore : FeedClickEnum()
    data class Emoji(val view: View, val data: TimelineData, val position: Int) : FeedClickEnum()
    data class Comment(val data: TimelineData, val showKeyboard: Boolean) : FeedClickEnum()

    data class ReactionList(val postId: Long) : FeedClickEnum()
    data class ProfileAction(val data: TimelineData) :
        FeedClickEnum()

    data class CommentAction(val data: TimelineData) :
        FeedClickEnum()

    data class ShowProfile(val data: TimelineData) : FeedClickEnum()
    data class ShowCommentProfile(val data: TimelineData) : FeedClickEnum()
    data class TagProfile(var user: MentionUser?) : FeedClickEnum()
    data class ADS(val view: View, val data: TimelineData, val position: Int) : FeedClickEnum()
    data class ScrollToPost(val position: Int) : FeedClickEnum()
}

class FeedAdapter :
    RecyclerView.Adapter<FeedRecyclerViewHolder>() {


    var lastPosition = -1
    var refreshPosition: Int? = null

    var mDataSet = ArrayList<TimelineData>()


    var mLastPlayer: ExoPlayer? = null

    fun setDataSet(list: List<TimelineData>) {
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }

    var itemClickListener: ((type: FeedClickEnum) -> Unit)? =
        null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedRecyclerViewHolder {
        return when (viewType) {
            R.layout.item_ads_list -> FeedRecyclerViewHolder.AdsViewHolder(
                ItemAdsListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_post_list -> FeedRecyclerViewHolder.PostViewHolder(
                LayoutOriginalPostBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: FeedRecyclerViewHolder, position: Int) {
        holder.itemClickListener = itemClickListener
        when (holder) {
            is FeedRecyclerViewHolder.PostViewHolder -> holder.bind(
                FeedOverView.Post(mDataSet[position]),
            ) {
                mLastPlayer = it
            }

            is FeedRecyclerViewHolder.AdsViewHolder -> holder.bind(
                mDataSet[position],
                position,
                lastPosition
            )


        }
    }

    override fun getItemCount() = mDataSet.size

    override fun getItemViewType(position: Int): Int {
        return if (mDataSet[position].feedType.equals("ad", true)) {
            R.layout.item_ads_list
        } else {
            R.layout.item_post_list
        }
    }

    fun removeItem(postId: Long) {

        val index = mDataSet.indexOfFirst {
            it.postId == postId
        }

        tryCatch {
            mDataSet.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /*fun addData(data: List<FeedOverView>) {
        val lastDataSize = mDataSet.size
        mDataSet.addAll(data)
        notifyItemRangeInserted(lastDataSize, mDataSet.size)
    }*/

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


sealed class FeedRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((type: FeedClickEnum) -> Unit)? =
        null

    var mLastPlayer: ExoPlayer? = null

    class AdsViewHolder(private val binding: ItemAdsListBinding) :
        FeedRecyclerViewHolder(binding) {
        fun bind(
            data: TimelineData,
            position: Int,
            lastPosition: Int
        ) {
            binding.imv.loadImage(
                binding.imv.context,
                data.mediaUrl?.firstOrNull(),
                R.drawable.image_placeholder_voucher_banner
            )

            binding.root.setOnClickListener {
                itemClickListener?.invoke(FeedClickEnum.ADS(it, data, position))
            }

        }
    }


    class PostViewHolder(private val binding: LayoutOriginalPostBinding) :
        FeedRecyclerViewHolder(binding) {

        var player: ExoPlayer? = null
        fun bind(
            data: FeedOverView.Post,
            setPlayer: (player: ExoPlayer?) -> Unit
        ) {

            binding.apply {
                tvName.text = data.postData.name

                if (data.postData.caption.isNullOrEmpty()) {
                    tvContent.gone()
                    tvReadMore.gone()
                } else {
                    tvContent.visible()
                    setContextText(tvContent, data.postData.caption ?: "", data.postData.taggedUser)
                    tvContent.doOnPreDraw {
                        val lines = TextMeasurementUtil.getTextLines(tvContent).size
                        if (lines > AppConstants.MIN_LINES) {
                            tvReadMore.visible()
                        } else {
                            tvReadMore.gone()
                        }
                    }
                }

                profileImv.loadImage(
                    profileImv.context,
                    data.postData.imageUrl,
                    R.drawable.ic_default_profile_image
                )
                if (data.postData.mediaType == "video" && (data.postData.mediaUrl?.size ?: 0) > 0) {
                    //video related
                    contentImv.gone()
                    lytSvPlayer.root.visible()
                    player = ExoPlayer.Builder(binding.lytSvPlayer.videoPlayer.context)
                        .setRenderersFactory(
                            DefaultRenderersFactory(binding.lytSvPlayer.videoPlayer.context).setEnableDecoderFallback(
                                true
                            )
                        ).build()
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

                                override fun onIsPlayingChanged(isPlaying: Boolean) {
                                    if (isPlaying) {
                                        binding.lytSvPlayer.ivPlay.setImageResource(R.drawable.ic_video_pause)
                                    } else {
                                        binding.lytSvPlayer.ivPlay.setImageResource(R.drawable.ic_video_play)
                                    }
                                }
                            })
                        }
                    val mediaItem: MediaItem =
                        MediaItem.fromUri(
                            Uri.parse(
                                data.postData.mediaUrl?.firstOrNull()
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
                    lytSvPlayer.root.gone()
                    if (data.postData.mediaUrl.isNullOrEmpty()) {
                        contentImv.gone()
                    } else {
                        contentImv.visible()
                        contentImv.loadImage(
                            contentImv.context,
                            data.postData.mediaUrl?.firstOrNull(),
                            R.drawable.placeholder_banner
                        )
                    }
                }
                var time =
                    data.postData.createdAt?.let {
                        DateFormats.convertDateTimeToTimeStampUTC(
                            it,
                            DateFormats.dateTimeFormat2
                        )
                    }

                tvTime.text = time

                if (!data.postData.location.isNullOrEmpty()) {
                    vCircle.visible()
                    tvLocation.visible()
                    tvLocation.text = data.postData.location
                }

                var hasLikeData = false
                if (data.postData.like != null) {
                    hasLikeData = true
                    this.lytLike.apply {
                        val likeData = data.postData.like
                        this.profileImvLike.loadImage(
                            this.profileImvLike.context,
                            likeData?.imageUrl,
                            R.drawable.ic_default_profile_image
                        )
                        this.tvLikeContent.text = if (likeData?.count == 0) {
                            "${likeData.name} liked this"
                        } else {
                            "${likeData?.name} and ${likeData?.count} other${if (likeData?.count == 1) "" else "s"} liked this"
                        }
                    }
                    this.lytLike.root.visible()
                } else {
                    hasLikeData = false
                    this.lytLike.root.gone()
                }

                when (data.postData.userType) {
                    UserType.Influencer.type -> {
                        binding.imvVerified.visible()
                        binding.btnAction.gone()
                        binding.lytLike.btnActionLike.gone()
                    }

                    UserType.Admin.type -> {
                        binding.imvVerified.visible()
                        binding.lytLike.btnActionLike.gone()
                        binding.btnAction.gone()
                    }

                    UserType.User.type -> {
                        binding.imvVerified.gone()
                        if (hasLikeData) {
                            binding.lytLike.btnActionLike.visible()
                            binding.btnAction.gone()
                        } else {
                            binding.btnAction.visible()
                            binding.lytLike.btnActionLike.gone()

                        }
                    }
                }
                data.postData.commentsCount.let {

                    var commentText = "View $it comments"
                    if (it == 0) {
                        commentText = ""
                    } else if (it == 1) {
                        commentText = "View $it comment"
                    }
                    tvComments.text = commentText

                }

                data.postData.userReaction?.let { loadImage(it, binding.ivEmojiImage) }.run {
                    binding.ivEmojiImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.ivEmojiImage.context,
                            R.drawable.ic_post_emoji
                        )
                    )
                }

                data.postData.reaction?.let { reactionData ->
                    if (reactionData.size == 1) {
                        binding.imageViewCircle1.visible()
                        binding.imageViewCircle2.gone()
                        binding.imageViewCircle3.gone()
                        binding.imageViewCircle4.gone()
                        reactionData[0].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle1
                            )
                        }
                    }
                    if (reactionData.size == 2) {
                        binding.imageViewCircle1.visible()
                        binding.imageViewCircle2.visible()
                        binding.imageViewCircle3.gone()
                        binding.imageViewCircle4.gone()
                        reactionData[0].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle1
                            )
                        }
                        reactionData[1].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle2
                            )
                        }
                    }
                    if (reactionData.size == 3) {
                        binding.imageViewCircle1.visible()
                        binding.imageViewCircle2.visible()
                        binding.imageViewCircle3.visible()
                        binding.imageViewCircle4.gone()
                        reactionData[0].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle1
                            )
                        }
                        reactionData[1].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle2
                            )
                        }
                        reactionData[2].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle3
                            )
                        }
                    }
                    if (reactionData.size == 4) {
                        binding.imageViewCircle1.visible()
                        binding.imageViewCircle2.visible()
                        binding.imageViewCircle3.visible()
                        binding.imageViewCircle4.visible()
                        reactionData[0].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle1
                            )
                        }
                        reactionData[1].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle2
                            )
                        }
                        reactionData[2].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle3
                            )
                        }
                        reactionData[3].reactionType?.let {
                            loadImage(
                                it,
                                binding.imageViewCircle4
                            )
                        }
                    }
                }



                data.postData.comment?.let {
                    if (it.comment.isNullOrEmpty()) {
                        binding.include18.root.gone()
                        binding.lytComment.root.gone()
                    } else {
                        binding.include18.root.visible()
                        binding.lytComment.apply {
                            root.visible()
                            val commentTime =
                                it.updatedAt?.let {
                                    DateFormats.convertDateTimeToTimeStampUTC(
                                        it,
                                        DateFormats.dateTimeFormat2
                                    )
                                }
                            val name = it.name
                            tvName.text = name
                            tvTime.text = commentTime
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
                            userImv.loadImage(
                                userImv.context,
                                it.imageUrl,
                                R.drawable.ic_default_profile_image
                            )
                            //comment read more
                            tvReadMore.setOnClickListener { its ->
                                tvContent.visible()
                                tvReadMore.visible()
                                if (tvReadMore.text == tvReadMore.context.getString(R.string.text_read_more)) {
                                    tvReadMore.text =
                                        tvReadMore.context.getText(R.string.text_read_less)
                                    tvContent.maxLines = AppConstants.MAX_LINES_COMMENT
                                } else {
                                    tvReadMore.text =
                                        tvReadMore.context.getText(R.string.text_read_more)
                                    tvContent.maxLines = AppConstants.MIN_LINES_COMMENT
                                }
                                tvContent.text = it.comment
                            }


                            when (it.userType) {
                                UserType.Influencer.type, UserType.Admin.type -> {
                                    imvVerified.visible()
                                }

                                else -> {
                                    imvVerified.gone()
                                }
                            }

                        }

                    }
                } ?: run {

                    binding.include18.root.gone()
                    binding.lytComment.root.gone()
                }


                if (data.postData.reaction.isNullOrEmpty()) {
                    binding.emojiView.gone()
                    binding.tvLikeCount.gone()
                } else {
                    binding.emojiView.visible()
                    binding.tvLikeCount.visible()
                    binding.tvLikeCount.text = data.postData.likesCount.toString()
                }

                binding.emojiView.setOnClickListener {
                    itemClickListener?.invoke(FeedClickEnum.ReactionList(data.postData.postId))

                }


                //video player actions
                binding.lytSvPlayer.ivPlay.setOnClickListener {
                    if (player == null)
                        return@setOnClickListener
                    if ((data.postData.mediaUrl?.size ?: 0) == 0)
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

                        if (mLastPlayer != player || mLastPlayer == null) {
                            mLastPlayer = player
                        }
                        setPlayer(mLastPlayer)

                    }
                }




                binding.tvLikeCount.setOnClickListener {
                    itemClickListener?.invoke(FeedClickEnum.ReactionList(data.postData.postId))
                }

                binding.ivEmoji.setOnClickListener {
                    itemClickListener?.invoke(
                        FeedClickEnum.Emoji(
                            it,
                            data.postData,
                            bindingAdapterPosition
                        )
                    )
                }
                binding.tvComments.setOnClickListener {
                    itemClickListener?.invoke(FeedClickEnum.Comment(data.postData, false))

                }
                binding.ivComments.setOnClickListener {
                    itemClickListener?.invoke(FeedClickEnum.Comment(data.postData, true))
                }
                binding.btnAction.setOnClickListener {
                    itemClickListener?.invoke(FeedClickEnum.ProfileAction(data.postData))
                }
                binding.lytLike.btnActionLike.setOnClickListener {
                    itemClickListener?.invoke(FeedClickEnum.ProfileAction(data.postData))
                }

                binding.lytComment.btnAction.invisible()

                binding.lytComment.btnAction.setOnClickListener {
                    itemClickListener?.invoke(FeedClickEnum.CommentAction(data.postData))
                }
                binding.lytComment.userImv.setOnClickListener {
                    itemClickListener?.invoke(
                        FeedClickEnum.ShowCommentProfile(data.postData)
                    )
                }
                binding.lytComment.tvName.setOnClickListener {
                    itemClickListener?.invoke(
                        FeedClickEnum.ShowCommentProfile(data.postData)
                    )
                }

                binding.profileImv.setOnClickListener {
                    itemClickListener?.invoke(
                        FeedClickEnum.ShowProfile(data.postData)
                    )

                }
                binding.tvName.setOnClickListener {
                    itemClickListener?.invoke(
                        FeedClickEnum.ShowProfile(data.postData)
                    )
                }
                binding.tvReadMore.setOnClickListener {
                    tvContent.visible()
                    tvReadMore.visible()
                    if (tvReadMore.text == tvReadMore.context.getString(R.string.text_read_more)) {
                        tvReadMore.text = tvReadMore.context.getText(R.string.text_read_less)
                        tvContent.maxLines = AppConstants.MAX_LINES
                    } else {
                        tvReadMore.text = tvReadMore.context.getText(R.string.text_read_more)
                        tvContent.maxLines = AppConstants.MIN_LINES
                    }
                    setContextText(
                        tvContent,
                        data.postData.caption ?: "",
                        data.postData.taggedUser
                    )

                    itemClickListener?.invoke(
                        FeedClickEnum.ScrollToPost(bindingAdapterPosition)
                    )

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


                val layout = (v as TextView).layout
                val x = event.x.toInt()
                val y = event.y.toInt()

                if (layout != null) {
                    val line = layout.getLineForVertical(y)
                    val offset = layout.getOffsetForHorizontal(line, x.toFloat())

                    if (offset != -1) {
                        val start: Int = caption.findWordStart(offset)
                        val user = taggedUser?.findUser(start)
                        if (user != null) {
                            itemClickListener?.invoke(FeedClickEnum.TagProfile(user))
                        } else {
                            try {
                                val char = caption[start - 1]
                                if (char.toString().equals("@", true)) {
                                    tvContent.context.showShortToast("User not found")
                                }
                            } catch (exp: Exception) {
                                exp.printStackTrace()
                            }
                        }
                    }
                }
                true
            }
        }


        private fun findUser(taggedUser: List<MentionUser>, start: Int): MentionUser? {
            return taggedUser.firstOrNull {
                ((it.start_pos ?: -1) + 1) == start
            }
        }


    }

    fun loadImage(type: String, imgView: ImageView) {
        imgView.loadCircleEmoji(imgView.context, type)
    }


}