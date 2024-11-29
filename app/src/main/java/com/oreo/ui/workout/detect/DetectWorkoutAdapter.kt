package com.oreo.ui.workout.detect

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit.luna.databinding.OreoItemDetectWorkoutListBinding
import com.noisefit_commans.constants.SportActivityName
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import java.util.concurrent.TimeUnit


class DetectWorkoutAdapter(
    val detectWorkoutListener: DetectWorkoutListener,
    val resourcesProvider: ResourcesProvider
) :
    RecyclerView.Adapter<DetectWorkoutAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OreoAutoSportData>()

    inner class ViewHolder(val binding: OreoItemDetectWorkoutListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: OreoAutoSportData) {

            val minutes = binding.root.context.getString(
                R.string.text_value_mins,
                TimeUnit.SECONDS.toMinutes(resultData.duration.toLong()).toString()
            )
            binding.tvMin.text = minutes
            //val calories = "${resultData.calories} kcal"
            //binding.tvCalories.text = calories

            binding.tvIntensity.text =
                getIntensity(resultData.intensity ?: 0, binding.tvIntensity.context)

            val time =
                DateFormats.convertTimestampToDate(
                    resultData.startTime,
                    DateFormats.time12Meridian()
                )
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

            val workoutName = resultData.type?.replace("_", " ")
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(DateFormats.defaultLocale) else it.toString() }

            binding.tvTitle.text = getTranslatedName(workoutName, resourcesProvider)

            var isOtherWorkout = false
            if (workoutName.equals(binding.root.context.getString(R.string.text_walking), true) ||
                workoutName.equals(binding.root.context.getString(R.string.text_running), true)
            ) {
                binding.btnEdit.visible()
                binding.btnAdd.text = binding.btnAdd.context.getString(R.string.text_confirm)
            } else {
                binding.btnEdit.gone()
                binding.btnAdd.text = binding.btnAdd.context.getString(R.string.text_identify)
                isOtherWorkout = true
            }


            binding.ivClose.setOnClickListener {
                detectWorkoutListener.onDismissWorkout(resultData, bindingAdapterPosition)
            }
            binding.btnAdd.setOnClickListener {
                if (isOtherWorkout) {
                    detectWorkoutListener.onIdentifyWorkout(resultData, bindingAdapterPosition)
                } else {
                    detectWorkoutListener.onAddWorkout(resultData, bindingAdapterPosition)
                }
            }
            binding.btnEdit.setOnClickListener {
                detectWorkoutListener.onIdentifyWorkout(resultData, bindingAdapterPosition)
            }
        }
    }

    private fun getTranslatedName(
        workoutName: String?,
        resourcesProvider: ResourcesProvider
    ): String {
        if (workoutName.isNullOrEmpty()) {
            return ""
        }

        return when (workoutName.lowercase()) {
            SportActivityName.RUNNING -> {
                resourcesProvider.getString(R.string.text_running).capitalizeWords()
            }

            else -> {
                resourcesProvider.getString(R.string.text_walking).capitalizeWords()
            }
        }
    }

    private fun getIntensity(intensity: Int, context: Context): String {
        return when (intensity) {
            0, 1 -> {
                context.getString(R.string.text_easy)
            }

            2 -> {
                context.getString(R.string.text_moderate)
            }

            else -> {
                context.getString(R.string.text_hard)
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
        } catch (exp: Exception) {
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
    fun onAddWorkout(data: OreoAutoSportData, position: Int)
    fun onIdentifyWorkout(data: OreoAutoSportData, position: Int)
    fun onDismissWorkout(data: OreoAutoSportData, position: Int)
}

