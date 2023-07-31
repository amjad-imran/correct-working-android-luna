package com.noisefit.ui.friends.profile.timeline

//import com.noisefit.ui.friends.profile.FriendProfileFragment.Companion.svMain
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
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.luna.databinding.FragmentTimeLineBinding
import com.noisefit.luna.databinding.LayoutPostPopUpEmojiBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.feeds.bottomSheet.DELETE_PC_REQUEST_KEY
import com.noisefit.ui.feeds.bottomSheet.EDIT_PC_REQUEST_KEY
import com.noisefit.ui.feeds.bottomSheet.REPORT_PC_REQUEST_KEY
import com.noisefit.ui.friends.profile.FriendProfileFragmentDirections
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint


private const val FRIEND_ID = "FRIEND_ID"

@AndroidEntryPoint
class TimelineFragment :
    BaseFragment<FragmentTimeLineBinding>(FragmentTimeLineBinding::inflate) {

    private val viewModel: TimelineViewModel by viewModels()
    private var popupWindow: PopupWindow? = null
    private var popupView: View? = null

    private val timelineAdapter by lazy {
        TimelineAdapterWithoutPl(object : OnTimelineInteractionListener {
            override fun onCommentsClick(
                timeLineData: TimelineData,
                position: Int,
                showKeyboard: Boolean
            ) {
                navigate(
                    R.id.postDetailsFragment, Bundle().apply {
                        putLong("id", timeLineData.postId)
                        putBoolean("showKeyboard", showKeyboard)
                    }
                )
            }

            override fun onTaggedUserClicked(user: MentionUser) {
                if (user.is_active != false) {
                    navigate(R.id.friendProfileFragment, Bundle().apply {
                        putInt("friendId", user.id.toInt())
                    })
                }
            }

            override fun onReactionClick(postId: Long) {
                navigate(R.id.bottomSheetReactionsPaginate, Bundle().apply {
                    this.putLong("postId", postId)
                })
                //viewModel.getPostReactions(postId)
            }

            override fun onProfileActionClick(timeLineData: TimelineData, position: Int) {
                viewModel.lastClickedPost = timeLineData

                openBottomSheet(
                    timeLineData.postId, -1,
                    timeLineData.userId
                )

            }

            override fun onCommentActionClick(timeLineData: TimelineData, position: Int) {

                openBottomSheet(
                    timeLineData.postId, timeLineData.comment?.postId ?: -1,
                    timeLineData.userId
                )

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
                navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", timeLineData.userId.toInt())
                })
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

    private fun openDeleteBottomSheet(postId: Long, commentId: Long) {
        LOGS.d("openDeleteBottomSheet delete")
        navigate(
            FriendProfileFragmentDirections.actionFriendProfileFragmentToDeletePCBottomSheet(
                postId,
                commentId
            )
        )
    }

    private fun openBottomSheet(postId: Long, commentId: Long, userId: Long) {
        if (viewModel.localDataStore.getUser()?.id?.toLong() == userId) {
            navigate(
                FriendProfileFragmentDirections.actionFriendProfileFragmentToEditPCBottomSheet(
                    postId,
                    commentId
                )
            )
        } else {
            setFragmentResultListener(REPORT_PC_REQUEST_KEY) { _, bundle ->
                val isReport = bundle.getBoolean("report")

            }
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.UT_REPORTPOST_CLICK)
            navigate(
                FriendProfileFragmentDirections.actionFriendProfileFragmentToReportPCBottomSheet(
                    postId,
                    commentId,
                    "ut"
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.friendId = it.getLong(FRIEND_ID)

        }
    }

    companion object {
        @JvmStatic
        fun newInstance(friendId: Long) =
            TimelineFragment().apply {
                arguments = Bundle().apply {
                    putLong(FRIEND_ID, friendId)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getUserTimeLineWithoutPl()
        setAdapter()


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
                            viewModel.getUserTimeLineWithoutPl()
                        }
                    }


                }
            }

        })

    }


    private fun setAdapter() {
        binding.rvTimeline.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timelineAdapter

        }

    }

    override fun initListener() {


        binding.lytCreatePost.root.setOnClickListener {
            navigate(R.id.createPostFragment)
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            EDIT_PC_REQUEST_KEY,
            this
        ) { _, bundle ->
            val isDelete = bundle.getBoolean("delete")
            val postId = bundle.getInt("postId")
            val commentId = bundle.getInt("commentId")
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


        requireActivity().supportFragmentManager.setFragmentResultListener(
            DELETE_PC_REQUEST_KEY,
            this
        ) { _, bundle ->
            val isDelete = bundle.getBoolean("delete")

            if (isDelete) {
                timelineAdapter.removePost(viewModel.lastClickedPost)
            }

        }
    }


    override fun subscribeObservers() {

        viewModel.userFriendReactions.observe(this) {
            it?.getContent()?.let { reactionWrapper ->
                openReactionTab(reactionWrapper)
            }
        }


        viewModel.emojiUpdatePostAt.observe(this) {
            it?.getContent()?.let { reactionTriple ->
                timelineAdapter.updateEmojiData(reactionTriple)
            }
        }


        viewModel.deletePost.observe(this) {
            it.getContent()?.let { postId ->
                openDeleteBottomSheet(postId, -1)
            }

        }
        viewModel.editPost.observe(this) {
            it.getContent()?.let { post ->
                navigate(
                    FriendProfileFragmentDirections.actionFriendProfileFragmentToUpdatePostFragment(
                        post
                    )
                )
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
            }
        }

        viewModel.getInitialLoading().observe(this) {
            if (it) {
                binding.progressBarInitial.visible()
            } else {
                binding.progressBarInitial.gone()
                binding.progressBarLoadMore.gone()
            }
        }

        viewModel.getLoadMoreLoading().observe(this) {
            if (it) {
                binding.progressBarLoadMore.visible()
                binding.progressBarInitial.gone()
                tryCatch {
                    nullableBinding?.rvTimeline?.post {
                        nullableBinding?.rvTimeline?.scrollToPosition(timelineAdapter.itemCount - 1)
                    }
                }
            } else {
                binding.progressBarLoadMore.gone()
                binding.progressBarInitial.gone()
            }
        }
        viewModel.getApiErrors().observe(this) {
            it.getContent()?.let { res ->
                uiController.onApiErrorReceived(res)
            }
        }
        viewModel.timeLineList.observe(this) {

            if (viewModel.isMyProfile()) {
                binding.lytCreatePost.root.visible()
            } else {
                binding.lytCreatePost.root.gone()
            }

            if (it.isNotEmpty()) {
                timelineAdapter.setDataSet(it, viewModel.localDataStore.getUser()?.id)
                binding.tvEmptyPlaceholder.gone()
                binding.rvTimeline.visible()
            } else {
                if (viewModel.isMyProfile()) {
                    binding.tvEmptyPlaceholder.visible()
                    binding.tvEmptyPlaceholder.text = getString(R.string.text_no_post_found_me)
                    binding.rvTimeline.gone()
                } else {
                    binding.tvEmptyPlaceholder.visible()
                    binding.tvEmptyPlaceholder.text = getString(R.string.text_no_post_found)
                    binding.rvTimeline.gone()
                }
            }
        }
        viewModel.newPageData.observe(this) {
            timelineAdapter.addNewData(it)


        }



        viewModel.updatePostAt.observe(this) {
            it?.getContent()?.let { position ->
                timelineAdapter.notifyItemChanged(position)
            }
        }
    }

    private fun openReactionTab(userFriendReactions: ArrayList<ReactionsWrapper>) {
        navigate(
            FriendProfileFragmentDirections.actionFriendProfileFragmentFragToBottomSheetReactions(
                userFriendReactions.toTypedArray()
            )
        )
    }

    private fun dismissPopupWindow() {
        popupWindow?.dismiss()
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

                viewModel.postEmoji(Emoji.EmojiFire, data.postId, position)

            } else {
                viewModel.postEmoji(null, data.postId, position)

            }

            handleEmojiView(data, binding)
            dismissPopupWindow()
        }

        binding.ivGiveEmojiStrong.setOnClickListener {
            if (binding.ivGiveEmojiStrong.background == null) {
                viewModel.postEmoji(Emoji.EmojiHand, data.postId, position)
            } else {
                viewModel.postEmoji(null, data.postId, position)
            }
            handleEmojiView(data, binding)
            dismissPopupWindow()
        }
        binding.ivGiveEmoji100.setOnClickListener {
            if (binding.ivGiveEmoji100.background == null) {
                viewModel.postEmoji(Emoji.Emoji100, data.postId, position)
            } else {
                viewModel.postEmoji(null, data.postId, position)
            }
            handleEmojiView(data, binding)
            dismissPopupWindow()
        }
        binding.ivGiveEmojiHeart.setOnClickListener {
            if (binding.ivGiveEmojiHeart.background == null) {
                viewModel.postEmoji(Emoji.EmojiHeart, data.postId, position)
            } else {
                viewModel.postEmoji(null, data.postId, position)
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


    override fun onDestroyView() {
        super.onDestroyView()
        popupView?.alpha = 1f
        dismissPopupWindow()
    }


}