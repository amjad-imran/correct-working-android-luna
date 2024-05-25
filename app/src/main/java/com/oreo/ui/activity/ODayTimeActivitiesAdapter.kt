package com.oreo.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemDaytimeActivitiesLayoutBinding
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ODayTimeActivitiesDataModel

class ODayTimeActivitiesAdapter(val mListener: DayTimeActivitiesInteractionListener) :
    RecyclerView.Adapter<ODayTimeActivitiesAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<ODayTimeActivitiesDataModel>()

    inner class ViewHolder(private val binding: ItemDaytimeActivitiesLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ODayTimeActivitiesDataModel) {
            if (data.type?.equals("workout", true) == true) {
                binding.tvName.setTextColor(
                    ContextCompat.getColor(
                        binding.tvName.context,
                        R.color.oreo_activity_bar_color
                    )
                )
                binding.imageView8.loadImage(binding.imageView8.context, data.workoutData?.iconUrl)

                val formattedTime =
                    "${DateFormats.convert24HourTo12(data.workoutData?.startTime).lowercase()} - ${
                        DateFormats.convert24HourTo12(data.workoutData?.endTime).lowercase()
                    }"
                binding.tvActivityTime.text = formattedTime
                binding.tvName.text = data.workoutData?.getFormattedActivityName()

            } else if (data.type?.equals("sleep", true) == true) {
                binding.tvName.setTextColor(
                    ContextCompat.getColor(
                        binding.tvName.context,
                        R.color.oreo_sleep_bar_color
                    )
                )
                binding.imageView8.setImageResource(R.drawable.ic_daytime_nap)

                val formattedTime = "${
                    DateFormats.formatDate(
                        data.startTime,
                        DateFormats.dateTimeFormat5(),
                        DateFormats.time12Meridian()
                    ).lowercase()
                } - ${
                    DateFormats.formatDate(
                        data.endTime,
                        DateFormats.dateTimeFormat5(),
                        DateFormats.time12Meridian()
                    ).lowercase()
                }"
                binding.tvActivityTime.text = formattedTime
                binding.tvName.text = data.type


            } else if (data.type?.equals("nap", true) == true) {
                binding.tvName.setTextColor(
                    ContextCompat.getColor(
                        binding.tvName.context,
                        R.color.oreo_sleep_bar_color
                    )
                )
                binding.imageView8.setImageResource(R.drawable.ic_daytime_nap)


                val formattedTime = "${
                    DateFormats.formatDate(
                        data.startTime,
                        DateFormats.dateTimeFormat5(),
                        DateFormats.time12Meridian()
                    ).lowercase()
                } - ${
                    DateFormats.formatDate(
                        data.endTime,
                        DateFormats.dateTimeFormat5(),
                        DateFormats.time12Meridian()
                    ).lowercase()
                }"
                binding.tvActivityTime.text = formattedTime
                binding.tvName.text = data.type

            }

            binding.root.setOnClickListener {
                mListener.onActivitiesSelected(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDaytimeActivitiesLayoutBinding.inflate(
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

    fun setDataSet(data: ArrayList<ODayTimeActivitiesDataModel>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    interface DayTimeActivitiesInteractionListener {
        fun onActivitiesSelected(data: ODayTimeActivitiesDataModel)
    }
}