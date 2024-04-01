package com.oreo.ui.workout.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowOwdItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OWDActivityData

class OWorkoutDetailslAdapterV2 : RecyclerView.Adapter<OWorkoutDetailslAdapterV2.ViewHolder>() {
    private var mDataSet = ArrayList<OWDActivityData>()

    inner class ViewHolder(val binding: RowOwdItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detailData: OWDActivityData) {

            binding.tvTitle.text = detailData.title
            binding.tvValue.text = detailData.value
            binding.tvUnitValue.text = detailData.unit
//            binding.
//            binding.tvUnitValue.setTextColor(binding..R.color.white_48)


            //Change code if workout params added
            if (itemCount > 2) {
                if ((itemCount % 2 == 0 && bindingAdapterPosition < itemCount - 2)
                    || (itemCount % 2 == 1 && bindingAdapterPosition < itemCount - 1)
                ) {
                    binding.viewDivider.root.visible()
                } else {
                    binding.viewDivider.root.invisible()
                }
            } else {
                binding.viewDivider.root.invisible()
            }

            binding.dividerVertical.gone()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OWorkoutDetailslAdapterV2.ViewHolder {
        val binding =
            RowOwdItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OWorkoutDetailslAdapterV2.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(data: List<OWDActivityData>) {
        mDataSet = data as ArrayList<OWDActivityData>
        notifyDataSetChanged()
    }
}