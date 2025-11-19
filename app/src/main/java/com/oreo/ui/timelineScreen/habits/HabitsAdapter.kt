package com.oreo.ui.timelineScreen.habits

// HabitsAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
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
                    onHabitClicked
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
        private val onHabitClicked: (Habit) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit, selectedHabits: Set<String>) {
            binding.tvText.text = habit.name
            val isSelected = selectedHabits.contains(habit.id)
            if(isSelected){
                binding.root.setBackgroundResource(R.drawable.bg_log_45_perc_transparent_circadian)
                binding.ivIcon.setImageResource(R.drawable.ic_check_circadian)
            }else{
                binding.root.setBackgroundResource(R.drawable.bg_log_45_perc_transparent_circadian)
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
}