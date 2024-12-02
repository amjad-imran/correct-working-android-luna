package com.oreo.ui.stress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemStressActivitiesLayoutBinding
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OStressActivitiesDataModel

class StressActivitiesAdapter(val mListener: StressActivitiesInteractionListener) :
    RecyclerView.Adapter<StressActivitiesAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<OStressActivitiesDataModel>()

    inner class ViewHolder(private val binding: ItemStressActivitiesLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: OStressActivitiesDataModel) {
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
                binding.tvName.text = data.workoutData?.getTranslatedActivityName()

            } else if (data.type?.equals("sleep", true) == true) {
                binding.tvName.setTextColor(
                    ContextCompat.getColor(
                        binding.tvName.context,
                        R.color.oreo_sleep_bar_color
                    )
                )
                binding.imageView8.setImageResource(R.drawable.ic_stress_nap)

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
                binding.tvName.text = binding.root.context.getString(R.string.text_sleep)/*data.type*/


            } else if (data.type?.equals("nap", true) == true) {
                binding.tvName.setTextColor(
                    ContextCompat.getColor(
                        binding.tvName.context,
                        R.color.oreo_sleep_bar_color
                    )
                )
                binding.imageView8.setImageResource(R.drawable.ic_stress_nap)


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
                binding.tvName.text = binding.root.context.getString(R.string.text_nap)/*data.type*/

            }

            binding.root.setOnClickListener {
                mListener.onActivitiesSelected(data)
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
        fun onActivitiesSelected(data: OStressActivitiesDataModel)
    }
}