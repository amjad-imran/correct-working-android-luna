package com.noisefit.ui.feeds.postdetails

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.noisefit.R
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.timeline.CommentData
import com.noisefit.data.model.timeline.ReactionData
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.databinding.FragmentPostDetailsBinding
import com.noisefit.databinding.LayoutPostPopUpEmojiBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.feeds.bottomSheet.DELETE_PC_REQUEST_KEY
import com.noisefit.ui.feeds.bottomSheet.EDIT_PC_REQUEST_KEY
import com.noisefit.ui.feeds.bottomSheet.REPORT_PC_REQUEST_KEY
import com.noisefit.ui.feeds.comment.OnPostCommentItemClickListener
import com.noisefit.ui.feeds.comment.PostCommentAdapter
import com.noisefit.ui.friends.FriendsFragmentDirections
import com.noisefit.ui.profile.UserType
import com.noisefit.util.*
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadCircleEmoji
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailsFragment :
    BaseFragment<FragmentPostDetailsBinding>(FragmentPostDetailsBinding::inflate) {
    val mViewModel: PostDetailsViewModel by viewModels()
    var player: ExoPlayer? = null

    private var popupView: View? = null
    private var popupWindow: PopupWindow? = null
    val args: PostDetailsFragmentArgs by navArgs()
    private val mPostCommentAdapter by lazy {
        PostCommentAdapter(object : OnPostCommentItemClickListener {
            override fun onReportAbuse(position: Int, commentData: CommentData) {
                mViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEEDS_REPORTCOMMENT_CLICK)
                mViewModel.commentId = commentData.commentId
                navigate(
                    PostDetailsFragmentDirections.postDetailsFragToReportPCBottomSheet(
                        commentData.postId,
                        commentData.commentId,
                        "comment"
                    )
                )

            }

            override fun onDeleteComment(position: Int, commentData: CommentData) {
                mViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEEDS_REPORTCOMMENT_CLICK)
                mViewModel.commentId = commentData.commentId

                navigate(
                    PostDetailsFragmentDirections.postDetailsFragToDeletePCBottomSheet(
                        commentData.postId,
                        commentData.commentId
                    )
                )

            }

            override fun onShowProfile(commentData: CommentData) {
                navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", commentData.userId ?: -1)
                })
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.postId = args.id
        setRecycler()
        mViewModel.getPostDetailsData()


        if (args.showKeyboard) {
            binding.lytPostCommentBottom.etCommentHere.requestFocus()
        }
    }

    private fun setRecycler() {
        with(binding.rvComments) {
            adapter = mPostCommentAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        if (player != null) {
            player?.play()
        }
    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            player?.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player?.release()
        }
    }

    fun showProfile(){
        val post = mViewModel.postDetailsData.value
        if(post==null) return
        when (post.userType) {
            UserType.Admin.type -> {
                navigate(R.id.noiseProfile)
            }
            else -> {
                navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", post?.userId?.toInt()?:-1)
                })
            }
        }


    }


    override fun initListener() {

        binding.lytPost.profileImv.setOnClickListener {
            showProfile()
        }
        binding.lytPost.tableName.setOnClickListener {
            showProfile()
        }


        binding.rvComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mViewModel.isCommentsApiLoading) {
                        return
                    }

                    val visibleItemCount = binding.rvComments.layoutManager?.childCount ?: 0
                    val totalItemCount = binding.rvComments.layoutManager?.itemCount ?: 0
                    val firstVisibleItemPosition =
                        (binding.rvComments.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        mViewModel.getComments()
                    }
                }
            }

        })


        binding.lytPostCommentBottom.etCommentHere.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    val imm: InputMethodManager? =
                        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(
                        binding.lytPostCommentBottom.etCommentHere,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }
            }
        binding.lytPost.lytSvPlayer.ivPlay.setOnClickListener {
            if (player == null)
                return@setOnClickListener
            if ((mViewModel.postDetailsData.value?.mediaUrl?.size ?: 0) == 0)
                return@setOnClickListener

            if (player?.isPlaying == true) {
                binding.lytPost.lytSvPlayer.ivPlay.visible()
                binding.lytPost.lytSvPlayer.ivPlay.setImageResource(R.drawable.ic_video_play)
                player?.pause()

            } else {
                binding.lytPost.lytSvPlayer.ivPlay.invisible()
                binding.lytPost.lytSvPlayer.ivPlay.setImageResource(R.drawable.ic_video_pause)
                player?.play()
            }
        }


        binding.lytPostCommentBottom.imageview.loadImage(
            requireActivity(),
            mViewModel.user?.imageUrl,
            R.drawable.ic_default_profile_image
        )
        binding.lytPost.ivComments.gone()
        binding.lytPost.ivCommentsImage.gone()
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytPost.ivEmoji.setOnClickListener {
            showPopup(binding.lytPost.ivEmoji)
        }

        binding.lytPost.tvLikeCount.setOnClickListener {
            navigate(R.id.bottomSheetReactionsPaginate, Bundle().apply {
                this.putLong("postId", mViewModel.postId)
            })
            //mViewModel.getPostReactions()
        }
        binding.lytPost.emojiView.setOnClickListener {
            navigate(R.id.bottomSheetReactionsPaginate, Bundle().apply {
                this.putLong("postId", mViewModel.postId)
            })
            //mViewModel.getPostReactions()
        }
        binding.lytPost.btnAction.setOnClickListener {
            if (mViewModel.userId == mViewModel.postDetailsData.value?.userId) {
                mViewModel.clickedPost = mViewModel.postDetailsData.value
                requireActivity().supportFragmentManager.setFragmentResultListener(
                    EDIT_PC_REQUEST_KEY,
                    this
                ) { _, bundle ->
                    val isDelete = bundle.getBoolean("delete")
                    if (isDelete) {
                        mViewModel.deletePost.postValue(Event(mViewModel.postId))
                    }

                    val isEdit = bundle.getBoolean("edit")
                    if (isEdit) {
                        mViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.EDITPOST_CLICK)
                        mViewModel.clickedPost?.let {
                            mViewModel.editPost.postValue(Event(it))
                        }
                    }

                }
                navigate(
                    PostDetailsFragmentDirections.postDetailsFragToEditPCBottomSheet(
                        mViewModel.postId, -1
                    )
                )

            } else {
                setFragmentResultListener(REPORT_PC_REQUEST_KEY) { _, bundle ->
                    val isReport = bundle.getBoolean("report")
                    if (isReport) {
                        /*  if (viewModel.commentId != -1) {
                              postCommentAdapter.removeItem(viewModel.commentId)
                              viewModel.commentId = -1

                          }*/

                    }

                }
                navigate(
                    PostDetailsFragmentDirections.postDetailsFragToReportPCBottomSheet(
                        mViewModel.postId, -1, "post"
                    )
                )
            }
        }

        binding.lytPost.tvReadMore.setOnClickListener {
            if (binding.lytPost.tvReadMore.text == binding.lytPost.tvReadMore.context.getString(R.string.text_read_more)) {
                binding.lytPost.tvReadMore.text =
                    binding.lytPost.tvReadMore.context.getText(R.string.text_read_less)
                binding.lytPost.tvContent.maxLines = AppConstants.MAX_LINES
            } else {
                binding.lytPost.tvReadMore.text =
                    binding.lytPost.tvReadMore.context.getText(R.string.text_read_more)
                binding.lytPost.tvContent.maxLines = AppConstants.MIN_LINES
            }
            setContextText(
                binding.lytPost.tvContent,
                mViewModel.postDetailsData.value?.caption ?: "",
                mViewModel.postDetailsData.value?.taggedUser
            )

        }

        binding.lytPostCommentBottom.etCommentHere.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                postComment()
                true
            } else false
        }

        binding.lytPostCommentBottom.btnPostComment.setOnClickListener {
            postComment()
        }




        setFragmentResultListener(DELETE_PC_REQUEST_KEY) { _, bundle ->
            val isDelete = bundle.getBoolean("delete")
            val commentCount = bundle.getInt("commentCount", 0)
            if (isDelete) {
                if (mViewModel.commentId != -1L) {
                    mViewModel.removeComment(mViewModel.commentId, commentCount)
                    mViewModel.commentId = -1

                }

            }

        }
        //call for post delete
        requireActivity().supportFragmentManager.setFragmentResultListener(
            DELETE_PC_REQUEST_KEY,
            this
        ) { _, bundle ->
            val isDelete = bundle.getBoolean("delete")
            if (isDelete) {
                mViewModel.navigateUp.postValue(Event(true))
            }

        }

        mViewModel.navigateUp.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }

        setFragmentResultListener(REPORT_PC_REQUEST_KEY) { _, bundle ->
            val isReport = bundle.getBoolean("report")
            LOGS.d("REPORT_INSIDE $isReport ${mViewModel.commentId}")
            if (isReport) {
                /*if (mViewModel.commentId != -1L) {
                    mViewModel.removeComment(mViewModel.commentId, commentCount)
                    mViewModel.commentId = -1

                }*/

            }

        }

    }

    private fun openDeleteBottomSheet(postId: Long, commentId: Long) {

        navigate(
            PostDetailsFragmentDirections.postDetailsFragToDeletePCBottomSheet(
                postId, commentId
            )
        )
    }

    override fun subscribeObservers() {

        mViewModel.getLoadMoreLoading().observe(this) {
            if (it) {
                binding.progressBarLoadMore.visible()
            } else {
                binding.progressBarLoadMore.gone()
            }
        }

        mViewModel.deletePost.observe(this) {
            it.getContent()?.let { postId ->
                openDeleteBottomSheet(postId, -1)
            }

        }
        mViewModel.editPost.observe(this) {
            it.getContent()?.let { post ->
                navigate(
                    PostDetailsFragmentDirections.postDetailsFragToUpdatePostFragment(
                        post
                    )
                )
            }
        }
        mViewModel.commentPosted.observe(this) {
            it.getContent()?.let { posted ->
                if (posted) {
                    clearCommentEt()
                }
            }
        }
        mViewModel.userCommentList.observe(this) {
            it?.let {
                binding.lytPostCommentBottom.root.visible()
                mPostCommentAdapter.setDataSet(it,mViewModel.isMyPost)
            }
        }
        mViewModel.userFriendReactions.observe(this) {
            it?.getContent()?.let { reactionWrapper ->
                openReactionTab(reactionWrapper)
            }
        }
        mViewModel.postDetailsData.observe(this) {
            updateUI(it)
        }
        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        mViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        mViewModel.emojiUpdatePostAt.observe(this) {
            it?.getContent()?.let { updateEmojiView(it) }
        }
    }


    private fun postComment() {
        val comment = binding.lytPostCommentBottom.etCommentHere.text.toString()
        if (comment.isNotEmpty()) {
            mViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEED_COMMENT_SUBMIT_CLICK)
            mViewModel.addComment(comment)
            uiController.hideSoftKeyboard()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopup(anchor: View) {
        popupView = anchor
        val offset = 168

        nullableBinding?.let {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth: Int = displayMetrics.widthPixels

            val binding = LayoutPostPopUpEmojiBinding.inflate(
                LayoutInflater.from(requireContext()), null, false
            )


            binding.emojiContainer.visible()
            this.popupWindow = PopupWindow(requireContext()).apply {
                isOutsideTouchable = true
                width = screenWidth

                contentView = binding.root.apply {
                    measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                }
                setBackgroundDrawable(BitmapDrawable())
            }.also { popupWindow ->
                val location = IntArray(2).apply {
                    anchor.getLocationOnScreen(this)
                }


                mViewModel.postDetailsData.value?.let { it1 -> handleEmojiClick(it1, binding) }
                handleEmojiView(mViewModel.postDetailsData.value?.userReaction, binding)
                popupWindow.setOnDismissListener {

                }


                popupWindow.showAtLocation(
                    anchor, Gravity.TOP or Gravity.START, (location[0]), location[1] - offset
                )

            }
        }

    }

    private fun handleEmojiClick(
        data: TimelineData,
        binding: LayoutPostPopUpEmojiBinding
    ) {
        binding.ivGiveEmojiFire.setOnClickListener {

            if (binding.ivGiveEmojiFire.background == null) {
                mViewModel.postEmoji(Emoji.EmojiFire)
            } else {
                mViewModel.postEmoji(null)
            }
            handleEmojiView(data.userReaction, binding)

            dismissPopupWindow()
        }

        binding.ivGiveEmojiStrong.setOnClickListener {

            if (binding.ivGiveEmojiStrong.background == null) {
                mViewModel.postEmoji(Emoji.EmojiHand)
            } else {
                mViewModel.postEmoji(null)
            }
            handleEmojiView(data.userReaction, binding)

            dismissPopupWindow()
        }
        binding.ivGiveEmoji100.setOnClickListener {

            if (binding.ivGiveEmoji100.background == null) {
                mViewModel.postEmoji(Emoji.Emoji100)
            } else {
                mViewModel.postEmoji(null)
            }
            handleEmojiView(data.userReaction, binding)

            dismissPopupWindow()
        }
        binding.ivGiveEmojiHeart.setOnClickListener {

            if (binding.ivGiveEmojiHeart.background == null) {
                mViewModel.postEmoji(Emoji.EmojiHeart)
            } else {
                mViewModel.postEmoji(null)
            }
            handleEmojiView(data.userReaction, binding)
            dismissPopupWindow()
        }
    }

    private fun dismissPopupWindow() {
        popupWindow?.dismiss()
    }

    private fun handleEmojiView(
        reaction: String?, binding: LayoutPostPopUpEmojiBinding
    ) {

        when (reaction) {
            Emoji.EmojiHeart.emoji -> {

                binding.ivGiveEmojiHeart.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.circle_secondary)
                binding.ivGiveEmojiStrong.background = null
                binding.ivGiveEmojiFire.background = null
                binding.ivGiveEmoji100.background = null

            }

            Emoji.EmojiHand.emoji -> {
                binding.ivGiveEmojiHeart.background = null
                binding.ivGiveEmojiFire.background = null
                binding.ivGiveEmoji100.background = null
                binding.ivGiveEmojiStrong.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.circle_secondary)
            }

            Emoji.EmojiFire.emoji -> {
                binding.ivGiveEmojiStrong.background = null
                binding.ivGiveEmojiHeart.background = null
                binding.ivGiveEmoji100.background = null
                binding.ivGiveEmojiFire.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.circle_secondary)
            }

            Emoji.Emoji100.emoji -> {
                binding.ivGiveEmojiStrong.background = null
                binding.ivGiveEmojiFire.background = null
                binding.ivGiveEmojiHeart.background = null
                binding.ivGiveEmoji100.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.circle_secondary)
            }

        }


    }

    private fun clearCommentEt() {
        binding.lytPostCommentBottom.etCommentHere.setText("")
    }


    private fun openReactionTab(userFriendReactions: ArrayList<ReactionsWrapper>) {
        navigate(
            PostDetailsFragmentDirections.postDetailsFragToBottomSheetReactions(
                userFriendReactions.toTypedArray()
            )
        )
    }

    private fun updateEmojiView(data: Pair<Emoji?, List<ReactionData>>) {
        data.second.let { reactionData ->
            var likeCount = 0
            reactionData.forEach {
                likeCount += it.count
            }
            if (reactionData.isEmpty()) {
                binding.lytPost.imageViewCircle1.gone()
                binding.lytPost.imageViewCircle2.gone()
                binding.lytPost.imageViewCircle3.gone()
                binding.lytPost.imageViewCircle4.gone()
                binding.lytPost.tvLikeCount.gone()
                binding.lytPost.ivEmojiImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.lytPost.ivEmojiImage.context,
                        R.drawable.ic_post_emoji
                    )
                )
                binding.lytPost.emojiView.gone()
            }
            if (reactionData.size == 1) {
                binding.lytPost.emojiView.visible()
                binding.lytPost.imageViewCircle1.visible()
                binding.lytPost.imageViewCircle2.gone()
                binding.lytPost.imageViewCircle3.gone()
                binding.lytPost.imageViewCircle4.gone()

                binding.lytPost.tvLikeCount.visible()
                binding.lytPost.tvLikeCount.text = likeCount.toString()
                reactionData[0].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle1
                    )
                    loadImage(it, binding.lytPost.ivEmojiImage)
                }

            }
            if (reactionData.size == 2) {
                binding.lytPost.emojiView.visible()
                binding.lytPost.imageViewCircle1.visible()
                binding.lytPost.imageViewCircle2.visible()
                binding.lytPost.imageViewCircle3.gone()
                binding.lytPost.imageViewCircle4.gone()

                binding.lytPost.tvLikeCount.visible()
                binding.lytPost.tvLikeCount.text = likeCount.toString()
                reactionData[0].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle1
                    )
                }
                reactionData[1].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle2
                    )
                    loadImage(it, binding.lytPost.ivEmojiImage)
                }
            }
            if (reactionData.size == 3) {
                binding.lytPost.emojiView.visible()
                binding.lytPost.imageViewCircle1.visible()
                binding.lytPost.imageViewCircle2.visible()
                binding.lytPost.imageViewCircle3.visible()
                binding.lytPost.imageViewCircle4.gone()

                binding.lytPost.tvLikeCount.visible()
                binding.lytPost.tvLikeCount.text = likeCount.toString()
                reactionData[0].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle1
                    )
                }
                reactionData[1].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle2
                    )
                }
                reactionData[2].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle3
                    )
                    loadImage(it, binding.lytPost.ivEmojiImage)
                }
            }
            if (reactionData.size == 4) {
                binding.lytPost.emojiView.visible()
                binding.lytPost.imageViewCircle1.visible()
                binding.lytPost.imageViewCircle2.visible()
                binding.lytPost.imageViewCircle3.visible()
                binding.lytPost.imageViewCircle4.visible()
                binding.lytPost.tvLikeCount.visible()
                binding.lytPost.tvLikeCount.text = likeCount.toString()
                reactionData[0].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle1
                    )

                }
                reactionData[1].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle2
                    )
                }
                reactionData[2].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle3
                    )
                }
                reactionData[3].reactionType?.let {
                    loadImage(
                        it,
                        binding.lytPost.imageViewCircle4
                    )
                    loadImage(it, binding.lytPost.ivEmojiImage)
                }
            }
        }

    }

    private fun updateUI(data: TimelineData?) {
        binding.svMain.visible()
        binding.lytPost.apply {
            binding.lytToolbar.tvTitle.text = data?.name
            tvName.text = data?.name
            if (data?.caption.isNullOrEmpty()) {
                tvContent.gone()
                tvReadMore.gone()
            } else {
                tvContent.visible()
                setContextText(tvContent, data?.caption ?: "", data?.taggedUser)
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
                data?.imageUrl,
                R.drawable.ic_default_profile_image
            )
            if (data?.mediaType == "video" && (data.mediaUrl?.size ?: 0) > 0) {
                //video related
                contentImv.gone()
                lytSvPlayer.root.visible()
                player = ExoPlayer.Builder(lytSvPlayer.videoPlayer.context).build()
                    .also { exoPlayer ->
                        lytSvPlayer.videoPlayer.player = exoPlayer
                        exoPlayer.playWhenReady = false
                        exoPlayer.addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                //do some code
                                if (playbackState == ExoPlayer.STATE_ENDED) {
                                    exoPlayer.seekTo(0)
                                    exoPlayer.pause()
                                    lytSvPlayer.ivPlay.visible()
                                    lytSvPlayer.ivPlay.setImageResource(R.drawable.ic_video_play)
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

                lytSvPlayer.videoPlayer.setControllerVisibilityListener { visibility ->
                    if (visibility == View.VISIBLE) {
                        LOGS.d("Visible")
                        lytSvPlayer.ivPlay.visible()
                    } else {
                        LOGS.d("Hide")
                        lytSvPlayer.ivPlay.gone()
                    }
                }
            } else {
                if (data?.mediaUrl.isNullOrEmpty()) {
                    contentImv.gone()
                } else {
                    contentImv.visible()
                    contentImv.loadImage(
                        contentImv.context,
                        data?.mediaUrl?.firstOrNull(),
                        R.drawable.image_placeholder_voucher
                    )
                }
            }

            val time =
                data?.createdAt?.let {
                    DateFormats.convertDateTimeToTimeStampUTC(
                        it,
                        DateFormats.dateTimeFormat2
                    )
                }
            tvTime.text = time

            if (!data?.location.isNullOrEmpty()) {
                vCircle.visible()
                tvLocation.visible()
                tvLocation.text = data?.location
            }

            when (data?.userType) {
                UserType.Influencer.type -> {
                    imvVerified.visible()
                    if (mViewModel.localDataStore.getUser()?.id?.toLong() == data.userId) {
                        btnAction.visible()
                    } else {
                        btnAction.gone()
                    }
                }

                UserType.Admin.type -> {
                    imvVerified.visible()
                    btnAction.gone()
                }

                UserType.User.type -> {
                    btnAction.visible()
                    imvVerified.gone()
                }
            }

            //todo as discussed we hide view comment as of now
            tvComments.gone()
            data?.userReaction?.let { loadImage(it, ivEmojiImage) }.run {
                ivEmojiImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        ivEmojiImage.context,
                        R.drawable.ic_post_emoji
                    )
                )
            }

            data?.reaction?.let { reactionData ->
                if (reactionData.size == 1) {
                    imageViewCircle1.visible()
                    imageViewCircle2.gone()
                    imageViewCircle3.gone()
                    imageViewCircle4.gone()
                    reactionData[0].reactionType?.let { loadImage(it, imageViewCircle1) }
                }
                if (reactionData.size == 2) {
                    imageViewCircle1.visible()
                    imageViewCircle2.visible()
                    imageViewCircle3.gone()
                    imageViewCircle4.gone()
                    reactionData[0].reactionType?.let { loadImage(it, imageViewCircle1) }
                    reactionData[1].reactionType?.let { loadImage(it, imageViewCircle2) }
                }
                if (reactionData.size == 3) {
                    imageViewCircle1.visible()
                    imageViewCircle2.visible()
                    imageViewCircle3.visible()
                    imageViewCircle4.gone()
                    reactionData[0].reactionType?.let { loadImage(it, imageViewCircle1) }
                    reactionData[1].reactionType?.let { loadImage(it, imageViewCircle2) }
                    reactionData[2].reactionType?.let { loadImage(it, imageViewCircle3) }
                }
                if (reactionData.size == 4) {
                    imageViewCircle1.visible()
                    imageViewCircle2.visible()
                    imageViewCircle3.visible()
                    imageViewCircle4.visible()
                    reactionData[0].reactionType?.let { loadImage(it, imageViewCircle1) }
                    reactionData[1].reactionType?.let { loadImage(it, imageViewCircle2) }
                    reactionData[2].reactionType?.let { loadImage(it, imageViewCircle3) }
                    reactionData[3].reactionType?.let { loadImage(it, imageViewCircle4) }
                }
            }
            if (data?.reaction.isNullOrEmpty()) {
                emojiView.gone()
                tvLikeCount.gone()
            } else {
                emojiView.visible()
                tvLikeCount.visible()
                tvLikeCount.text = data?.likesCount.toString()
            }

            //
            lytComment.root.gone()


        }
    }

    fun loadImage(type: String, imgView: ImageView) {
        imgView.loadCircleEmoji(imgView.context, type)
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
                        if (user.is_active != false) {
                            navigate(R.id.friendProfileFragment, Bundle().apply {
                                putInt("friendId", user.id.toInt())
                            })
                        }
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