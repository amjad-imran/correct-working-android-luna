package com.oreo.ui.googlefit

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowGoogleFitBodyMeasurementsBinding
import com.noisefit.luna.databinding.RowGoogleFitSleepBinding
import com.noisefit.luna.databinding.RowGoogleFitWorkoutBinding
import com.noisefit.ui.onboarding.onboardProfile.DefaultWeightInKg
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.common.setTextGradient
import com.noisefit_commans.models.BodyMeasurementGoogleFit
import com.noisefit_commans.models.WorkoutGoogleFit
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import com.oreo.data.model.GoogleFitDataDisplayModel
import com.oreo.data.model.GoogleFitDataType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class GoogleFitDataAdapter(val isMetric: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mDataSet = ArrayList<GoogleFitDataDisplayModel>()

    inner class ViewHolderWorkout(val binding: RowGoogleFitWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GoogleFitDataDisplayModel) {

            val instant = Instant.ofEpochSecond(data.startTime)
            val instantEnd = Instant.ofEpochSecond(data.endTime)

            val start = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            val end = ZonedDateTime.ofInstant(instantEnd, ZoneId.systemDefault())
            binding.tvDate.text = DateFormats.getOrdinalDate(start.toLocalDate(), 1)

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

            val rawData =
                Gson().fromJson<WorkoutGoogleFit>(data.rawData ?: "")//do this in viewmodel instead
            val calories = rawData.calories

            if (calories == null || calories == 0.0f) {
                binding.lytCaloriesBurn.apply {
                    this.root.gone()
                }
                binding.vCalories.gone()
            } else {
                binding.lytCaloriesBurn.apply {
                    tvValue.text = "${calories.roundToInt()}"
                    tvUnit.text = "kcal"
                    this.root.visible()
                }
                binding.vCalories.visible()
            }

            if (data.isSelected) {
                binding.ivSelect.setImageResource(R.drawable.ic_google_fit_selected)
            } else {
                binding.ivSelect.setImageResource(R.drawable.ic_google_fit_default)
            }

            binding.root.setOnClickListener {
                data.isSelected = data.isSelected.not()
                notifyItemChanged(bindingAdapterPosition)
            }

        }
    }

    inner class ViewHolderSleep(val binding: RowGoogleFitSleepBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GoogleFitDataDisplayModel) {

            if (data.type == GoogleFitDataType.NAP) {
                binding.tvTitle.text = binding.tvTitle.context.getString(R.string.text_nap)
            } else {
                binding.tvTitle.text = binding.tvTitle.context.getString(R.string.text_sleep)
            }

            val instant = Instant.ofEpochSecond(data.startTime)
            val instantEnd = Instant.ofEpochSecond(data.endTime)

            val start = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            val end = ZonedDateTime.ofInstant(instantEnd, ZoneId.systemDefault())

            binding.tvDate.text = DateFormats.getOrdinalDate(start.toLocalDate(), 1)

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

            if (data.isSelected) {
                binding.ivSelect.setImageResource(R.drawable.ic_google_fit_selected)
            } else {
                binding.ivSelect.setImageResource(R.drawable.ic_google_fit_default)
            }

            binding.root.setOnClickListener {
                data.isSelected = data.isSelected.not()
                notifyItemChanged(bindingAdapterPosition)
            }

        }
    }

    inner class ViewHolderBodyMeasurements(val binding: RowGoogleFitBodyMeasurementsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GoogleFitDataDisplayModel) {

            val measurementData = Gson().fromJson<BodyMeasurementGoogleFit>(data.rawData ?: "")

            if (measurementData.gFitHeight != null) {
                binding.lytHeight.apply {

                    tvChangedValue.setTextGradient(
                        Color.parseColor("#ffffff"),
                        Color.parseColor("#DDC5FF"),
                        Color.parseColor("#A665FF")
                    )

                    this.tvTitle.text = tvTitle.context.getString(R.string.height)
                    tvCurrentValue.text =
                        if (isMetric) {
                            "${measurementData.userHeight}"
                        } else {
                            "${DistanceUtil.convertCmsToInch(measurementData.userHeight).toDouble().roundToInt()}"
/*
                            val (feet, inches) = DistanceUtil.convertCmToFeetAndInches(
                                measurementData.userHeight.toDouble()
                            )
                            "$feet'${inches.roundToInt()}\""*/
                        }

                    val instant = Instant.ofEpochSecond(measurementData.userTimeStamp)
                    val start = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

                    val instantChanged = Instant.ofEpochSecond(
                        measurementData.gFitHeight?.timeStamp ?: ZonedDateTime.now().toEpochSecond()
                    )
                    val changedTime =
                        ZonedDateTime.ofInstant(instantChanged, ZoneId.systemDefault())

                    tvCurrentDate.text = DateFormats.getOrdinalDate(start.toLocalDate(), 1)
                    tvChangedDate.text =
                        DateFormats.getOrdinalDate(changedTime.toLocalDate(), 1)

                    tvChangedValue.text = if (isMetric) {
                        "${measurementData.gFitHeight!!.value}"
                    } else {
                        "${DistanceUtil.convertCmsToInch(measurementData.gFitHeight!!.value).toDouble().roundToInt()}"

                        /*val (feet, inches) = DistanceUtil.convertCmToFeetAndInches(measurementData.gFitHeight!!.value.toDouble())
                        "$feet'${inches.roundToInt()}\""*/
                    }

                    this.root.visible()
                }
            } else {
                binding.lytHeight.apply {
                    this.root.gone()
                }
            }

            if (measurementData.gFitWeight != null) {
                binding.lytWeight.apply {
                    this.tvTitle.text = tvTitle.context.getString(R.string.weight)

                    tvCurrentValue.text = if (isMetric) {
                        "${measurementData.userWeight}"
                    } else {
                        DistanceUtil.convertKgToLbs(measurementData.userWeight).toDouble()
                            .roundToInt()
                            .toString()
                    }

                    tvChangedValue.setTextGradient(
                        Color.parseColor("#ffffff"),
                        Color.parseColor("#DDC5FF"),
                        Color.parseColor("#A665FF")
                    )

                    val instant = Instant.ofEpochSecond(measurementData.userTimeStamp)
                    val start = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

                    val instantChanged = Instant.ofEpochSecond(
                        measurementData.gFitHeight?.timeStamp ?: ZonedDateTime.now().toEpochSecond()
                    )
                    val changedTime =
                        ZonedDateTime.ofInstant(instantChanged, ZoneId.systemDefault())

                    tvCurrentDate.text = DateFormats.getOrdinalDate(start.toLocalDate(), 1)
                    tvChangedDate.text =
                        DateFormats.getOrdinalDate(changedTime.toLocalDate(), 1)


                    val userWeight = measurementData.gFitWeight!!.value
                    tvChangedValue.text = if (isMetric) {
                        "$userWeight"
                    } else {
                        DistanceUtil.convertKgToLbs(userWeight).toDouble().roundToInt()
                            .toString()
                    }

                    this.root.visible()
                }
            } else {
                binding.lytWeight.apply {
                    this.root.gone()
                }
            }


            if (data.isSelected) {
                binding.ivSelect.setImageResource(R.drawable.ic_google_fit_selected)
            } else {
                binding.ivSelect.setImageResource(R.drawable.ic_google_fit_default)
            }

            binding.ivSelect.setOnClickListener {
                data.isSelected = data.isSelected.not()
                notifyItemChanged(bindingAdapterPosition)
            }

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
                (holder as ViewHolderBodyMeasurements).bind(mDataSet[position])
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

    fun getSelectedData(): List<GoogleFitDataDisplayModel> {
        return mDataSet.filter { it.isSelected }
    }

    fun removeSyncedData(success: List<GoogleFitDataDisplayModel>) {
        success.forEach { data ->
            mDataSet.removeIf { it ->
                it.isDataSame(data)
            }
        }
        notifyDataSetChanged()
    }

    fun selectAll() {
        mDataSet.forEach {
            it.isSelected = true
        }
        notifyDataSetChanged()
    }
}