package com.noisefit.ui.dashboard.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemRecentActivityLayoutBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ImageUtil
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.utils.DateFormats

class RecentWorkoutsAdapter : RecyclerView.Adapter<RecentWorkoutsAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SportsModeResponse>()
    private var onRecentWorkoutsInteractionListener: RecentWorkoutsInteractionListener? = null

    inner class ViewHolder(private val binding: ItemRecentActivityLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sportsModeResponse: SportsModeResponse, position: Int) {

            var hasData = true
            if (sportsModeResponse.activityType.equals("No data", true)) {
                binding.tvName.text = sportsModeResponse.activityType
                binding.tvDistance.text = "--"
                binding.tvDate.text = ""
                binding.container.alpha = .5f
                hasData = false
            } else {

                binding.container.alpha = 1f
                binding.tvDistance.text = sportsModeResponse.formattedData
                binding.tvDistanceUnit.text = sportsModeResponse.formattedDataUnit
                binding.tvDate.text =
                    DateFormats.formatActivityDate(sportsModeResponse.date) + " " + DateFormats.formatActivityTime(
                        sportsModeResponse.time
                    )
                binding.tvName.text = sportsModeResponse.getFormattedActivityName()
            }
            if (mDataSet.size - 1 == position) {
                binding.divider.root.gone()
            } else {
                binding.divider.root.visible()
            }
            val activityName = sportsModeResponse.type ?: sportsModeResponse.activityType

            binding.container.setOnClickListener {
                if (hasData) {
                    onRecentWorkoutsInteractionListener?.onWorkoutSelected(sportsModeResponse)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentActivityLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    fun setOnRecentWorkoutsInteractionListener(onRecentWorkoutsInteractionListener: RecentWorkoutsInteractionListener) {
        this.onRecentWorkoutsInteractionListener = onRecentWorkoutsInteractionListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }


    fun setDataSet(data: List<SportsModeResponse>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size

    interface RecentWorkoutsInteractionListener {
        fun onWorkoutSelected(sportsModeResponse: SportsModeResponse)
    }

}