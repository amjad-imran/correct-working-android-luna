package com.oreo.ui.workout.detect

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
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

            val time =
                DateFormats.convertTimestampToDate(resultData.startTime, DateFormats.time12Meridian)
                    .lowercase()
            val timeArray = time.split(" ")
            if (timeArray.isNotEmpty() && timeArray.size == 2) {
                binding.tvStart.text = buildSpannedString {
                    append(timeArray[0])
                    inSpans(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                binding.tvStart.context,
                                R.color.white_48
                            )
                        )
                    ) {
                        append(" ${timeArray[1].lowercase()}")
                    }
                }
            } else {
                binding.tvStart.text = time
            }

            binding.tvTitle.text = resultData.type?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(DateFormats.defaultLocale) else it.toString() }
            binding.btnCancel.setOnClickListener {
                detectWorkoutListener.onDismissWorkout(resultData, bindingAdapterPosition)
            }
            binding.btnIdentify.setOnClickListener {
                detectWorkoutListener.onIdentifyWorkout(resultData, bindingAdapterPosition)
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

    fun removeItem(position: Int) {
        try {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        } catch (exp: ArrayIndexOutOfBoundsException) {
            exp.printStackTrace()
            //CASE : when Swap is in progress
        }

    }
    fun setData(resultData: List<OreoAutoSportData>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }
}

interface DetectWorkoutListener {
    fun onIdentifyWorkout(data: OreoAutoSportData, position: Int)
    fun onDismissWorkout(data: OreoAutoSportData, position: Int)
}

