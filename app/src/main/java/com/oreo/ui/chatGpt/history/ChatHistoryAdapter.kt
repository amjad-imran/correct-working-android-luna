package com.oreo.ui.chatGpt.history

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowChatHistoryHeaderBinding
import com.noisefit.luna.databinding.RowChatHistoryThreadBinding
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ai.ChatHistoryItem
import androidx.core.graphics.drawable.toDrawable


class ChatHistoryAdapter(
    private val listener: ChatHistoryInteraction,
) : ListAdapter<ChatHistoryItem, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ChatHistoryItem>() {

            override fun areItemsTheSame(
                oldItem: ChatHistoryItem,
                newItem: ChatHistoryItem
            ): Boolean {
                // Headers: same date/header key
                if (oldItem.isHeader && newItem.isHeader) {
                    return oldItem.date == newItem.date &&
                            oldItem.headerOther == newItem.headerOther
                }

                // Threads: same thread id
                return oldItem.threadId != null &&
                        oldItem.threadId == newItem.threadId
            }

            override fun areContentsTheSame(
                oldItem: ChatHistoryItem,
                newItem: ChatHistoryItem
            ): Boolean {
                // If ChatHistoryItem is a data class, this is enough:
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolderHeader(val binding: RowChatHistoryHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatHistoryItem) {
            binding.tvDate.text = data.headerOther ?: DateFormats.formatDateTime(
                data.date, DateFormats.dateFormat3(),
                DateFormats.dateFormat6()
            )
        }
    }

    inner class ViewHolderThread(val binding: RowChatHistoryThreadBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatHistoryItem) {
            binding.tvTitle.text = data.title
            binding.tvMessage.text = data.title

            val timeAgo = if (data.timestamp != null) {
                DateUtils.getRelativeTimeSpanString(
                    data.timestamp!! * 1000,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
            } else ""

            binding.tvTime.text = timeAgo

            binding.root.setOnClickListener {
                data.threadId?.let {
                    listener.onThreadClicked(it, data.title ?: "")
                }
            }

            binding.ivOption.setOnClickListener {
                data.threadId?.let { it1 -> showOptionPopup(it, it1) }
            }
        }

        private fun showOptionPopup(anchor: View, threadId: String) {
            val context = anchor.context
            val contentView = LayoutInflater.from(context)
                .inflate(R.layout.popup_item_menu_chat_history, null, false)

            val popupWindow = PopupWindow(
                contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                isOutsideTouchable = true
                elevation = 12f
                setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                isClippingEnabled = false
            }

            contentView.findViewById<View>(R.id.llDelete).setOnClickListener {
                popupWindow.dismiss()
                listener.onDeleteThreadClicked(threadId)
            }

            // Measure popup width
            contentView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val popupWidth = contentView.measuredWidth

            anchor.post {
                // Right edge of popup = right edge of anchor
                val xOff = anchor.width - popupWidth

                // Popup top = anchor bottom
                val yOff = 0

                popupWindow.showAsDropDown(anchor, xOff, yOff)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ChaHistoryViewType.HEADER.type) {
            val binding = RowChatHistoryHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ViewHolderHeader(binding)
        } else {
            val binding = RowChatHistoryThreadBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ViewHolderThread(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder.itemViewType == ChaHistoryViewType.HEADER.type) {
            (holder as ViewHolderHeader).bind(item)
        } else {
            (holder as ViewHolderThread).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isHeader) {
            ChaHistoryViewType.HEADER.type
        } else {
            ChaHistoryViewType.DATA.type
        }
    }

    // helper for swipe/delete etc.
    fun getThreadId(pos: Int): String? = currentList.getOrNull(pos)?.threadId
}

enum class ChaHistoryViewType(val type: Int) {
    HEADER(0), DATA(1)
}

interface ChatHistoryInteraction {
    fun onThreadClicked(threadId: String, title: String)
    fun onDeleteThreadClicked(threadId: String)
}