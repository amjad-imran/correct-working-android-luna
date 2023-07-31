package com.noisefit.ui.challengeNew.detail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.response.ChallengeHistory
import com.noisefit.luna.databinding.CalendarDay2Binding
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats.isAfterToday
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class DayViewAdapter(val listener: OnDayClickListener) :
    RecyclerView.Adapter<DayViewAdapter.DayViewHolder>() {
    private val mDataSet = ArrayList<com.noisefit_commans.data.response.ChallengeHistory>()
    private var mSelectedDate: DateTime? = null


    inner class DayViewHolder(val binding: CalendarDay2Binding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            challengeHistory: com.noisefit_commans.data.response.ChallengeHistory,
            selectedDate: DateTime
        ) {
            val parseFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
            val date = parseFormat.parseDateTime(challengeHistory.date)



            binding.tvDay.text = date.dayOfMonth().getAsText(Locale.ENGLISH)
            binding.tvMonth.text = date.monthOfYear().getAsShortText(Locale.ENGLISH)
            binding.tvWeekDay.text = date.dayOfWeek().getAsText(Locale.ENGLISH).substring(0,1)

            if (selectedDate == date) {
                binding.tvDay.setTextColor(Color.parseColor(binding.tvDay.context.getString(R.color.accent_color)))
                binding.tvMonth.setTextColor(
                    Color.parseColor(
                        binding.tvDay.context.getString(
                            R.color.accent_color
                        )
                    )
                )
                binding.dayBack.visible()
            } else {
                binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                binding.tvMonth.setTextColor(Color.parseColor("#ffffff"))
                binding.dayBack.invisible()


            }
            if (isAfterToday(date)) {
                binding.tvDay.setTextColor(Color.parseColor("#33ffffff"))
                binding.tvMonth.setTextColor(Color.parseColor("#33ffffff"))
                binding.dayBack.invisible()

            }

            binding.root.setOnClickListener {
                if (!isAfterToday(date)) {
                    mSelectedDate = date
                    notifyDataSetChanged()
                    listener.onDayItemClick(date)
                }
            }


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding =
            CalendarDay2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        mSelectedDate?.let { holder.bind(mDataSet[position], it) }
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(
        dataSet: List<com.noisefit_commans.data.response.ChallengeHistory>,
        currentTimes: DateTime
    ) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        mSelectedDate = currentTimes
        notifyDataSetChanged()


    }

}

interface OnDayClickListener {
    fun onDayItemClick(date: DateTime)
}