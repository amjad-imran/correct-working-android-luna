package com.noisefit.ui.dashboard.graphs.sleep

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.SleepExtraData
import com.noisefit.luna.databinding.ItemSleepDataLayoutBinding
import com.noisefit_commans.data.enums.SleepExtraType

class SleepExtraDataAdapter : RecyclerView.Adapter<SleepExtraDataAdapter.ViewHolder>() {

    private var sleepExtraInteractionListener: SleepExtraInteractionListener? = null
    private var mDataSet = ArrayList<SleepExtraData>()

    inner class ViewHolder(val binding: ItemSleepDataLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sleepExtraData: SleepExtraData, position: Int) {
            binding.tvTitle.text = sleepExtraData.title
            binding.tvDescription.text = sleepExtraData.description

            if (sleepExtraData.isSelected) {
                if(sleepExtraData.sleepExtraType== SleepExtraType.HeartRate){
                    binding.llContainerSecond.background = AppCompatResources.getDrawable(
                        binding.llContainerSecond.context,
                        R.drawable.rectangle_red_stroke
                    )
                }else{
                    binding.llContainerSecond.background = AppCompatResources.getDrawable(
                        binding.llContainerSecond.context,
                        R.drawable.rectangle_stress_stroke
                    )
                }

            } else {
                binding.llContainerSecond.background = AppCompatResources.getDrawable(
                    binding.llContainerSecond.context,
                    R.drawable.rectangle_black_stroke
                )
            }

            binding.container.setOnClickListener {
                val isSelected = !sleepExtraData.isSelected

                val type = if (isSelected) {
                    sleepExtraData.sleepExtraType
                } else {
                    SleepExtraType.NONE
                }


                sleepExtraInteractionListener?.onDetailClicked(
                    type,
                    position
                )
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSleepDataLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    fun setData(data: ArrayList<SleepExtraData>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    fun setData(overlayType: SleepExtraType, position: Int) {
        mDataSet[position].isSelected = overlayType != SleepExtraType.NONE
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setInterface(sleepExtraInteractionListener: SleepExtraInteractionListener) {
        this.sleepExtraInteractionListener = sleepExtraInteractionListener
    }

    fun reset() {
        mDataSet.forEach {
            it.isSelected = false
        }
        notifyDataSetChanged()
    }

    interface SleepExtraInteractionListener {
        fun onDetailClicked(overlayType: SleepExtraType, position: Int)
    }

}