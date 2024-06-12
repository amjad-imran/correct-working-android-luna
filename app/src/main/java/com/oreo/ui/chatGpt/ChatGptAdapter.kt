package com.oreo.ui.chatGpt

import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemChatMessageRecivedListBinding
import com.noisefit.luna.databinding.ItemChatMessageRetryBinding
import com.noisefit.luna.databinding.ItemChatMessageSentListBinding
import com.noisefit.luna.databinding.ItemChatMessageThinkingBinding
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChatGptOverview
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin


class ChatGptAdapter :
    RecyclerView.Adapter<ChatGptViewItemsHolder>() {


    var items = listOf<ChatGptOverview>()
        set(value) {
            field = value
//            if(refreshPosition != null && refreshPosition != -1){
//                notifyItemChanged(refreshPosition!!)
//            }else{
//                notifyDataSetChanged()
//            }notifyItemInserted(mData.size());
//            notifyItemChanged(items.size)
            notifyDataSetChanged()

        }


    //    override fun onCurrentListChanged(previousList: MutableList<Item>, currentList: MutableList<Item>) {
//        super.onCurrentListChanged(previousList, currentList)
//        //E.g. check if new item has been added
//        if (currentList.size == previousList.size + 1) {
//            recyclerView.scrollToPosition(currentList.size - 1)
//        }
//    }
    var itemClickListener: ((item: ChatGptOverview, position: Int) -> Unit)? =
        null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatGptViewItemsHolder {
        return when (viewType) {
            R.layout.item_chat_message_sent_list -> ChatGptViewItemsHolder.ChatMessageSentViewHolder(
                ItemChatMessageSentListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_chat_message_recived_list -> ChatGptViewItemsHolder.ChatMessageReceivedViewHolder(
                ItemChatMessageRecivedListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_chat_message_thinking -> ChatGptViewItemsHolder.ChatThinkingViewHolder(
                ItemChatMessageThinkingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_chat_message_retry -> ChatGptViewItemsHolder.ChatMessageRetryViewHolder(
                ItemChatMessageRetryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )


            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: ChatGptViewItemsHolder, position: Int) {
        holder.itemClickListener = itemClickListener
        fun update() {

        }
        when (holder) {
            is ChatGptViewItemsHolder.ChatMessageSentViewHolder -> holder.bind(
                items[position] as ChatGptOverview.SentMessage,
                position
            )

            is ChatGptViewItemsHolder.ChatMessageReceivedViewHolder -> holder.bind(
                items[position] as ChatGptOverview.ReceivedMessage,
                position
            )

            is ChatGptViewItemsHolder.ChatMessageRetryViewHolder -> holder.bind(
                items[position] as ChatGptOverview.RetryMessage,
                position
            )

            is ChatGptViewItemsHolder.ChatThinkingViewHolder -> holder.bind(
                items[position] as ChatGptOverview.ThinkingMessage,
                position
            )
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ChatGptOverview.SentMessage -> R.layout.item_chat_message_sent_list
            is ChatGptOverview.ReceivedMessage -> R.layout.item_chat_message_recived_list
            is ChatGptOverview.RetryMessage -> R.layout.item_chat_message_retry
            is ChatGptOverview.ThinkingMessage -> R.layout.item_chat_message_thinking
        }
    }
}


sealed class ChatGptViewItemsHolder(binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((item: ChatGptOverview, position: Int) -> Unit)? =
        null

    class ChatMessageSentViewHolder(private val binding: ItemChatMessageSentListBinding) :
        ChatGptViewItemsHolder(binding) {
        fun bind(
            data: ChatGptOverview.SentMessage,
            position: Int
        ) {

            binding.logo.loadImage(binding.logo.context, data.userImage)
            binding.tvMessage.text = data.message
        }
    }

    class ChatMessageRetryViewHolder(private val binding: ItemChatMessageRetryBinding) :
        ChatGptViewItemsHolder(binding) {
        fun bind(
            data: ChatGptOverview.RetryMessage,
            position: Int
        ) {
            binding.logo.loadImage(binding.logo.context, R.drawable.ic_chat_error)
            binding.tvMessage.text = data.message

            binding.tvRetry.setOnClickListener {
                itemClickListener?.invoke(data, bindingAdapterPosition)
            }
        }
    }

    class ChatMessageReceivedViewHolder(private val binding: ItemChatMessageRecivedListBinding) :
        ChatGptViewItemsHolder(binding) {

        fun bind(
            data: ChatGptOverview.ReceivedMessage,
            position: Int
        ) {
            binding.apply {
                tvMessage.visible()


                val markwon = Markwon.create(this.tvMessage.context)

              /*  val markwon = Markwon.builder(this.tvMessage.context)
                    .usePlugin(SoftBreakAddsNewLinePlugin.create())
                    *//*.usePlugin(ImagesPlugin.create())*//*
                    .build()*/

                markwon.setMarkdown(tvMessage, data.message)
                logo.visible()
            }
        }

        fun TextView.animateTextWithUnderscore(mText: CharSequence, delayMillis: Long = 15) {
            text = null
            var mTextView = this
            var index = 0
            val handler = Handler()

            val typewriterRunnable = object : Runnable {
                override fun run() {
                    val newText = "${mText.subSequence(0, index)}_"// <-- underscore is optioanal
                    text = newText

                    if (index < mText.length) {
                        handler.postDelayed(this, delayMillis)
                    }
                    index++
                    LOGS.d("SDAsdasdasdasdasda $index --> ${mText.length + 1}")
                    if (index == mText.length) {
                        LOGS.d("SDAsdasdasdasdasda")
                        mTextView.clearAnimation()
                        text = null

                    }
                }
            }

            handler.postDelayed(typewriterRunnable, delayMillis)
        }


    }

    class ChatThinkingViewHolder(private val binding: ItemChatMessageThinkingBinding) :
        ChatGptViewItemsHolder(binding) {

        fun bind(
            data: ChatGptOverview.ThinkingMessage,
            position: Int
        ) {
            binding.apply {
                lottie.repeatCount = LottieDrawable.INFINITE
                lottie.setAnimation(R.raw.anim_ai_thinking)
                lottie.playAnimation()
            }
        }

        fun TextView.animateTextWithUnderscore(mText: CharSequence, delayMillis: Long = 15) {
            text = null
            var mTextView = this
            var index = 0
            val handler = Handler()

            val typewriterRunnable = object : Runnable {
                override fun run() {
                    val newText = "${mText.subSequence(0, index)}_"// <-- underscore is optioanal
                    text = newText

                    if (index < mText.length) {
                        handler.postDelayed(this, delayMillis)
                    }
                    index++
                    LOGS.d("SDAsdasdasdasdasda $index --> ${mText.length + 1}")
                    if (index == mText.length) {
                        LOGS.d("SDAsdasdasdasdasda")
                        mTextView.clearAnimation()
                        text = null

                    }
                }
            }

            handler.postDelayed(typewriterRunnable, delayMillis)
        }


    }


}