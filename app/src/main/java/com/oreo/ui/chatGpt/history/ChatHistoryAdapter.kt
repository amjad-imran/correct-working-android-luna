package com.oreo.ui.chatGpt.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowChatHistoryHeaderBinding
import com.noisefit.luna.databinding.RowChatHistoryThreadBinding
import com.oreo.data.model.ai.ChatHistoryItem


class ChatHistoryAdapter(
    private val listener: ChatHistoryInteraction,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mDataSet = ArrayList<ChatHistoryItem>()

    inner class ViewHolderHeader(val binding: RowChatHistoryHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ChatHistoryItem) {

        }
    }

    inner class ViewHolderThread(val binding: RowChatHistoryThreadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ChatHistoryItem) {

            binding.tvHeadline.text = data.title
            binding.tvMessage.text = data.message

            binding.root.setOnClickListener {
                listener.onThreadClicked(data.threadId)
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
        if (holder.itemViewType == ChaHistoryViewType.DATA.type) {
            (holder as ViewHolderThread).bind(mDataSet[position])
        } else {
            (holder as ViewHolderHeader).bind(mDataSet[position])
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
}

enum class ChaHistoryViewType(val type: Int) {
    HEADER(0), DATA(1)
}

interface ChatHistoryInteraction {
    fun onThreadClicked(threadId: String)
}