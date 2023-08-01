package com.oreo.ui.workout.detect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoItemDetectWorkoutListBinding
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.utils.DateFormats
import java.util.concurrent.TimeUnit


class DetectWorkoutAdapter(val detectWorkoutListener: DetectWorkoutListener) :
    RecyclerView.Adapter<DetectWorkoutAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OreoAutoSportData>()

    inner class ViewHolder(val binding: OreoItemDetectWorkoutListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: OreoAutoSportData) {

            val minutes = "${TimeUnit.SECONDS.toMinutes(resultData.duration.toLong())} mins"
            binding.tvMin.text = minutes
            val calories = "${resultData.calories} kcal"
            binding.tvCalories.text = calories
            binding.tvStart.text =
                DateFormats.convertTimestampToDate(resultData.startTime, DateFormats.time12Meridian).lowercase()
            binding.tvTitle.text = resultData.type
            binding.btnCancel.setOnClickListener {
                detectWorkoutListener.onDismissWorkout()
            }
            binding.btnIdentify.setOnClickListener {
                detectWorkoutListener.onIdentifyWorkout()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoItemDetectWorkoutListBinding.inflate(
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

    fun setData(resultData: List<OreoAutoSportData>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }
}

interface DetectWorkoutListener {
    fun onIdentifyWorkout()
    fun onDismissWorkout()
}

