package com.oreo.ui.chatGpt.history

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowChatHistoryHeaderBinding
import com.noisefit.luna.databinding.RowChatHistoryThreadBinding
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ai.ChatHistoryItem


class ChatHistoryAdapter(
    private val listener: ChatHistoryInteraction,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mDataSet = ArrayList<ChatHistoryItem>()

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
            } else {
                ""
            }




            binding.tvTime.text = timeAgo

            //LocalDate.parse(data.date).format(DateTimeFormatter.ofPattern("dd MMM, yyyy"))

            binding.root.setOnClickListener {
                data.threadId?.let {
                    listener.onThreadClicked(it, data.title ?: "")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ChaHistoryViewType.HEADER.type) {
            val binding = RowChatHistoryHeaderBinding.inflate(LayoutInflater.from(parent.context))
            return ViewHolderHeader(binding)
        } else {
            val binding = RowChatHistoryThreadBinding.inflate(LayoutInflater.from(parent.context))
            return ViewHolderThread(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == ChaHistoryViewType.HEADER.type) {
            (holder as ViewHolderHeader).bind(mDataSet[position])
        } else {
            (holder as ViewHolderThread).bind(mDataSet[position])
        }
    }

    override fun getItemCount() = mDataSet.size

    override fun getItemViewType(position: Int): Int {
        return if (mDataSet[position].isHeader) {
            ChaHistoryViewType.HEADER.type
        } else {
            ChaHistoryViewType.DATA.type
        }
    }

    fun setDataSet(dataSet: List<ChatHistoryItem>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun removeItem(pos: Int) {
        mDataSet.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun getThreadId(pos: Int): String? {
        return try {
            mDataSet[pos].threadId
        } catch (exp: Exception) {
            null
        }
    }
}

enum class ChaHistoryViewType(val type: Int) {
    HEADER(0), DATA(1)
}

interface ChatHistoryInteraction {
    fun onThreadClicked(threadId: String, title: String)
}