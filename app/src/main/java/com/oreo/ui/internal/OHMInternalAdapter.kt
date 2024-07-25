package com.oreo.ui.internal

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemHmInternalBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OHMDataModel

class OHMInternalAdapter(val listener: HMItemClickListener) :
    RecyclerView.Adapter<OHMInternalAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OHMDataModel>()

    inner class ViewHolder(val binding: ItemHmInternalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: OHMDataModel) {
            binding.ivIcon.loadImage(binding.ivIcon.context, resultData.type.icon)
            binding.tvTitle.text = resultData.type.displayName
            if (resultData.value != null || resultData.valueTime != null) {
                binding.ivForward.gone()
                binding.lytRightValues.root.visible()

                if (resultData.valueTime != null) {
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                        resultData.valueTime ?: 0
                    )
                    if (hour == 0) {
                        binding.lytRightValues.lytHourMin.apply {
                            this.tvHour.gone()
                            this.textHour.gone()
                            this.tvMin.text = "$minute"
                            this.textMin.text = this.textMin.context.getString(R.string.text_min)
                        }
                    } else {
                        binding.lytRightValues.lytHourMin.apply {
                            this.tvHour.visible()
                            this.textHour.visible()
                            this.tvHour.text = "$hour"
                            this.textHour.text = this.textHour.context.getString(R.string.text_hr)
                            this.tvMin.text = "$minute"
                            this.textMin.text = this.textMin.context.getString(R.string.text_min)
                        }
                    }
                } else {
                    binding.lytRightValues.lytHourMin.apply {
                        this.tvHour.gone()
                        this.textHour.gone()
                        this.tvMin.text = "${resultData.value}"
                        this.textMin.text = "${resultData.unit}"
                    }
                }

                binding.lytRightValues.apply {
                    this.tvRangeValue.text = resultData.text
                    val (back, textColor) = getColorByStatus(resultData.status)
                    this.tvRangeValue.setTextColor(textColor)
                    this.viewTextBack.setBackgroundResource(back)

                    if(resultData.status.equals("optimal")){
                        this.tvRangeValue.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hm_tick, 0, 0, 0);
                    }else{
                        this.tvRangeValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    }
                }
            } else {
                binding.lytRightValues.root.gone()
                binding.ivForward.visible()
            }



            binding.ivForward.setOnClickListener {
                listener.onItemClick(resultData, bindingAdapterPosition)
            }
        }

        private fun getColorByStatus(status: String?): Pair<Int, Int> {
            if (status == null) return Pair(0, R.color.white)

            return when (status.lowercase()) {
                "warning" -> Pair(R.drawable.back_hm_warning, Color.parseColor("#ff7c94"))
                "optimal" -> Pair(R.drawable.back_hm_optimal, Color.parseColor("#29cc74"))
                "fair" -> Pair(R.drawable.back_hm_fair, Color.parseColor("#d79d58"))
                else -> Pair(0, R.color.white)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemHmInternalBinding.inflate(
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

    fun setData(resultData: List<OHMDataModel>) {

        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

    interface HMItemClickListener {
        fun onItemClick(resultData: OHMDataModel, position: Int)
    }
}

