package com.oreo.ui.femalehealth.cycletracker.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FmhCycleTrackHistoryItemBinding
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.ui.femalehealth.cycletracker.OnHistoryItemClickListener
import kotlin.math.abs

class FMHCycleTrackerHistoryAdapter(val listener: OnHistoryItemClickListener) :
    RecyclerView.Adapter<FMHCycleTrackerHistoryAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<FMHCycleHistoryDataModel>()

    inner class ViewHolder(val binding: FmhCycleTrackHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FMHCycleHistoryDataModel) {
            binding.tvHeader.text = if (bindingAdapterPosition == 0) {
                binding.root.context.getString(R.string.text_current_cycle_days, data.cycleLength)
            } else {
                binding.root.context.getString(R.string.text_value_days, data.cycleLength)
            }
            binding.tvStartedOn.text = binding.root.context.getString(
                R.string.text_started_on_value,
                DateFormats.formatDate(
                    data.periodDate,
                    DateFormats.dateFormat3(),
                    DateFormats.dateFormat7()
                )
            )


            val ovDays = abs(
                DateFormats.getDateDiff(
                    DateFormats.dateFormat3(),
                    data.ovulationStartDate,
                    data.periodDate
                )
            )
            val ovDates = data.fertileWindow?.split("/")

            var ovStart = 0
            var ovEnd = 0
            if (ovDates?.size == 2) {
                ovStart = abs(
                    DateFormats.getDateDiff(
                        DateFormats.dateFormat3(),
                        ovDates[0],
                        data.periodDate
                    )
                ).toInt()

                ovEnd = abs(
                    DateFormats.getDateDiff(
                        DateFormats.dateFormat3(),
                        ovDates[1],
                        data.periodDate
                    )
                ).toInt()
            }


            binding.pbHistory.setData(
                cycleLength = data.cycleLength ?: 0,
                periodLength = data.periodLength ?: 0,
                ovStart = ovStart,
                ovEnd = ovEnd,
                ovDay = ovDays.toInt()
            )

            binding.root.setOnClickListener {
                listener.onHistoryItemClick(data, bindingAdapterPosition)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            FmhCycleTrackHistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: List<FMHCycleHistoryDataModel>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }


}

