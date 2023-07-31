package com.noisefit.ui.feeds.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.data.model.timeline.CommentData
import com.noisefit.databinding.LayoutOriginalPostCommentBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.profile.UserType
import com.noisefit.util.TextMeasurementUtil
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.DateFormats


class PostCommentAdapter(val listener: OnPostCommentItemClickListener) :
    RecyclerView.Adapter<PostCommentAdapter.ViewHolder>() {


    val mDataSet = ArrayList<CommentData>()
    var isMyPost: Boolean = false

    inner class ViewHolder(val binding: LayoutOriginalPostCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CommentData, position: Int) {
            binding.apply {
                val time =
                    data.updatedAt?.let {
                        DateFormats.convertDateTimeToTimeStampUTC(
                            it,
                            DateFormats.dateTimeFormat2
                        )
                    }

                tvName.text = data.name
                tvTime.text = time
                if (data.comment.isNullOrEmpty()) {
                    tvContent.gone()
                    tvReadMore.gone()
                } else {
                    tvContent.visible()
                    tvContent.text = data.comment
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
                    data.imageUrl,
                    R.drawable.ic_default_profile_image
                )

                when (data.userType) {
                    UserType.Influencer.type, UserType.Admin.type -> {
                        if (!data.isMyComment) {
                            binding.btnAction.invisible()
                        }
                        binding.imvVerified.visible()

                    }

                    else -> {
                        binding.btnAction.visible()
                        binding.imvVerified.gone()
                    }
                }

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
                    tvContent.text = data.comment
                }

                binding.btnAction.setOnClickListener {
                    if (isMyPost) {
                        listener.onDeleteComment(position, data)
                    } else {
                        if (data.isMyComment) {
                            listener.onDeleteComment(position, data)
                        } else {
                            listener.onReportAbuse(position, data)
                        }
                    }
                }

                binding.userImv.setOnClickListener {
                    listener.onShowProfile(data)
                }
                binding.tvName.setOnClickListener {
                    listener.onShowProfile(data)
                }

            }


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutOriginalPostCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    fun removeItem(commentId: Long?) {
        if (commentId == null) return

        val index = mDataSet.indexOfFirst {
            it.commentId == commentId
        }

        tryCatch {
            mDataSet.removeAt(index)
            notifyItemRemoved(index)
        }

    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<CommentData>, isMyPosts: Boolean) {
        isMyPost = isMyPosts
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }
}

interface OnPostCommentItemClickListener {
    fun onReportAbuse(position: Int, commentData: CommentData)

    fun onDeleteComment(position: Int, commentData: CommentData)

    fun onShowProfile(commentData: CommentData)
}