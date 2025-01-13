package com.oreo.ui.googlefit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowGoogleFitBodyMeasurementsBinding
import com.noisefit.luna.databinding.RowGoogleFitSleepBinding
import com.noisefit.luna.databinding.RowGoogleFitWorkoutBinding
import com.oreo.data.model.GoogleFitDataDisplayModel
import com.oreo.data.model.GoogleFitDataType

class GoogleFitDataAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mDataSet = ArrayList<GoogleFitDataDisplayModel>()

    inner class ViewHolderWorkout(val binding: RowGoogleFitWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GoogleFitDataDisplayModel) {

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
        if (viewType == GoogleFitDataType.SLEEP.type) {
            val binding =
                RowGoogleFitSleepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolderSleep(binding)
        } else if (viewType == GoogleFitDataType.NAP.type) {
            val binding =
                RowGoogleFitSleepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolderSleep(binding)
        } else if (viewType == GoogleFitDataType.BODY_MEASUREMENTS.type) {
            val binding =
                RowGoogleFitSleepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolderSleep(binding)
        } else {
            val binding = RowGoogleFitWorkoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolderWorkout(binding)
        }
    }

    override fun getItemCount(): Int = mDataSet.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            GoogleFitDataType.SLEEP.type, GoogleFitDataType.NAP.type -> {
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