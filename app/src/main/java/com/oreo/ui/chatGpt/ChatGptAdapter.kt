package com.oreo.ui.chatGpt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemAiHeaderMealBinding
import com.noisefit.luna.databinding.ItemAiHeaderWorkoutBinding
import com.noisefit.luna.databinding.ItemChatMessageRecivedListBinding
import com.noisefit.luna.databinding.ItemChatMessageRetryBinding
import com.noisefit.luna.databinding.ItemChatMessageSentListBinding
import com.noisefit.luna.databinding.ItemChatMessageThinkingBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChatGptOverview
import com.oreo.ui.chatGpt.functions.SubMealAdapter
import io.noties.markwon.Markwon
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.CoreProps
import org.commonmark.node.Heading
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.graphics.Color
import com.bumptech.glide.Glide
import android.app.Dialog
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.graphics.drawable.ColorDrawable
import android.content.Context
import java.util.UUID

// Removed standalone attachment bindings; sent message now renders attachment inline


class ChatGptAdapter :
    RecyclerView.Adapter<ChatGptViewItemsHolder>() {

    var itemClickListener: ChatClickListener? = null

    private val likedMessageIds = mutableSetOf<java.util.UUID>()
    private val dislikedMessageIds = mutableSetOf<java.util.UUID>()

    fun markLiked(id: java.util.UUID) {
        likedMessageIds.add(id)
        dislikedMessageIds.remove(id)
        notifyItemChangedById(id)
    }

    fun markDisliked(id: java.util.UUID) {
        dislikedMessageIds.add(id)
        likedMessageIds.remove(id)
        notifyItemChangedById(id)
    }

    private fun notifyItemChangedById(id: java.util.UUID) {
        val list = asyncListDiffer.currentList
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) notifyItemChanged(index)
    }

    private val asyncListDiffer =
        AsyncListDiffer(this, object : DiffUtil.ItemCallback<ChatGptOverview>() {
            override fun areItemsTheSame(
                oldItem: ChatGptOverview,
                newItem: ChatGptOverview
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ChatGptOverview,
                newItem: ChatGptOverview
            ): Boolean {
                return oldItem == newItem
            }
        })


    fun setDataSet(data: List<ChatGptOverview>) {
        val newList = data.toMutableList()
        asyncListDiffer.submitList(newList)
    }

    var items = listOf<ChatGptOverview>()
        set(value) {

            asyncListDiffer.submitList(value)


            //field = value
//            if(refreshPosition != null && refreshPosition != -1){
//                notifyItemChanged(refreshPosition!!)
//            }else{
//                notifyDataSetChanged()
//            }notifyItemInserted(mData.size());
//            notifyItemChanged(items.size)
            //notifyDataSetChanged()

        }


    //    override fun onCurrentListChanged(previousList: MutableList<Item>, currentList: MutableList<Item>) {
//        super.onCurrentListChanged(previousList, currentList)
//        //E.g. check if new item has been added
//        if (currentList.size == previousList.size + 1) {
//            recyclerView.scrollToPosition(currentList.size - 1)
//        }
//    }
    /*var itemClickListener: ((item: ChatGptOverview, position: Int) -> Unit)? =
        null*/

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

            R.layout.item_ai_header_workout -> ChatGptViewItemsHolder.ChatHeaderWorkoutViewHolder(
                ItemAiHeaderWorkoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_ai_header_meal -> ChatGptViewItemsHolder.ChatHeaderMealViewHolder(
                ItemAiHeaderMealBinding.inflate(
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
                asyncListDiffer.currentList[position] as ChatGptOverview.SentMessage,
                position
            )

            is ChatGptViewItemsHolder.ChatMessageReceivedViewHolder -> holder.bind(
                asyncListDiffer.currentList[position] as ChatGptOverview.ReceivedMessage,
                position,
                likedMessageIds.contains((asyncListDiffer.currentList[position] as ChatGptOverview.ReceivedMessage).id),
                dislikedMessageIds.contains((asyncListDiffer.currentList[position] as ChatGptOverview.ReceivedMessage).id)
            )

            is ChatGptViewItemsHolder.ChatMessageRetryViewHolder -> holder.bind(
                asyncListDiffer.currentList[position] as ChatGptOverview.RetryMessage,
                position
            )

            is ChatGptViewItemsHolder.ChatThinkingViewHolder -> holder.bind(
                asyncListDiffer.currentList[position] as ChatGptOverview.ThinkingMessage,
                position
            )

            is ChatGptViewItemsHolder.ChatHeaderWorkoutViewHolder -> holder.bind(
                asyncListDiffer.currentList[position] as ChatGptOverview.HeaderWorkout,
                position
            )

            is ChatGptViewItemsHolder.ChatHeaderMealViewHolder -> holder.bind(
                asyncListDiffer.currentList[position] as ChatGptOverview.HeaderMeal,
                position
            )
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size

    override fun getItemViewType(position: Int): Int {
        val item = asyncListDiffer.currentList[position]
        return when (item) {
            is ChatGptOverview.SentMessage -> R.layout.item_chat_message_sent_list
            is ChatGptOverview.ReceivedMessage -> R.layout.item_chat_message_recived_list
            is ChatGptOverview.RetryMessage -> R.layout.item_chat_message_retry
            is ChatGptOverview.ThinkingMessage -> R.layout.item_chat_message_thinking
            is ChatGptOverview.HeaderWorkout -> R.layout.item_ai_header_workout
            is ChatGptOverview.HeaderMeal -> R.layout.item_ai_header_meal
        }
    }

    fun checkRateState(id: java.util.UUID): Boolean {
        if (likedMessageIds.contains(id)) {
            return true
        }
        if (dislikedMessageIds.contains(id)) {
            return true
        }
        return false
    }
}


sealed class ChatGptViewItemsHolder(binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ChatClickListener? = null

    class ChatMessageSentViewHolder(private val binding: ItemChatMessageSentListBinding) :
        ChatGptViewItemsHolder(binding) {
        fun bind(
            data: ChatGptOverview.SentMessage,
            position: Int
        ) {
            binding.tvMessage.text = data.message

            val src = data.attachmentSource
            val type = data.attachmentMimeType
            if (!src.isNullOrEmpty() && !type.isNullOrEmpty()) {
                if (type.startsWith("image/")) {
                    binding.cardImage.visible()
                    binding.lytDoc.gone()
                    Glide.with(binding.cardImage.context)
                        .load(src)
                        .into(binding.ivImage)

                    // Show full-screen preview on tap
                    binding.cardImage.setOnClickListener {
                        if (src.isNotEmpty()) {
                            showImagePreviewDialog(binding.cardImage.context, src)
                        }
                    }
                } else {
                    binding.cardImage.gone()
                    binding.lytDoc.visible()
                    binding.tvDocName.text = data.attachmentName ?: "Document"
                    binding.tvDocType.text = if (type == "application/pdf") "PDF" else "DOC"
                }
            } else {
                binding.cardImage.gone()
                binding.lytDoc.gone()
            }
        }
    }

    class ChatHeaderMealViewHolder(private val binding: ItemAiHeaderMealBinding) :
        ChatGptViewItemsHolder(binding) {
        fun bind(
            data: ChatGptOverview.HeaderMeal,
            position: Int
        ) {
            binding.lytMeal.apply {
                tvTitle.gone()
                imageView58.gone()
                //tvEditDietPlan.gone()
                root.visible()
                //ivMealImage.loadImage(ivMealImage.context, data.meal.img)
                rvMeals.layoutManager = LinearLayoutManager(binding.root.context)
                rvMeals.adapter = SubMealAdapter(data.meal.meal ?: ArrayList(), {})
            }

        }
    }

    class ChatHeaderWorkoutViewHolder(private val binding: ItemAiHeaderWorkoutBinding) :
        ChatGptViewItemsHolder(binding) {
        fun bind(
            data: ChatGptOverview.HeaderWorkout,
            position: Int
        ) {
            binding.tvWorkoutName.text = data.workout.workout_name
            binding.tvSetsData.text = data.workout.reps
            binding.tvDescription.text = data.workout.description

        }
    }


    class ChatMessageRetryViewHolder(private val binding: ItemChatMessageRetryBinding) :
        ChatGptViewItemsHolder(binding) {
        fun bind(
            data: ChatGptOverview.RetryMessage,
            position: Int
        ) {
            //binding.logo.loadImage(binding.logo.context, R.drawable.ic_chat_error)
            binding.tvMessage.text = data.message

            /*binding.tvRetry.setOnClickListener {
                itemClickListener?.invoke(data, bindingAdapterPosition)
            }*/
        }
    }

    class ChatMessageReceivedViewHolder(private val binding: ItemChatMessageRecivedListBinding) :
        ChatGptViewItemsHolder(binding) {

        fun bind(
            data: ChatGptOverview.ReceivedMessage,
            position: Int,
            liked: Boolean,
            disliked: Boolean
        ) {
            binding.apply {
                tvMessage.visible()

                val ctx = this.tvMessage.context
                val markwon = ChatMarkwonProvider.get(ctx)

                //val markwon = Markwon.create(this.tvMessage.context)

                /*  val markwon = Markwon.builder(this.tvMessage.context)
                      .usePlugin(SoftBreakAddsNewLinePlugin.create())
                      *//*.usePlugin(ImagesPlugin.create())*//*
                    .build()*/

                markwon.setMarkdown(tvMessage, data.message)

                if(data.isGenerating){
                    binding.ivLike.gone()
                    binding.ivDislike.gone()
                    binding.ivCopy.gone()
                }else{
                    binding.ivLike.visible()
                    binding.ivDislike.visible()
                    binding.ivCopy.visible()
                }

                binding.ivLike.setImageResource(
                    if (liked) R.drawable.ic_thumbs_up_v2_filled else R.drawable.ic_thumbs_up_v2
                )
                binding.ivDislike.setImageResource(
                    if (disliked) R.drawable.ic_thumbs_down_v2_filled else R.drawable.ic_thumbs_down_v2
                )

                binding.ivCopy.setOnClickListener {
                    itemClickListener?.onCopyMessage(data)
                }
                binding.ivLike.setOnClickListener {
                    itemClickListener?.onLikeMessage(data)
                }
                binding.ivDislike.setOnClickListener {
                    itemClickListener?.onDislikeMessage(data)
                }
            }
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
                lottie.setAnimation(R.raw.anim_ai_thinking_2)
                lottie.playAnimation()
            }
        }

    }


}

private object ChatMarkwonProvider {
    @Volatile
    private var instance: Markwon? = null

    fun get(context: android.content.Context): Markwon {
        val cached = instance
        if (cached != null) return cached
        return synchronized(this) {
            instance ?: build(context).also { instance = it }
        }
    }

    private fun build(ctx: android.content.Context): Markwon {
        return Markwon.builder(ctx)
            .usePlugin(CorePlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    builder.setFactory(Heading::class.java) { _, props ->
                        val level = CoreProps.HEADING_LEVEL.require(props)
                        when (level) {
                            1 -> arrayOf(
                                StyleSpan(Typeface.BOLD),
                                AbsoluteSizeSpan(spToPx(ctx, 18), false)
                            )

                            2 -> arrayOf(
                                StyleSpan(Typeface.BOLD),
                                AbsoluteSizeSpan(spToPx(ctx, 16), false)
                            )

                            3 -> arrayOf(
                                AbsoluteSizeSpan(spToPx(ctx, 14), false)
                            )

                            in 4..Int.MAX_VALUE -> arrayOf(
                                AbsoluteSizeSpan(spToPx(ctx, 14), false),
                                ForegroundColorSpan(
                                    Color.argb((0.7f * 255).toInt(), 255, 255, 255)
                                )
                            )

                            else -> emptyArray()
                        }
                    }
                }
            })
            .build()
    }
}

private fun spToPx(context: android.content.Context, sp: Int): Int {
    val scaledDensity = context.resources.displayMetrics.scaledDensity
    return (sp * scaledDensity).toInt()
}

private fun showImagePreviewDialog(context: Context, imageUrl: String) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(com.noisefit.luna.R.layout.dialog_image_preview)
    dialog.window?.apply {
        setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))
        clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    val imageView: ImageView = dialog.findViewById(com.noisefit.luna.R.id.ivPreview)
    val close: View = dialog.findViewById(com.noisefit.luna.R.id.ivClose)

    Glide.with(context)
        .load(imageUrl)
        .into(imageView)

    close.setOnClickListener { dialog.dismiss() }
    imageView.setOnClickListener { /* swallow to avoid dismiss */ }
    dialog.setCancelable(true)
    dialog.show()
}


interface ChatClickListener {
    fun onCopyMessage(message: com.oreo.data.model.ChatGptOverview.ReceivedMessage)
    fun onLikeMessage(message: com.oreo.data.model.ChatGptOverview.ReceivedMessage)
    fun onDislikeMessage(message: com.oreo.data.model.ChatGptOverview.ReceivedMessage)
}
