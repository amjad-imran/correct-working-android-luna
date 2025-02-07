package com.oreo.ui.sleep2.sleepplanner.planner

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.LayoutAlarmSetForViewBinding
import com.oreo.data.model.AlarmDisplayModel
import com.oreo.ui.sleep2.sleepplanner.SADaysAdapter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmsAdapter(val onAlarmClicked: (AlarmDisplayModel) -> Unit) :
    RecyclerView.Adapter<AlarmsAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<AlarmDisplayModel>()

    inner class ViewHolder(val binding: LayoutAlarmSetForViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AlarmDisplayModel) {

            binding.lytAlarmTime.apply {
                this.lytBedTime.tvTitle.text = this.root.context.getString(R.string.text_bedtime)
                this.lytBedTime.ivIcon.setImageResource(R.drawable.ic_bedtime_sleep)

                this.lytWakeupTime.tvTitle.text = this.root.context.getString(R.string.text_wakeup)
                this.lytWakeupTime.ivIcon.setImageResource(R.drawable.ic_wakeup_sleep)


                val bedTime = LocalTime.parse(data.bedTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                val wakeTime =
                    LocalTime.parse(data.wakeTime, DateTimeFormatter.ofPattern("HH:mm:ss"))

                lytBedTime.tvTime.text = bedTime.format(DateTimeFormatter.ofPattern("hh:mm"))
                lytBedTime.tvTimeUnit.text = bedTime.format(DateTimeFormatter.ofPattern("a")).lowercase()
                lytBedTime.tvTimeUnit.setTextColor(Color.parseColor("#99FFFFFF"))

                lytWakeupTime.tvTime.text = wakeTime.format(DateTimeFormatter.ofPattern("hh:mm"))
                lytWakeupTime.tvTimeUnit.text = wakeTime.format(DateTimeFormatter.ofPattern("a")).lowercase()
                lytWakeupTime.tvTime.setTextColor(Color.parseColor("#99FFFFFF"))
                lytWakeupTime.tvTimeUnit.setTextColor(Color.parseColor("#99FFFFFF"))

            }



            binding.rvDays.adapter = SADaysAdapter().apply {
                this.setData(data.selectedDays)
            }

            binding.root.setOnClickListener {
                onAlarmClicked(data)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutAlarmSetForViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(dataSet: List<AlarmDisplayModel>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}