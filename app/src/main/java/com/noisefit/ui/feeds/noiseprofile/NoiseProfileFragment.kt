package com.noisefit.ui.feeds.noiseprofile

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.NoiseProfileData
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.databinding.FragmentNoiseProfileBinding
import com.noisefit.databinding.LayoutPostPopUpEmojiBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.friends.profile.timeline.OnTimelineInteractionListener
import com.noisefit.ui.friends.profile.timeline.TimelineAdapterWithoutPl
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.prettyCount
import com.noisefit_commans.utils.prettyCountDecimal
import dagger.hilt.android.AndroidEntryPoint
import me.dkzwm.widget.srl.RefreshingListenerAdapter

@AndroidEntryPoint
class NoiseProfileFragment :
    BaseFragment<FragmentNoiseProfileBinding>(FragmentNoiseProfileBinding::inflate) {
    private val mViewModel: NoiseProfileViewModel by viewModels()

    private var popupWindow: PopupWindow? = null
    private var popupView: View? = null
    private var recyclerManager: LinearLayoutManager? = null

    var isLoading = false

    private val mAdapter: TimelineAdapterWithoutPl by lazy {
        TimelineAdapterWithoutPl(object : OnTimelineInteractionListener {
            override fun onCommentsClick(
                timeLineData: TimelineData,
                position: Int,
                showKeyboard: Boolean
            ) {
                navigate(R.id.postDetailsFragment, Bundle().apply {
                    putLong("id", timeLineData.postId)
                    putBoolean("showKeyboard",showKeyboard)
                })
            }

            override fun onReactionClick(postId: Long) {
                navigate(R.id.bottomSheetReactionsPaginate, Bundle().apply {
                    this.putLong("postId", postId)
                })
                //mViewModel.getPostReactions(postId)
            }

            override fun onTaggedUserClicked(user: MentionUser) {
                navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", user.id.toInt())
                })
            }

            override fun onProfileActionClick(timeLineData: TimelineData, position: Int) {
                mViewModel.lastClickedPost = timeLineData
                timeLineData.comment?.isMyComment?.let {
                    openBottomSheet(
                        timeLineData.postId, -1,
                        it
                    )
                }
            }

            override fun onCommentActionClick(timeLineData: TimelineData, position: Int) {
                timeLineData.comment?.isMyComment?.let {
                    openBottomSheet(
                        timeLineData.postId, timeLineData.comment?.postId ?: -1,
                        it
                    )
                }
            }

            override fun onEmojiClick(
                view: View,
                pos: Int,
                context: Context,
                timeLineData: TimelineData
            ) {
                showPopup(timeLineData, view, pos)
            }

            override fun onUserProfileActionClick(timeLineData: TimelineData) {

            }

            override fun onCommentUserProfileActionClick(timeLineData: TimelineData) {
                navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", timeLineData.comment?.userId ?: -1)
                })
            }

            override fun scrollTOPosition(position: Int) {
                tryCatch {
                    nullableBinding?.rvTimeline?.post {
                        nullableBinding?.rvTimeline?.scrollToPosition(position)
                    }
                }
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytContainer.gone()
        binding.animationView.setAnimation(R.raw.loading_swipe_anim)
        setRecycler()
        mViewModel.getNoiseProfile()
    }

    private fun setRecycler() {
        with(binding.rvTimeline) {
            recyclerManager = LinearLayoutManager(requireContext())
            layoutManager = recyclerManager
            adapter = mAdapter
        }

    }

    override fun initListener() {

        binding.swipeToRefresh.setOnRefreshListener(object : RefreshingListenerAdapter() {
            override fun onRefreshing() {
                super.onRefreshing()
                binding.swipeToRefresh.refreshComplete()
                mViewModel.clearTimelineData()
                mViewModel.currentPageSeries = 1
                mViewModel.timelinePageLimit = -1
                mViewModel.lastDataSize = -1
                mViewModel.getUserTimeLineWithoutPl()
            }
        })

        binding.lytContainer.setOnScrollChangeListener(object :
            NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(
                v: NestedScrollView,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                    mViewModel.getUserTimeLineWithoutPl()
                    isLoading = false;
                }
                mAdapter.pausePlayer()
            }


        })

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

        mViewModel.getInitialLoading().observe(this) {
            if (it) {
                binding.progressBarInitial.visible()
            } else {
                binding.progressBarInitial.gone()
                binding.progressBarLoadMore.gone()
            }
        }

        mViewModel.getLoadMoreLoading().observe(this) {
            if (it) {
                binding.progressBarLoadMore.visible()
                binding.progressBarInitial.gone()
                tryCatch {
                    nullableBinding?.rvTimeline?.post {
                        nullableBinding?.rvTimeline?.scrollToPosition(mAdapter.itemCount - 1)
                    }
                }
            } else {
                binding.progressBarLoadMore.gone()
                binding.progressBarInitial.gone()
            }
        }


        mViewModel.emojiUpdatePostAt.observe(this) {
            it?.getContent()?.let { reactionTriple ->
                mAdapter.updateEmojiData(reactionTriple)
            }
        }
        mViewModel.userFriendReactions.observe(this) {
            it?.getContent()?.let { reactionWrapper ->
                openReactionTab(reactionWrapper)
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
                    NoiseProfileFragmentDirections.actionNoiseProfileFragmentToUpdatePostFragment(
                        post
                    )
                )
            }
        }

        mViewModel.noiseProfileData.observe(this) {
            if (it != null) {
                binding.lytContainer.visible()
                updateTopUi(it)
            } else {
                navigateUpSafe()
            }
        }
        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }


        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        mViewModel.updatePostAt.observe(this) {
            it?.getContent()?.let { position ->
                mAdapter.notifyItemChanged(position)
            }
        }
        mViewModel.timeLineList.observe(this) {
            it?.let {
                mAdapter.setDataSet(it, mViewModel.localDataStore.getUser()?.id)
            }

        }
        mViewModel.newPageData.observe(this) {
            mAdapter.addNewData(it)


        }
    }

    private fun openDeleteBottomSheet(postId: Long, commentId: Long) {
        LOGS.d("openDeleteBottomSheet delete")
        navigate(
            NoiseProfileFragmentDirections.actionNoiseProfileFragmentToDeletePCBottomSheet(
                postId,
                commentId
            )
        )
    }

    private fun openBottomSheet(postId: Long, commentId: Long, myComment: Boolean) {
        if (myComment) {
            navigate(
                NoiseProfileFragmentDirections.actionNoiseProfileFragmentToEditPCBottomSheet(
                    postId,
                    commentId
                )
            )
        } else {
            navigate(
                NoiseProfileFragmentDirections.actionNoiseProfileFragmentToReportPCBottomSheet(
                    postId,
                    commentId,
                    "ut"
                )
            )
        }
    }

    private fun openReactionTab(userFriendReactions: ArrayList<ReactionsWrapper>) {
        navigate(
            NoiseProfileFragmentDirections.actionNoiseProfileFragmentToBottomSheetReactions(
                userFriendReactions.toTypedArray()
            )
        )
    }

    private fun updateTopUi(resultData: NoiseProfileData) {
        binding.lytTopProfile.ivProfile.loadImage(
            requireContext(),
            resultData.imageUrl,
            R.drawable.ic_default_profile_image
        )


        binding.lytTopProfile.tvCommunityCount.text = resultData.community.prettyCountDecimal()
        binding.lytTopProfile.tvPostsCount.text = resultData.postCount.toString()
        binding.lytTopProfile.tvDescription.text = resultData.introduction

        binding.lytTopProfile.textView11.text = resultData.firstName
        binding.lytToolbar.tvTitle.text = resultData.firstName
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun showPopup(
        data: TimelineData,
        anchor: View,
        position: Int
    ) {
        popupView = anchor
        val offset = 168

        nullableBinding?.let {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth: Int = displayMetrics.widthPixels

            val binding =
                LayoutPostPopUpEmojiBinding.inflate(
                    LayoutInflater.from(anchor.context),
                    null,
                    false
                )


            binding.emojiContainer.visible()
            this.popupWindow = PopupWindow(anchor.context).apply {
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


                handleEmojiClick(data, position, binding)
                handleEmojiView(data, binding)
                popupWindow.setOnDismissListener {

                    nullableBinding?.rvTimeline?.setOnTouchListener(null)
                }


                popupWindow.showAtLocation(
                    anchor,
                    Gravity.TOP or Gravity.START,
                    (location[0]),
                    location[1] - offset
                )

            }
        }

    }

    private fun handleEmojiClick(
        data: TimelineData,
        position: Int,
        binding: LayoutPostPopUpEmojiBinding
    ) {
        binding.ivGiveEmojiFire.setOnClickListener {

            if (binding.ivGiveEmojiFire.background == null) {

                mViewModel.postEmoji(Emoji.EmojiFire, data.postId, position)

            } else {
                mViewModel.postEmoji(null, data.postId, position)

            }

            handleEmojiView(data, binding)
            dismissPopupWindow()
        }

        binding.ivGiveEmojiStrong.setOnClickListener {
            if (binding.ivGiveEmojiStrong.background == null) {
                mViewModel.postEmoji(Emoji.EmojiHand, data.postId, position)
            } else {
                mViewModel.postEmoji(null, data.postId, position)
            }
            handleEmojiView(data, binding)
            dismissPopupWindow()
        }
        binding.ivGiveEmoji100.setOnClickListener {
            if (binding.ivGiveEmoji100.background == null) {
                mViewModel.postEmoji(Emoji.Emoji100, data.postId, position)
            } else {
                mViewModel.postEmoji(null, data.postId, position)
            }
            handleEmojiView(data, binding)
            dismissPopupWindow()
        }
        binding.ivGiveEmojiHeart.setOnClickListener {
            if (binding.ivGiveEmojiHeart.background == null) {
                mViewModel.postEmoji(Emoji.EmojiHeart, data.postId, position)
            } else {
                mViewModel.postEmoji(null, data.postId, position)
            }

            handleEmojiView(data, binding)
            dismissPopupWindow()
        }
    }

    private fun handleEmojiView(
        data: TimelineData,
        binding: LayoutPostPopUpEmojiBinding
    ) {

        when (data.userReaction) {
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


    private fun dismissPopupWindow() {
        popupWindow?.dismiss()
    }

    override fun onResume() {
        super.onResume()
        mAdapter.pausePlayer()
        mViewModel.getUserTimeLineWithoutPl()
    }

    override fun onPause() {
        super.onPause()
        mAdapter.pausePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdapter.stopPlayer()
    }

}