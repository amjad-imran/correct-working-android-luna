package com.oreo.ui.femalehealth.cycletracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FmhCycleHistoryItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.FMHCycleHistoryDataModel
import kotlin.math.abs

class FMHCycleHistoryAdapter(val listener: OnHistoryItemClickListener) :
    RecyclerView.Adapter<FMHCycleHistoryAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<FMHCycleHistoryDataModel>()

    inner class ViewHolder(val binding: FmhCycleHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FMHCycleHistoryDataModel) {

            binding.tvHeader.text = if (bindingAdapterPosition == 0) {
                binding.tvHeader.context.getString(
                    R.string.text_current_cycle_days,
                    data.cycleLength?:0
                )
            } else {
                binding.tvHeader.context.getString(R.string.text_value_days, data.cycleLength?:0)
            }
            binding.tvStartedOn.text = binding.tvStartedOn.context.getString(
                R.string.text_started_on_value, DateFormats.formatDate(
                    data.periodDate,
                    DateFormats.dateFormat3(),
                    DateFormats.dateFormat7()
                )
            )

            var ovDays = -1L
            var ovStart = -1
            var ovEnd = -1
            if (data.ovulationStartDate != null) {
                ovDays = abs(
                    DateFormats.getDateDiff(
                        DateFormats.dateFormat3(),
                        data.ovulationStartDate,
                        data.periodDate
                    )
                )
                val ovDates = data.fertileWindow?.split("/")

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
            }

            val availableDaya = (data.cycleLength ?: 0) - (data.periodLength ?: 0)
            if (availableDaya < 14) {
                ovStart = -1
                ovEnd = -1
                ovDays = -1
            }

            binding.pbHistory.setData(
                cycleLength = data.cycleLength ?: 0,
                periodLength = data.periodLength ?: 0,
                ovStart = ovStart,
                ovEnd = ovEnd,
                ovDay = ovDays.toInt()
            )


            if (bindingAdapterPosition == mDataSet.size - 1) {
                binding.divider1.root.gone()
            } else {
                binding.divider1.root.visible()
            }

            binding.root.setOnClickListener {
                listener.onHistoryItemClick(data, bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            FmhCycleHistoryItemBinding.inflate(
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

interface OnHistoryItemClickListener {
    fun onHistoryItemClick(data: FMHCycleHistoryDataModel, position: Int)
}



