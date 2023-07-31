package com.oreo.ui.workout.detect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoItemDetectWorkoutListBinding


class DetectWorkoutAdapter(val detectWorkoutListener: DetectWorkoutListener) :
    RecyclerView.Adapter<DetectWorkoutAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<String>()

    inner class ViewHolder(val binding: OreoItemDetectWorkoutListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: String) {

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

    fun setData(resultData: ArrayList<String>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }
}

interface DetectWorkoutListener {
    fun onIdentifyWorkout()
    fun onDismissWorkout()
}

