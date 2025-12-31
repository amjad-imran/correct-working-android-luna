package com.oreo.ui.timelineScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemHabitTimelineScreenBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.data.model.timeline.habits.HabitsByDateResponse.*

class ItemHabitsTimelineAdapter(
    private val onCross: (Options) -> Unit,
    private val onCheck: (Options) -> Unit,
    private var isFromLunaDash: Boolean = false,
) : ListAdapter<Options, ItemHabitsTimelineAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Options>() {
        override fun areItemsTheSame(oldItem: Options, newItem: Options) = oldItem.timeTrackerOptionId == newItem.timeTrackerOptionId
        override fun areContentsTheSame(oldItem: Options, newItem: Options) = oldItem == newItem
    }

    inner class VH(private val binding: ItemHabitTimelineScreenBinding) : RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
        fun bind(item: Options, isLastItem: Boolean) {
            binding.tvHabitTitle.text = item.options

            val isSkipping = item.state == Options.State.Skipping

            if(isFromLunaDash) {
                binding.igCross.invisible()
                if(isLastItem) binding.viewDivider.gone()
            }
            else binding.igCross.visible()

            if(isSkipping){
                binding.lytContent.invisible()
                binding.lytSkippedDone.apply {
                    tvHabitTitle.text = item.options
                    root.setBackgroundColor("#1A7E0707".toColorInt())
                    ivCheckStatus.setImageResource(R.drawable.ic_cross_habit_timeline)
                    tvDoneSkipped.text = context.getString(R.string.text_skipped)
                    root.visible()
                }
            }else{
                if(item.isCompleted==true){
                    binding.lytContent.invisible()
                    binding.lytSkippedDone.apply {
                        tvHabitTitle.text = item.options
                        root.setBackgroundColor("#0A81ED8D".toColorInt())
                        ivCheckStatus.setImageResource(R.drawable.ic_check_habit_timeline)
                        tvDoneSkipped.text = context.getString(R.string.text_done)
                        root.visible()
                    }
                }else{
                    binding.lytSkippedDone.root.gone()
                    binding.lytContent.visible()
                }
            }

            // prevent multi-taps
            binding.igCross.isEnabled = !isSkipping
            binding.igCross.setOnClickListener {
                onCross(item)
            }
            binding.igTick.setOnClickListener { onCheck(item) }
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
        holder.bind(getItem(position), position==itemCount-1)
    }
}