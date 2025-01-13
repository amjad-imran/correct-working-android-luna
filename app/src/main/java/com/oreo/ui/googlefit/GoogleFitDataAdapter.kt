package com.oreo.ui.googlefit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowGoogleFitBodyMeasurementsBinding
import com.noisefit.luna.databinding.RowGoogleFitSleepBinding
import com.noisefit.luna.databinding.RowGoogleFitWorkoutBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.GoogleFitDataDisplayModel
import com.oreo.data.model.GoogleFitDataType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class GoogleFitDataAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mDataSet = ArrayList<GoogleFitDataDisplayModel>()

    inner class ViewHolderWorkout(val binding: RowGoogleFitWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GoogleFitDataDisplayModel) {

            val instant = Instant.ofEpochSecond(data.startTime)
            val instantEnd = Instant.ofEpochSecond(data.endTime)

            val start = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            val end = ZonedDateTime.ofInstant(instantEnd, ZoneId.systemDefault())
            binding.tvDate.text = start.format(DateTimeFormatter.ofPattern("dd MMM yy"))

            binding.lytStartTime.apply {
                this.tvValue.text = start.format(DateTimeFormatter.ofPattern("hh:mm"))
                this.tvUnit.text = start.format(DateTimeFormatter.ofPattern("a"))
            }

            binding.lytEndTime.apply {
                this.tvValue.text = end.format(DateTimeFormatter.ofPattern("hh:mm"))
                this.tvUnit.text = end.format(DateTimeFormatter.ofPattern("a"))
            }

            val duration = data.endTime - data.startTime
            val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                duration.toInt()
            )
            if (hour == 0) {
                binding.lytDurationHour.root.gone()
            } else {
                binding.lytDurationHour.apply {
                    tvValue.text = "$hour"
                    tvUnit.text = "hr"
                    this.root.visible()
                }
            }

            binding.lytDurationMin.apply {
                tvValue.text = "$minute"
                tvUnit.text = "min"
                this.root.visible()
            }


        }
    }

    inner class ViewHolderSleep(val binding: RowGoogleFitSleepBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GoogleFitDataDisplayModel) {

        }
    }

    inner class ViewHolderBodyMeasurements(val binding: RowGoogleFitBodyMeasurementsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GoogleFitDataDisplayModel) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            GoogleFitDataType.SLEEP.type -> {
                val binding =
                    RowGoogleFitSleepBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ViewHolderSleep(binding)
            }

            GoogleFitDataType.NAP.type -> {
                val binding =
                    RowGoogleFitSleepBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ViewHolderSleep(binding)
            }

            GoogleFitDataType.BODY_MEASUREMENTS.type -> {
                val binding =
                    RowGoogleFitBodyMeasurementsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ViewHolderBodyMeasurements(binding)
            }

            else -> {
                val binding = RowGoogleFitWorkoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ViewHolderWorkout(binding)
            }
        }
    }

    override fun getItemCount(): Int = mDataSet.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            GoogleFitDataType.SLEEP.type,
            GoogleFitDataType.NAP.type -> {
                (holder as ViewHolderSleep).bind(mDataSet[position])
            }

            GoogleFitDataType.BODY_MEASUREMENTS.type -> {
                (holder as ViewHolderSleep).bind(mDataSet[position])
            }

            else -> {
                (holder as ViewHolderWorkout).bind(mDataSet[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mDataSet[position].type.type
    }

    fun setDataSet(dataSet: List<GoogleFitDataDisplayModel>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}