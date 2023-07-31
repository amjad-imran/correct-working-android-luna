package com.noisefit.ui.feeds.feed

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.BottomNavOption
import com.noisefit.MainViewModel
import com.noisefit.luna.R
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.luna.databinding.FragmentFeedBinding
import com.noisefit.luna.databinding.LayoutPostPopUpEmojiBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.feeds.bottomSheet.DELETE_PC_REQUEST_KEY
import com.noisefit.ui.feeds.bottomSheet.EDIT_PC_REQUEST_KEY
import com.noisefit.ui.feeds.bottomSheet.REPORT_PC_REQUEST_KEY
import com.noisefit.ui.feeds.create.CREATE_POST_KEY
import com.noisefit.ui.friends.FriendsFragmentDirections
import com.noisefit.ui.friends.compete.FriendSharedViewModel
import com.noisefit.ui.profile.UserType
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.ui.settings.feedbacknew.LATER
import com.noisefit.ui.settings.feedbacknew.RATE_NOW
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint
import me.dkzwm.widget.srl.RefreshingListenerAdapter


@AndroidEntryPoint
class FeedFragment : BaseFragment<FragmentFeedBinding>(FragmentFeedBinding::inflate) {

    private val viewModel: FeedViewModel by viewModels()
    private var popupWindow: PopupWindow? = null
    private var popupView: View? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private var recyclerManager: LinearLayoutManager? = null
    private val sharedViewModel: FriendSharedViewModel by activityViewModels()

    private val feedAdapter by lazy {
        FeedAdapter()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        binding.animationView.setAnimation(R.raw.loading_swipe_anim)

    }

    override fun onResume() {
        super.onResume()
        viewModel.resetPaginationData()
        viewModel.getDashboardFeed(false)

    }

    override fun onPause() {
        super.onPause()
        feedAdapter.pausePlayer()
    }

    private fun setAdapter() {
        binding.rvTimeline.apply {
            recyclerManager = LinearLayoutManager(requireContext())
            layoutManager = recyclerManager
            adapter = feedAdapter
        }
        feedAdapter.itemClickListener = { type ->
            when (type) {
                is FeedClickEnum.ADS -> {
                    handleAds(type.data.actionId)
                }

                is FeedClickEnum.Comment -> {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEED_COMMENT_CLICK)
                    navigate(
                        FriendsFragmentDirections.actionNavigationFriendsToPostDetailsFragment(
                            type.data.postId,
                        ).apply {
                            showKeyboard = type.showKeyboard
                        }
                    )
                }

                is FeedClickEnum.CommentAction -> {
                    navigate(FriendsFragmentDirections.actionNavigationFriendsToFriendProfileFragment()
                        .apply {
                            this.friendId = type.data.userId.toInt()
                        })
                }

                is FeedClickEnum.Emoji -> {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEED_EMOJI_CLICK)
                    showPopup(type.data, type.view, type.position)
                }

                is FeedClickEnum.ProfileAction -> {
                    viewModel.lastClickedPost = type.data

                    if (viewModel.userId == type.data.userId) {
                        navigate(
                            FriendsFragmentDirections.actionNavigationFriendsToEditPCBottomSheet(
                                type.data.postId, -1
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
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEEDS_REPORTPOST_CLICK)
                        navigate(
                            FriendsFragmentDirections.actionNavigationFriendsToReportPCBottomSheet(
                                type.data.postId, -1, "feed"
                            )
                        )
                    }

                }

                is FeedClickEnum.ReadMore -> {

                }

                is FeedClickEnum.ReactionList -> {
                    navigate(R.id.bottomSheetReactionsPaginate, Bundle().apply {
                        this.putLong("postId", type.postId)
                    })
                    //viewModel.getPostReactions(type.postId)
                }

                is FeedClickEnum.ShowProfile -> {
                    when (type.data.userType) {
                        UserType.Admin.type -> {
                            navigate(R.id.noiseProfile)
                        }

                        else -> {
                            navigate(FriendsFragmentDirections.actionNavigationFriendsToFriendProfileFragment()
                                .apply {
                                    this.friendId = type.data.userId.toInt()
                                })
                        }
                    }

                }

                is FeedClickEnum.ShowCommentProfile -> {
                    navigate(FriendsFragmentDirections.actionNavigationFriendsToFriendProfileFragment()
                        .apply {
                            this.friendId = type.data.comment?.userId ?: -1
                        })
                }

                is FeedClickEnum.TagProfile -> {
                    val id = type.user?.id

                    if (id != null && type.user?.is_active != false) {
                        navigate(R.id.friendProfileFragment, Bundle().apply {
                            putInt("friendId", id.toInt())
                        })
                    } else {
                        context.showShortToast("User not found")
                    }
                }

                is FeedClickEnum.ScrollToPost -> {
                    tryCatch {
                        nullableBinding?.rvTimeline?.post {
                            nullableBinding?.rvTimeline?.scrollToPosition(type.position)
                        }
                    }
                }
            }
        }
    }

    private fun handleAds(actionId: Int) {
        when (actionId) {
            3 -> {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLEGESBANNER_CLICK)
                mainViewModel.navigateTo(BottomNavOption.EXPLORE)
            }

            12 -> {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDSBANNER_CLICK)
                navigate(R.id.addFriendsFragment)
            }
        }
    }

    private fun openDeleteBottomSheet(postId: Long, commentId: Long) {

        navigate(
            FriendsFragmentDirections.actionNavigationFriendsToDeletePCBottomSheet(
                postId, commentId
            )
        )
    }

    override fun initListener() {
        binding.swipeToRefresh.setOnRefreshListener(object : RefreshingListenerAdapter() {
            override fun onRefreshing() {
                super.onRefreshing()
                //binding.swipeToRefresh.refreshComplete()
                viewModel.hasNextData = true
                viewModel.currentPageSeries = 1
                binding.textSyncingData.visible()
                viewModel.getDashboardFeed(true)
            }
        })

        binding.rvTimeline.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.isFeedApiLoading) {
                        return
                    }

                    nullableBinding?.let {
                        val visibleItemCount = it.rvTimeline.layoutManager?.childCount ?: 0
                        val totalItemCount = it.rvTimeline.layoutManager?.itemCount ?: 0
                        val firstVisibleItemPosition =
                            (it.rvTimeline.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            viewModel.getDashboardFeed(false)
                        }
                    }
                    feedAdapter.pausePlayer()
                }
            }

        })

        binding.lytCreatePost.root.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMMUNITY_NEWPOST_CLICK)
            navigate(FriendsFragmentDirections.actionNavigationFriendsToCreatePostFragment())
//            navigate(R.id.postDetailsFragment)
        }


        requireActivity().supportFragmentManager.setFragmentResultListener(
            CREATE_POST_KEY,
            this
        ) { _, bundle ->
            val updated = bundle.getBoolean("updated")
            if (updated) {
                tryCatch {
                    viewModel.clearFeedData()
                    nullableBinding?.rvTimeline?.post {
                        nullableBinding?.rvTimeline?.scrollToPosition(0)
                    }
                    showRatingPopUp()
                }
            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            DELETE_PC_REQUEST_KEY, this
        ) { _, bundle ->
            val isDelete = bundle.getBoolean("delete")
            if (isDelete) {
                viewModel.lastClickedPost?.let {
                    feedAdapter.removeItem(it.postId)
                }

            }

        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            EDIT_PC_REQUEST_KEY, this
        ) { _, bundle ->
            val isDelete = bundle.getBoolean("delete")
            if (isDelete) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.DELETEPOST_CLICK)
                viewModel.lastClickedPost?.let {
                    viewModel.deletePost.postValue(Event(it.postId))
                }
            }

            val isEdit = bundle.getBoolean("edit")
            if (isEdit) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.EDITPOST_CLICK)
                viewModel.lastClickedPost?.let {
                    viewModel.editPost.postValue(Event(it))
                }
            }

        }
    }

    private fun showRatingPopUp() {
        if (viewModel.localDataStore.getFeedPostCreateCount() == 1) {
            requireActivity().supportFragmentManager.setFragmentResultListener(RATE_NOW,this) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.localDataStore.setShowReviewPopUp(false)
                    ShareUtil.openPlayStore(requireContext(), "com.noisefit")
                }
            }
            requireActivity().supportFragmentManager.setFragmentResultListener(LATER,this) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.localDataStore.setLaterNowLastReviewShownTimeStamp(System.currentTimeMillis())
                    viewModel.localDataStore.setShowReviewPopUp(true)
                }
            }
            navigate(R.id.rateNowBottomSheet,Bundle().apply {
                putString("cameFrom","feed")
            })
        }
    }

    override fun subscribeObservers() {
        viewModel.deletePost.observe(this) {
            it.getContent()?.let { postId ->
                openDeleteBottomSheet(postId, -1)
            }

        }

        viewModel.userFriendReactions.observe(this) {
            it?.getContent()?.let { reactionWrapper ->
                openReactionTab(reactionWrapper)
            }
        }
        viewModel.editPost.observe(this) {
            it.getContent()?.let { post ->
                navigate(
                    FriendsFragmentDirections.actionNavigationFriendsToUpdatePostFragment(
                        post
                    )
                )
            }
        }

        viewModel.emojiUpdatePostAt.observe(this) {
            it?.getContent()?.let { reactionTriple ->
                feedAdapter.updateEmojiData(reactionTriple)
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
                binding.progressBarLoadMore.gone()
            }
        }
        viewModel.getInitialLoading().observe(this) {
            if (it) {
                binding.progressBarInitial.visible()
            } else {
                stopRefresh()
                binding.progressBarInitial.gone()
                binding.progressBarLoadMore.gone()
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
                stopRefresh()

            }
        }

        viewModel.getLoadMoreLoading().observe(this) {
            if (it) {
                binding.progressBarLoadMore.visible()
                binding.progressBarInitial.gone()
                /*  tryCatch {
                      nullableBinding?.rvTimeline?.post {
                          nullableBinding?.rvTimeline?.scrollToPosition(feedAdapter.itemCount - 1)
                      }
                  }*/
            } else {
                binding.progressBarLoadMore.gone()
                binding.progressBarInitial.gone()
            }
        }


        viewModel.feedOverviewList.observe(this) {
            it?.let {
                feedAdapter.setDataSet(it)
            }
        }

    }

    private fun openReactionTab(userFriendReactions: ArrayList<ReactionsWrapper>) {
        navigate(
            FriendsFragmentDirections.actionNavigationFriendsFragToBottomSheetReactions(
                userFriendReactions.toTypedArray()
            )
        )
    }

    private fun stopRefresh() {
        binding.swipeToRefresh.refreshComplete()
        binding.textSyncingData.gone()
    }

    private fun dismissPopupWindow() {
        popupWindow?.dismiss()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopup(
        data: TimelineData, anchor: View, position: Int
    ) {
        popupView = anchor
        val offset = 168

        nullableBinding?.let {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth: Int = displayMetrics.widthPixels

            val binding = LayoutPostPopUpEmojiBinding.inflate(
                LayoutInflater.from(anchor.context), null, false
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
                handleEmojiView(data.userReaction, binding)
                popupWindow.setOnDismissListener {

                    nullableBinding?.rvTimeline?.setOnTouchListener(null)
                }


                popupWindow.showAtLocation(
                    anchor, Gravity.TOP or Gravity.START, (location[0]), location[1] - offset
                )

            }
        }

    }

    private fun handleEmojiClick(
        data: TimelineData, position: Int, binding: LayoutPostPopUpEmojiBinding
    ) {
        binding.ivGiveEmojiFire.setOnClickListener {

            if (binding.ivGiveEmojiFire.background == null) {
                viewModel.postEmoji(Emoji.EmojiFire, data.postId, position)
            } else {
                viewModel.postEmoji(null, data.postId, position)
            }
            handleEmojiView(data.userReaction, binding)

            dismissPopupWindow()
        }

        binding.ivGiveEmojiStrong.setOnClickListener {

            if (binding.ivGiveEmojiStrong.background == null) {
                viewModel.postEmoji(Emoji.EmojiHand, data.postId, position)
            } else {
                viewModel.postEmoji(null, data.postId, position)
            }
            handleEmojiView(data.userReaction, binding)

            dismissPopupWindow()
        }
        binding.ivGiveEmoji100.setOnClickListener {

            if (binding.ivGiveEmoji100.background == null) {
                viewModel.postEmoji(Emoji.Emoji100, data.postId, position)
            } else {
                viewModel.postEmoji(null, data.postId, position)
            }
            handleEmojiView(data.userReaction, binding)

            dismissPopupWindow()
        }
        binding.ivGiveEmojiHeart.setOnClickListener {

            if (binding.ivGiveEmojiHeart.background == null) {
                viewModel.postEmoji(Emoji.EmojiHeart, data.postId, position)
            } else {
                viewModel.postEmoji(null, data.postId, position)
            }
            handleEmojiView(data.userReaction, binding)
            dismissPopupWindow()
        }
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


    override fun onDestroyView() {
        super.onDestroyView()
        feedAdapter.stopPlayer()
        dismissPopupWindow()
    }


}