import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import java.util.UUID
import androidx.core.graphics.toColorInt

class ChatAdapter(
    private val messages: MutableList<VoiceChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val container: FrameLayout) :
        RecyclerView.ViewHolder(container) {
            val messageText: TextView = container.getChildAt(0) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val frameLayout = FrameLayout(parent.context)
        frameLayout.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val textView = TextView(parent.context)
        textView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.setPadding(32, 20, 32, 20)
        textView.textSize = 16f

        frameLayout.addView(textView)
        return ChatViewHolder(frameLayout)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        val tv = holder.messageText
        val context = tv.context

        tv.text = msg.message

        val params = tv.layoutParams as FrameLayout.LayoutParams

        if (msg.isUser) {
            params.gravity = Gravity.END
            params.setMargins(
                60.dpToPx(context),
                8.dpToPx(context),
                16.dpToPx(context),
                8.dpToPx(context)
            )
            tv.apply {
                background = userBubble(context)
                textSize = 16F
                setTextColor(context.getColor(R.color.white_80))
            }
        } else {
            params.gravity = Gravity.START
            params.setMargins(
                16.dpToPx(context),
                8.dpToPx(context),
                20.dpToPx(context),
                8.dpToPx(context)
            )
            tv.apply {
                setBackgroundColor(Color.TRANSPARENT)
                textSize = 20F
                setTextColor("#CECECE".toColorInt())
            }
        }

        tv.layoutParams = params
    }


    override fun getItemCount() = messages.size

    fun submitMessages(newMessages: List<VoiceChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun userBubble(context: Context): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12.dpToPx(context).toFloat()
            setColor(context.getColor(R.color.white_10))
        }
    }

}

data class VoiceChatMessage(
    val id: UUID,
    var message: String,
    val isUser: Boolean,
    var isStreaming: Boolean = false
)