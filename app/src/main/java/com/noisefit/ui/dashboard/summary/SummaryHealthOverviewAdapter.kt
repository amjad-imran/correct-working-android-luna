package com.noisefit.ui.dashboard.summary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.noisefit.R
import com.noisefit_commans.data.model.HealthOverview
import com.noisefit.databinding.*
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils

const val HealthOverviewEmpty = "--"


class SummaryHealthOverviewAdapter :
    RecyclerView.Adapter<HomeRecyclerViewHolder>() {

    var devicePaired = false
    var lastPosition = -1
    var refreshPosition: Int? = null

    var items = listOf<HealthOverview>()
        set(value) {
            field = value
//            if(refreshPosition != null && refreshPosition != -1){
//                notifyItemChanged(refreshPosition!!)
//            }else{
//                notifyDataSetChanged()
//            }
            notifyDataSetChanged()

        }

    var itemClickListener: ((view: View, item: HealthOverview, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        return when (viewType) {
            R.layout.layout_steps_count_header -> HomeRecyclerViewHolder.StepsViewHolder(
                LayoutStepsCountHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.layout_blood_oxygen_header -> HomeRecyclerViewHolder.BloodOxygenViewHolder(
                LayoutBloodOxygenHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.layout_heart_rate_header -> HomeRecyclerViewHolder.HeartRateViewHolder(
                LayoutHeartRateHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.layout_sleep_header -> HomeRecyclerViewHolder.SleepViewHolder(
                LayoutSleepHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.layout_distance_header -> HomeRecyclerViewHolder.DistanceViewHolder(
                LayoutDistanceHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.layout_stress_header -> HomeRecyclerViewHolder.StressViewHolder(
                LayoutStressHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.layout_temp_header -> HomeRecyclerViewHolder.BodyTempViewHolder(
                LayoutTempHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.layout_calories_header -> HomeRecyclerViewHolder.CaloriesViewHolder(
                LayoutCaloriesHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        holder.itemClickListener = itemClickListener
        when (holder) {
            is HomeRecyclerViewHolder.HeartRateViewHolder -> holder.bind(
                items[position] as HealthOverview.HeartRate,
                position,
                lastPosition,
                devicePaired
            )
            is HomeRecyclerViewHolder.BloodOxygenViewHolder -> holder.bind(
                items[position] as HealthOverview.BloodOxygen,
                position,
                lastPosition,
                devicePaired
            )
            is HomeRecyclerViewHolder.StepsViewHolder -> holder.bind(
                items[position] as HealthOverview.Steps,
                position,
                lastPosition,
                devicePaired
            )
            is HomeRecyclerViewHolder.StressViewHolder -> holder.bind(
                items[position] as HealthOverview.Stress,
                position,
                lastPosition,
                devicePaired
            )
            is HomeRecyclerViewHolder.DistanceViewHolder -> holder.bind(
                items[position] as HealthOverview.Distance,
                position,
                lastPosition,
                devicePaired
            )
            is HomeRecyclerViewHolder.SleepViewHolder -> holder.bind(
                items[position] as HealthOverview.Sleep,
                position,
                lastPosition,
                devicePaired
            )
            is HomeRecyclerViewHolder.BodyTempViewHolder -> holder.bind(
                items[position] as HealthOverview.BodyTemp,
                position,
                lastPosition,
                devicePaired
            )
            is HomeRecyclerViewHolder.CaloriesViewHolder -> holder.bind(
                items[position] as HealthOverview.Calories,
                position,
                lastPosition,
                devicePaired
            )
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HealthOverview.Steps -> R.layout.layout_steps_count_header
            is HealthOverview.HeartRate -> R.layout.layout_heart_rate_header
            is HealthOverview.BloodOxygen -> R.layout.layout_blood_oxygen_header
            is HealthOverview.Stress -> R.layout.layout_stress_header
            is HealthOverview.Distance -> R.layout.layout_distance_header
            is HealthOverview.Sleep -> R.layout.layout_sleep_header
            is HealthOverview.BodyTemp -> R.layout.layout_temp_header
            is HealthOverview.Calories -> R.layout.layout_calories_header
        }
    }
}


sealed class HomeRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((view: View, item: HealthOverview, position: Int) -> Unit)? = null

    class StepsViewHolder(private val binding: LayoutStepsCountHeaderBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            steps: HealthOverview.Steps,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {

            if (HealthOverviewEmpty.equals(steps.value, true)) {
                //binding.container.alpha = HideAlpha

                binding.tvNoData.visible()
//                if (devicePaired) {
//                    binding.tvNoData.visible()
//                } else {
//                    binding.tvNoData.gone()
//                }
                binding.tvSteps.text = ""

            } else {
//                val progress = "${steps.completed.upToNDecimal(2)}% of ${steps.goal}"
//                binding.tvGoals.text = progress
                binding.tvNoData.gone()
                //binding.container.alpha = ShowAlpha
                binding.tvSteps.text = steps.value
                binding.timeAgo.text = steps.timeAgo
            }
//            binding.layouProgress.pbSteps.progress = steps.completed.roundToInt()
            //val progress = "${steps.completed.upToNDecimal(2)}% of ${steps.goal}"

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, steps, position)
            }
        }
    }


    class CaloriesViewHolder(private val binding: LayoutCaloriesHeaderBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            calories: HealthOverview.Calories,
            position: Int,
            lastPosition: Int,
            devicePaired: Boolean
        ) {

            if (HealthOverviewEmpty.equals(calories.value, true)) {
                //binding.container.alpha = HideAlpha

                binding.tvNoData.visible()
//                if (devicePaired) {
//                    binding.tvNoData.visible()
//                } else {
//                    binding.tvNoData.gone()
//                }
                binding.tvValue.text = ""
            } else {

                binding.tvTime.text = calories.timeAgo
                binding.tvNoData.gone()
                //binding.container.alpha = ShowAlpha
                binding.tvValue.text = calories.value
            }
//            binding.layouProgress.pbSteps.progress = steps.completed.roundToInt()
            //val progress = "${steps.completed.upToNDecimal(2)}% of ${steps.goal}"

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, calories, position)
            }
        }
    }

    class BloodOxygenViewHolder(private val binding: LayoutBloodOxygenHeaderBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            bloodOxygen: HealthOverview.BloodOxygen, position: Int, lastPosition: Int,
            devicePaired: Boolean
        ) {

            if (HealthOverviewEmpty.equals(bloodOxygen.value, true)) {
                //binding.container.alpha = HideAlpha
                binding.tvNoData.visible()
                binding.tvValue.text = ""
                binding.tvAvg.text = ""
            } else {
                binding.tvNoData.gone()
                binding.tvAvg.text = bloodOxygen.avg
                //binding.container.alpha = ShowAlpha
                binding.tvValue.text = bloodOxygen.value
                binding.tvTime.text = bloodOxygen.timeAgo
            }
//            binding.layouProgress.pbSteps.apply {
//                progress = bloodOxygen.valueInInt
//            }



            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, bloodOxygen, position)
            }
        }
    }

    class HeartRateViewHolder(private val binding: LayoutHeartRateHeaderBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            heartRate: HealthOverview.HeartRate, position: Int, lastPosition: Int,
            devicePaired: Boolean
        ) {

            if (HealthOverviewEmpty.equals(heartRate.value, true)) {
                //binding.container.alpha = HideAlpha
                binding.tvNoData.visible()
                binding.tvValue.text = ""
                binding.tvAvg.text = ""
                binding.textUnit.text = ""
            } else {
                binding.tvNoData.gone()
                //binding.container.alpha = ShowAlpha
                binding.tvValue.text = heartRate.value
                binding.textUnit.text = "bpm"
                binding.tvTime.text = heartRate.timeAgo
                binding.tvAvg.text = heartRate.avg
            }
//            HeartChartUtils.setSmallHrChart(binding.lineChart)
//            HeartChartUtils.setDayHrChartData(
//                heartRate.values,
//                binding.lineChart,
//                false
//            )

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, heartRate, position)
            }
        }
    }

    class DistanceViewHolder(private val binding: LayoutDistanceHeaderBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            distance: HealthOverview.Distance, position: Int, lastPosition: Int,
            devicePaired: Boolean
        ) {

            if (HealthOverviewEmpty.equals(distance.value, true)) {
                //binding.container.alpha = HideAlpha
                binding.tvNoData.visible()
                binding.tvValue.text = ""
                binding.textUnit.text = ""
            } else {
                binding.tvNoData.gone()
                //binding.container.alpha = ShowAlpha
                binding.tvValue.text = distance.value
                binding.textUnit.text = distance.distanceUnit
                binding.tvTime.text = distance.timeAgo
            }
//            DistanceBarChartUtils.setSmallChart(binding.barChart)
//            DistanceBarChartUtils.setSmallChartData(distance.values,binding.barChart)

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, distance, position)
            }
        }
    }

    class SleepViewHolder(private val binding: LayoutSleepHeaderBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            sleep: HealthOverview.Sleep, position: Int, lastPosition: Int,
            devicePaired: Boolean
        ) {

            if (sleep.duration == -1) {
                binding.tvSleepHour.text = ""
                binding.tvSleepMinute.text = ""
                binding.textHour.gone()
                binding.textMins.gone()
                //binding.container.alpha = HideAlpha
                binding.tvNoData.visible()
                binding.tvAvg.text = ""
            } else {
                binding.textHour.visible()
                binding.textMins.visible()
                binding.tvAvg.text = sleep.sleepScore
                val formattedDuration = ApplicationUtils.getFormattedSleepDuration(sleep.duration)
                binding.tvSleepHour.text = formattedDuration.first.toString()
                binding.tvSleepMinute.text = formattedDuration.second.toString()
                binding.tvNoData.gone()
                binding.tvTime.text = sleep.date
                //binding.container.alpha = ShowAlpha
            }
            //binding.divider.root.gone()

//            val sleepDayGraphView = SleepGraphViewSmall(NoiseFitApplicationMain.context!!)
//            binding.flSleepGraph.addView(sleepDayGraphView)
//            sleepDayGraphView.init()
//            sleepDayGraphView.setData(sleep.countCardData)
//            sleepDayGraphView.setData(sleep.sleepArray)


            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, sleep, position)
            }
        }
    }

    class StressViewHolder(private val binding: LayoutStressHeaderBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            stress: HealthOverview.Stress, position: Int, lastPosition: Int,
            devicePaired: Boolean
        ) {


            if (HealthOverviewEmpty.equals(stress.value, true)) {
                //binding.container.alpha = HideAlpha
                binding.tvNoData.visible()
                binding.tvValue.text = ""
                binding.tvAvg.text = ""
                binding.textUnit.text = ""
            } else {
                binding.tvNoData.gone()
                binding.tvAvg.text = stress.avg
                //binding.container.alpha = ShowAlpha
                binding.tvValue.text = stress.value
                binding.textUnit.text = stress.type
            }
//            StressBarChartUtils.setChart(binding.barChart)
//            StressBarChartUtils.setChartData(stress.values,binding.barChart,stress.colors)
            binding.tvTime.text = stress.timeAgo
            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, stress, position)
            }
        }
    }

    class BodyTempViewHolder(private val binding: LayoutTempHeaderBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            bodyTemp: HealthOverview.BodyTemp, position: Int, lastPosition: Int,
            devicePaired: Boolean
        ) {


            if (HealthOverviewEmpty.equals(bodyTemp.value, true)) {
                //binding.container.alpha = HideAlpha
                binding.tvNoData.visible()
                binding.tvValue.text = ""
                binding.tvAvg.text = ""
            } else {
                binding.tvNoData.gone()
                binding.tvAvg.text = bodyTemp.avg

                //binding.container.alpha = ShowAlpha
                binding.tvValue.text = bodyTemp.value
            }
//            BodyTempChartUtils.setChart(binding.barChart)
//            BodyTempChartUtils.setChartData(bodyTemp.values,binding.barChart,bodyTemp.colors)
            binding.tvTime.text = bodyTemp.timeAgo
            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, bodyTemp, position)
            }
        }
    }
}



