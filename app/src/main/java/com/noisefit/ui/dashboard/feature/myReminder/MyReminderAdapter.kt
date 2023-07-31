package com.noisefit.ui.dashboard.feature.myReminder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemMyReminderBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.utils.DateFormats


class MyReminderAdapter(val listener: ReminderAction) :
    RecyclerView.Adapter<MyReminderAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<ReminderList.Reminder>()
    private var mEditMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMyReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(reminder: ReminderList.Reminder) {

            binding.tvReminderText.text = "Reminder ${bindingAdapterPosition + 1}"
            binding.tvReminderTitle.text = reminder.label

            if (reminder.month == 0 && reminder.day == 0 && reminder.year == 0) {
                binding.tvReminderDate.invisible()

            }
            binding.tvReminderTime.text = DateFormats.formatTimeWithAmPm(reminder.hour, reminder.minute)
            binding.tvReminderDate.text =
                DateFormats.getReminderDate(reminder.month, reminder.day, reminder.year)

            binding.root.setOnClickListener {
                listener.onItemClicked(bindingAdapterPosition, reminder)
            }

            binding.ivRemove.setOnClickListener {
                listener.onItemRemoved(bindingAdapterPosition, reminder)
            }

            if (mEditMode) {
                binding.ivRemove.visible()
            } else {
                binding.ivRemove.gone()
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMyReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun getCurrentDataSet(): List<ReminderList.Reminder> {
        return mDataSet
    }

    fun removeItem(position: Int) {
        try {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        } catch (exp: ArrayIndexOutOfBoundsException) {
            exp.printStackTrace()
            //CASE : when Swap is in progress
        }

    }

    fun setDataSet(reminders: List<ReminderList.Reminder>) {
        mDataSet = reminders as ArrayList<ReminderList.Reminder>
        notifyDataSetChanged()
    }
}

interface ReminderAction {
    fun onItemClicked(position: Int, reminder: ReminderList.Reminder)
    fun onItemRemoved(position: Int, reminder: ReminderList.Reminder)
}