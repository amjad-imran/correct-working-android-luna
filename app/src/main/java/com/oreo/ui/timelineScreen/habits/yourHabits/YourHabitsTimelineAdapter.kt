package com.oreo.ui.timelineScreen.habits.yourHabits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemHabitTimelineScreenBinding
import com.oreo.data.model.timeline.habits.HabitsByDateResponse.*

class YourHabitsTimelineAdapter(
    private val onCross: (Options) -> Unit,
    private val onCheck: (Options) -> Unit,
) : ListAdapter<Options, YourHabitsTimelineAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Options>() {
        override fun areItemsTheSame(oldItem: Options, newItem: Options) = oldItem.timeTrackerOptionId == newItem.timeTrackerOptionId
        override fun areContentsTheSame(oldItem: Options, newItem: Options) = oldItem == newItem
    }

    inner class VH(private val binding: ItemHabitTimelineScreenBinding) : RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
        fun bind(item: Options) {
            binding.tvHabitTitle.text = item.options

            when{
                item.isCompleted || item.isCancelled -> {
                    if(item.isCompleted){
                        binding.igTick.setImageResource(R.drawable.ic_hm_check_mark)

                        binding.igCross.setImageResource(R.drawable.ic_cross_disabled_you_habits)
                    }else{
                        binding.igTick.setImageResource(R.drawable.ic_check_disabled_you_habits)

                        binding.igCross.setImageResource(R.drawable.ic_not_done_circadian)
                    }
                }

                else -> {
                    binding.igTick.apply {
                        setImageResource(R.drawable.ic_check_your_habits)

                        setOnClickListener { onCheck(item) }
                    }
                    binding.igCross.apply {
                        setImageResource(R.drawable.ic_close_your_habits)

                        setOnClickListener { onCross(item) }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            ItemHabitTimelineScreenBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}