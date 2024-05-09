package com.oreo.ui.chatGpt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemChatMessageRecivedListBinding
import com.noisefit.luna.databinding.ItemChatMessageSentListBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.ChatGptOverview


class ChatGptAdapter :
    RecyclerView.Adapter<HallOfFameRecyclerViewHolder>() {


    var items = listOf<ChatGptOverview>()
        set(value) {
            field = value
//            if(refreshPosition != null && refreshPosition != -1){
//                notifyItemChanged(refreshPosition!!)
//            }else{
//                notifyDataSetChanged()
//            }
            notifyDataSetChanged()

        }

    var itemClickListener: ((view: View, item: ChatGptOverview, position: Int) -> Unit)? =
        null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HallOfFameRecyclerViewHolder {
        return when (viewType) {
            R.layout.item_chat_message_sent_list -> HallOfFameRecyclerViewHolder.ChatMessageSentViewHolder(
                ItemChatMessageSentListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_chat_message_recived_list -> HallOfFameRecyclerViewHolder.ChatMessageReceivedViewHolder(
                ItemChatMessageRecivedListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )


            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: HallOfFameRecyclerViewHolder, position: Int) {
        holder.itemClickListener = itemClickListener
        fun update() {

        }
        when (holder) {
            is HallOfFameRecyclerViewHolder.ChatMessageSentViewHolder -> holder.bind(
                items[position] as ChatGptOverview.SentMessage,
                position
            )

            is HallOfFameRecyclerViewHolder.ChatMessageReceivedViewHolder -> holder.bind(
                items[position] as ChatGptOverview.ReceivedMessage,
                position
            )


        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ChatGptOverview.SentMessage -> R.layout.item_chat_message_sent_list
            is ChatGptOverview.ReceivedMessage -> R.layout.item_chat_message_recived_list

        }
    }
}


sealed class HallOfFameRecyclerViewHolder(binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((view: View, item: ChatGptOverview, position: Int) -> Unit)? =
        null

    class ChatMessageSentViewHolder(private val binding: ItemChatMessageSentListBinding) :
        HallOfFameRecyclerViewHolder(binding) {
        fun bind(
            data: ChatGptOverview.SentMessage,
            position: Int
        ) {

            binding.tvMessage.text = data.message
        }
    }

    class ChatMessageReceivedViewHolder(private val binding: ItemChatMessageRecivedListBinding) :
        HallOfFameRecyclerViewHolder(binding) {

        fun bind(
            data: ChatGptOverview.ReceivedMessage,
            position: Int
        ) {

            if (data.thinking) {

                binding.apply {
                    lottie.visible()
                    lottie.setAnimation(R.raw.anim_heart_measure)
                    lottie.playAnimation()
                    tvMessage.gone()
                }

            } else {
                binding.apply {
                    lottie.gone()
                    tvMessage.visible()
                    tvMessage.text = data.message
                }
            }


        }
    }


}