package com.oreo.ui.sleep2.sleepplanner

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemActiveDaysBinding
import com.noisefit.luna.databinding.ItemAlarmDaysBinding
import com.noisefit_commans.data.model.AlarmTimingsData
import com.oreo.data.model.FHFlowIconsModel
import java.util.Calendar

class SADaysAdapter() :
    RecyclerView.Adapter<SADaysAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Int>()

    inner class ViewHolder(val binding: ItemAlarmDaysBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dayKey: Int) {

            binding.tvTitle.text = getDayName(dayKey, binding.root.context)

        }

        private fun getDayName(dayKey: Int, context: Context?): String {
            return when (dayKey) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"

                else -> ""
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemAlarmDaysBinding.inflate(
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


    fun setData(resultData: List<Int>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }


}



