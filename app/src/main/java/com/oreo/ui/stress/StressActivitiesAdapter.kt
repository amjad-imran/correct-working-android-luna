package com.oreo.ui.stress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemStressActivitiesLayoutBinding
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OStressActivitiesDataModel

class StressActivitiesAdapter(val mListener: StressActivitiesInteractionListener) :
    RecyclerView.Adapter<StressActivitiesAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<OStressActivitiesDataModel>()

    inner class ViewHolder(private val binding: ItemStressActivitiesLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: OStressActivitiesDataModel) {
            if (data.type?.lowercase() == "workout") {
                binding.tvName.setTextColor(
                    ContextCompat.getColor(
                        binding.tvName.context,
                        R.color.oreo_activity_bar_color
                    )
                )
                binding.imageView8.setImageResource(R.drawable.ic_walking_lb)
            } else if (data.type?.lowercase() == "sleep") {
                binding.tvName.setTextColor(
                    ContextCompat.getColor(
                        binding.tvName.context,
                        R.color.oreo_sleep_bar_color
                    )
                )
                binding.imageView8.setImageResource(R.drawable.ic_stress_nap)
            }
            binding.tvName.text = data.type
            val formattedTime = "${
                DateFormats.formatDate(
                    data.startTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.time12Meridian
                )
            } - ${
                DateFormats.formatDate(
                    data.endTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.time12Meridian
                )
            }"
            binding.tvActivityTime.text = formattedTime

            binding.ivMore.setOnClickListener {
                data.id?.let { it1 -> mListener.onActivitiesSelected(it1) }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStressActivitiesLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(data: ArrayList<OStressActivitiesDataModel>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    interface StressActivitiesInteractionListener {
        fun onActivitiesSelected(id: String)
    }
}