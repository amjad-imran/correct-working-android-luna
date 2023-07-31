package com.noisefit.ui.feeds.comment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.timeline.CommentData
import com.noisefit.luna.databinding.FragmentPostCommentBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.feeds.bottomSheet.DELETE_PC_REQUEST_KEY
import com.noisefit.ui.feeds.bottomSheet.EDIT_PC_REQUEST_KEY
import com.noisefit.ui.feeds.bottomSheet.REPORT_PC_REQUEST_KEY
import com.noisefit.util.*
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@Deprecated("Use PostDetailsFragment")
@AndroidEntryPoint
class PostCommentFragment :
    BaseFragment<FragmentPostCommentBinding>(FragmentPostCommentBinding::inflate) {

    private val viewModel: PostCommentViewModel by viewModels()
    val args: PostCommentFragmentArgs by navArgs()

    private val postCommentAdapter by lazy {
        PostCommentAdapter(object : OnPostCommentItemClickListener {
            override fun onReportAbuse(position: Int, commentData: CommentData) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEEDS_REPORTCOMMENT_CLICK)
                viewModel.commentId = commentData.commentId
                navigate(
                    PostCommentFragmentDirections.actionPostCommentFragmentToReportPCBottomSheet(
                        commentData.postId,
                        commentData.commentId,
                        "comment"
                    )
                )
            }

            override fun onDeleteComment(position: Int, commentData: CommentData) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEEDS_REPORTCOMMENT_CLICK)
                viewModel.commentId = commentData.commentId
                navigate(
                    PostCommentFragmentDirections.actionPostCommentFragmentToEditPCBottomSheet(
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

    private fun openDeleteBottomSheet(postId: Long, commentId: Long) {
        LOGS.d("openDeleteBottomSheet delete")

        navigate(
            PostCommentFragmentDirections.actionPostCommentFragmentToDeletePCBottomSheet(
                postId,
                commentId
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.caption = args.content
        viewModel.postId = args.id
        args.taggedUsers?.let { users ->
            viewModel.taggedUser.addAll(users.toList())
        }
        setAdapter()
        viewModel.getComment()
        initView()
    }

    private fun initView() {
        if (viewModel.caption.isEmpty()) {
            binding.tvCaption.gone()
        } else {
            setContextText(binding.tvCaption, viewModel.caption ?: "", viewModel.taggedUser)
            binding.tvCaption.doOnPreDraw {
                val lines = TextMeasurementUtil.getTextLines(binding.tvCaption).size
                if (lines > AppConstants.MIN_LINES) {
                    binding.tvReadMore.visible()
                } else {
                    binding.tvReadMore.gone()
                }
            }
        }




        binding.tvReadMore.setOnClickListener {
            binding.tvCaption.visible()
            binding.tvCaption.visible()
            if (binding.tvReadMore.text == binding.tvReadMore.context.getString(R.string.text_read_more)) {
                binding.tvReadMore.text =
                    binding.tvReadMore.context.getText(R.string.text_read_less)
                binding.tvCaption.maxLines = AppConstants.MAX_LINES
            } else {
                binding.tvReadMore.text =
                    binding.tvReadMore.context.getText(R.string.text_read_more)
                binding.tvCaption.maxLines = AppConstants.MIN_LINES
            }
            setContextText(binding.tvCaption, viewModel.caption ?: "", viewModel.taggedUser)
        }
        binding.lytPostCommentBottom.imageview.loadCircleImage(
            requireActivity(),
            viewModel.user?.imageUrl
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setContextText(
        tvContent: TextView,
        caption: String,
        taggedUser: List<MentionUser>?
    ) {
        tvContent.text = caption.generatePostSpan()
        tvContent.setOnTouchListener { v, event ->
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

    private fun setAdapter() {
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postCommentAdapter
        }
    }

    override fun initListener() {

        binding.lytPostCommentBottom.etCommentHere.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                postComment()
                true
            } else false
        })

        binding.ivBack.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytPostCommentBottom.btnPostComment.setOnClickListener {
            postComment()
        }
//
//        binding.lytPostCommentBottom.etCommentHere.onDone {
//            postComment()
//        }


        setFragmentResultListener(EDIT_PC_REQUEST_KEY) { _, bundle ->
            val isDelete = bundle.getBoolean("delete")
            if (isDelete) {
                viewModel.updateDeleteBottomSheet(true)
            }

            val isEdit = bundle.getBoolean("edit")
            if (isEdit) {

            }

        }

        setFragmentResultListener(DELETE_PC_REQUEST_KEY) { _, bundle ->
            val isDelete = bundle.getBoolean("delete")
            if (isDelete) {
                if (viewModel.commentId != -1L) {
                    viewModel.removeComment(viewModel.commentId)
                    viewModel.commentId = -1

                }

            }

        }

        setFragmentResultListener(REPORT_PC_REQUEST_KEY) { _, bundle ->
            val isReport = bundle.getBoolean("report")
            LOGS.d("REPORT_INSIDE $isReport ${viewModel.commentId}")
            if (isReport) {
                if (viewModel.commentId != -1L) {
                    viewModel.removeComment(viewModel.commentId)
                    //postCommentAdapter.removeItem(viewModel.commentId)
                    viewModel.commentId = -1

                }

            }

        }

    }

    private fun postComment() {
        val comment = binding.lytPostCommentBottom.etCommentHere.text.toString()
        if (comment.isNotEmpty()) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEED_COMMENT_SUBMIT_CLICK)
            viewModel.addComment(comment)
            uiController.hideSoftKeyboard()
        }
    }

    private fun clearCommentEt() {
        binding.lytPostCommentBottom.etCommentHere.setText("")
    }

    override fun subscribeObservers() {
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it.getContent()?.let { res ->
                uiController.onApiErrorReceived(res)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.commentPosted.observe(this) {
            it.getContent()?.let { posted ->
                if (posted) {
                    clearCommentEt()
                }
            }
        }
        viewModel.userCommentList.observe(this) {
            it?.let {
                binding.lytPostCommentBottom.root.visible()
                postCommentAdapter.setDataSet(it,false)
            }
        }

        viewModel.showDeleteBottomSheet.observe(this) {
            it?.getContent()?.let { status ->
                if (status) {
                    openDeleteBottomSheet(viewModel.postId!!, viewModel.commentId!!)
                }
            }
        }
    }


}