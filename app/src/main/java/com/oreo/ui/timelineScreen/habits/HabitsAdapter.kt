package com.oreo.ui.timelineScreen.habits

// HabitsAdapter.kt
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemHabitAddHabitsBinding
import com.noisefit.luna.databinding.ItemSectionHeaderAddHabitsBinding
import com.oreo.data.model.timeline.habits.Habit
import com.oreo.data.model.timeline.habits.HabitListItem

class HabitsAdapter(
    private val onHabitClicked: (Habit) -> Unit
) : ListAdapter<HabitListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    var selectedHabits: Set<String> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ROW = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HabitListItem.Header -> TYPE_HEADER
            is HabitListItem.Row -> TYPE_ROW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                HeaderViewHolder(
                    ItemSectionHeaderAddHabitsBinding.inflate(
                        inflater, parent, false
                    )
                )
            }

            else -> {
                HabitViewHolder(
                    ItemHabitAddHabitsBinding.inflate(
                        inflater, parent, false
                    ),
                    onHabitClicked,
                    createSelectedGradientBorderDrawable(),
                    createUnselectedGradientBorderDrawable()
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HabitListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is HabitListItem.Row -> (holder as HabitViewHolder).bind(item.habit, selectedHabits)
        }
    }

    class HeaderViewHolder(private val binding: ItemSectionHeaderAddHabitsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HabitListItem.Header) {
            binding.tvSectionTitle.text = item.title
        }
    }

    class HabitViewHolder(
        private val binding: ItemHabitAddHabitsBinding,
        private val onHabitClicked: (Habit) -> Unit,
        private val selectedBg: Drawable,
        private val unselectedBg: Drawable,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit, selectedHabits: Set<String>) {
            binding.tvText.text = habit.name
            val isSelected = selectedHabits.contains(habit.id)
            if(isSelected){
                binding.root.background = selectedBg
                binding.ivIcon.setImageResource(R.drawable.ic_checked_lifeos_onboard)
            }else{
                binding.root.background = unselectedBg
                binding.ivIcon.setImageResource(R.drawable.ic_add)
            }

            // Set Click Listeners
            binding.root.setOnClickListener { onHabitClicked(habit) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HabitListItem>() {
        override fun areItemsTheSame(oldItem: HabitListItem, newItem: HabitListItem): Boolean {
            return when {
                oldItem is HabitListItem.Header && newItem is HabitListItem.Header ->
                    oldItem.title == newItem.title

                oldItem is HabitListItem.Row && newItem is HabitListItem.Row ->
                    oldItem.habit.id == newItem.habit.id

                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: HabitListItem, newItem: HabitListItem): Boolean {
            return oldItem == newItem
        }
    }

    fun createSelectedGradientBorderDrawable(): Drawable {
        val borderWidth = 2f
        val gradientColors = intArrayOf("#1AFFFFFF".toColorInt(),"#00FFFFFF".toColorInt())
        val backgroundColor = "#29FFFFFF".toColorInt()

        // Paint for the background fill
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }

        // Paint for the gradient border stroke
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, 400f,
                gradientColors,
                null,
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }

        return object : Drawable() {
            override fun draw(canvas: Canvas) {
                val halfBorder = borderWidth / 2

                // Full rect for background fill
                val fillRect = RectF(
                    0f,
                    0f,
                    bounds.width().toFloat(),
                    bounds.height().toFloat()
                )
                canvas.drawRoundRect(fillRect, 24f, 24f, fillPaint)

                // Inset rect for border
                val strokeRect = RectF(
                    halfBorder,
                    halfBorder,
                    bounds.width() - halfBorder,
                    bounds.height() - halfBorder
                )
                canvas.drawRoundRect(strokeRect, 24f, 24f, strokePaint)
            }

            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(cf: ColorFilter?) {}
            override fun getOpacity() = PixelFormat.TRANSLUCENT
        }
    }

    fun createUnselectedGradientBorderDrawable(): Drawable {
        val borderWidth = 2f
        val gradientColors = intArrayOf("#1AFFFFFF".toColorInt(),"#00FFFFFF".toColorInt())
        val backgroundColor = "#0FFFFFFF".toColorInt()

        // Paint for the background fill
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }

        // Paint for the gradient border stroke
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, 400f,
                gradientColors,
                null,
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }

        return object : Drawable() {
            override fun draw(canvas: Canvas) {
                val halfBorder = borderWidth / 2

                // Full rect for background fill
                val fillRect = RectF(
                    0f,
                    0f,
                    bounds.width().toFloat(),
                    bounds.height().toFloat()
                )
                canvas.drawRoundRect(fillRect, 24f, 24f, fillPaint)

                // Inset rect for border
                val strokeRect = RectF(
                    halfBorder,
                    halfBorder,
                    bounds.width() - halfBorder,
                    bounds.height() - halfBorder
                )
                canvas.drawRoundRect(strokeRect, 24f, 24f, strokePaint)
            }

            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(cf: ColorFilter?) {}
            override fun getOpacity() = PixelFormat.TRANSLUCENT
        }
    }

}