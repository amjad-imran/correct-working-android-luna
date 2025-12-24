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
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemHabitAddHabitsBinding
import com.noisefit.luna.databinding.ItemSectionHeaderAddHabitsBinding
import com.oreo.data.model.timeline.habits.HabitListItem
import com.oreo.data.model.timeline.habits.HabitUi

class HabitsAdapter(
    private val onHabitClicked: (HabitUi) -> Unit
) : ListAdapter<HabitListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    private var selectedHabits: Set<String> = emptySet()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ROW = 1
        private const val TYPE_EMPTY = 2
        private const val PAYLOAD_SELECTION = "payload_selection"
    }

    fun updateSelectedHabits(newSet: Set<String>) {
        selectedHabits = newSet
        // simplest approach:
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTION)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HabitListItem.Header -> TYPE_HEADER
            is HabitListItem.Row -> TYPE_ROW
            HabitListItem.EmptyBottom -> TYPE_EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemSectionHeaderAddHabitsBinding.inflate(inflater, parent, false)
            )
            TYPE_ROW -> HabitViewHolder(
                ItemHabitAddHabitsBinding.inflate(inflater, parent, false),
                onHabitClicked,
                createSelectedGradientBorderDrawable(),
                createUnselectedGradientBorderDrawable()
            )
            else -> EmptyViewHolder(FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    parent.measuredHeight.takeIf { it > 0 } ?: ViewGroup.LayoutParams.MATCH_PARENT
                )
            })
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        when (val item = getItem(position)) {
            is HabitListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is HabitListItem.Row -> {
                val vh = holder as HabitViewHolder
                // if just selection changed, no need to reset text etc (optional optimization)
                if (payloads.contains(PAYLOAD_SELECTION)) {
                    vh.bindSelectionOnly(item.habit, selectedHabits)
                } else {
                    vh.bind(item.habit, selectedHabits)
                }
            }
            HabitListItem.EmptyBottom -> Unit
        }
    }

    class HeaderViewHolder(private val binding: ItemSectionHeaderAddHabitsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HabitListItem.Header) {
            binding.tvSectionTitle.text = item.category.title
        }
    }

    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class HabitViewHolder(
        private val binding: ItemHabitAddHabitsBinding,
        private val onHabitClicked: (HabitUi) -> Unit,
        private val selectedBg: Drawable,
        private val unselectedBg: Drawable
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: HabitUi, selectedHabits: Set<String>) {
            binding.tvText.text = habit.name
            bindSelectionOnly(habit, selectedHabits)
            binding.root.setOnClickListener { onHabitClicked(habit) }
        }

        fun bindSelectionOnly(habit: HabitUi, selectedHabits: Set<String>) {
            val isSelected = selectedHabits.contains(habit.id)
            if (isSelected) {
                binding.root.background = selectedBg
                binding.ivIcon.setImageResource(R.drawable.ic_checked_lifeos_onboard)
            } else {
                binding.root.background = unselectedBg
                binding.ivIcon.setImageResource(R.drawable.ic_add)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HabitListItem>() {
        override fun areItemsTheSame(old: HabitListItem, new: HabitListItem): Boolean {
            return when {
                old is HabitListItem.Header && new is HabitListItem.Header ->
                    old.category.id == new.category.id

                old is HabitListItem.Row && new is HabitListItem.Row ->
                    old.habit.id == new.habit.id

                old is HabitListItem.EmptyBottom && new is HabitListItem.EmptyBottom -> true
                else -> false
            }
        }

        override fun areContentsTheSame(old: HabitListItem, new: HabitListItem): Boolean = old == new
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