package com.oreo.ui.internal

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemRvDropDownBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState

class ODropDownAdapter(val listener: ODDItemClickListener) :
    RecyclerView.Adapter<ODropDownAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SleepInternalLaunchState>()
    private var selectedLaunchMode: SleepInternalLaunchState? = null

    inner class ViewHolder(val binding: ItemRvDropDownBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: SleepInternalLaunchState) {

            val (title, icon) = getTitle(binding.tvTitle.context, resultData)
            binding.ivIcon.loadImage(binding.ivIcon.context, icon)
            binding.tvTitle.text = title
            if (selectedLaunchMode == resultData) {
                binding.lytMain.setBackgroundResource(R.drawable.back_selected_drop_down)
            } else {
                binding.lytMain.setBackgroundResource(R.drawable.back_non_selected_drop_down)
            }

            binding.root.setOnClickListener {
                listener.onItemClick(resultData, bindingAdapterPosition)
                selectedLaunchMode = resultData
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemRvDropDownBinding.inflate(
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

    fun setData(
        resultData: List<SleepInternalLaunchState>,
        selectedLaunchMode: SleepInternalLaunchState
    ) {
        this.selectedLaunchMode = selectedLaunchMode
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

    interface ODDItemClickListener {
        fun onItemClick(resultData: SleepInternalLaunchState, position: Int)
    }

    fun getTitle(
        context: Context,
        selectedLaunchMode: SleepInternalLaunchState
    ): Pair<String, Int> {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                Pair(
                    context.getString(R.string.text_restorative_sleep),
                    R.drawable.ic_sleep_restroactive_sp
                )
            }

            SleepInternalLaunchState.SLEEP_TIME -> Pair(
                context.getString(R.string.text_sleep_time),
                R.drawable.ic_sleep_time
            )

            SleepInternalLaunchState.HOUR_VS_NEED -> Pair(
                context.getString(R.string.text_hour_vs_need),
                R.drawable.ic_sleep_snooz
            )

            SleepInternalLaunchState.SLEEP_PERFORMANCE -> Pair(
                context.getString(R.string.text_sleep_performance),
                R.drawable.ic_sleep_performance
            )

            SleepInternalLaunchState.EFFICIENCY -> Pair(
                context.getString(R.string.text_efficiency),
                R.drawable.ic_sleep_efficiency
            )

            SleepInternalLaunchState.REM_SLEEP -> Pair(
                context.getString(R.string.text_rem_sleep),
                R.drawable.ic_sleep_rem_sp
            )

            SleepInternalLaunchState.DEEP_SLEEP -> Pair(
                context.getString(R.string.text_deep_sleep),
                R.drawable.ic_sleep_deep_sp
            )

            SleepInternalLaunchState.LATENCY -> Pair(
                context.getString(R.string.text_latency),
                R.drawable.ic_sleep_latency
            )

            SleepInternalLaunchState.RESTFULNESS -> Pair(
                context.getString(R.string.text_restfulness),
                R.drawable.ic_sleep_restfulness
            )

            SleepInternalLaunchState.SLEEP_DURATION -> Pair(
                context.getString(R.string.text_sleep_duration),
                R.drawable.ic_clock_off_sleep
            )

            SleepInternalLaunchState.TIMING -> Pair(
                context.getString(R.string.text_timing),
                R.drawable.ic_clock_off_sleep
            )

            SleepInternalLaunchState.RESPIRATORY_RATE -> Pair(
                context.getString(R.string.text_respiratory_rate),
                R.drawable.ic_respiratory_rate
            )

            SleepInternalLaunchState.RESTING_HEART_RATE -> Pair(
                context.getString(R.string.text_resting_heart_rate),
                R.drawable.ic_resting_hr
            )

            SleepInternalLaunchState.HRV -> Pair(
                context.getString(R.string.text_hrv),
                R.drawable.ic_hrv
            )

            SleepInternalLaunchState.SKIN_TEMPERATURE -> Pair(
                context.getString(R.string.text_skin_temperature),
                R.drawable.ic_skin_tempreature
            )

            SleepInternalLaunchState.BLOOD_OXYGEN -> Pair(
                context.getString(R.string.text_blood_oxygen),
                R.drawable.ic_blood_oxygen
            )
        }
    }
}

