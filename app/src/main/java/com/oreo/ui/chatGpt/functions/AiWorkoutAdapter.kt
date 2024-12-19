package com.oreo.ui.chatGpt.functions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.AiWorkout
import com.noisefit.luna.databinding.RowAiWorkoutBinding

class AiWorkoutAdapter(val onWorkoutSelected: (List<AiWorkout>) -> Unit) :
    RecyclerView.Adapter<AiWorkoutAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<AiWorkout>()

    inner class ViewHolder(val binding: RowAiWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AiWorkout) {

            binding.tvWorkoutName.text = data.workout_name
            binding.tvDetails.text = data.reps

            binding.root.setOnClickListener {
                onWorkoutSelected(getWorkouts(bindingAdapterPosition))
            }

        }

    }

    fun getWorkouts(position: Int): List<AiWorkout> {
        return mDataSet.subList(position, mDataSet.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowAiWorkoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: List<AiWorkout>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }


}