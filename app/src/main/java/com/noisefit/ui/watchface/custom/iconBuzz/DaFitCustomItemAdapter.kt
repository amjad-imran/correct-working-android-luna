package com.noisefit.ui.watchface.custom.iconBuzz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.CustomDaFitIcons
import com.noisefit.luna.databinding.ItemDaFitCustomLayoutBinding


class DaFitCustomItemAdapter(val listener: DaFitCustomItemInteractionListener) :
    RecyclerView.Adapter<DaFitCustomItemAdapter.ViewHolder>() {

    val mDataSet = ArrayList<CustomDaFitIcons>()
    var selectedPosition = 0

    inner class ViewHolder(val binding: ItemDaFitCustomLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(customDaFitIcons: CustomDaFitIcons) {

            binding.tvSubtitle.text = customDaFitIcons.subTitle
            binding.tvTitle.text = customDaFitIcons.title

            binding.container.setOnClickListener {
                listener.onWidgetSelected(customDaFitIcons,bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDaFitCustomLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(widgetsList: List<CustomDaFitIcons>) {

        mDataSet.clear()
        mDataSet.addAll(widgetsList)
        notifyDataSetChanged()
    }

    fun setData(subTitle: String,position: Int) {

        mDataSet[position].subTitle = subTitle
        notifyItemChanged(position)
    }


}

interface DaFitCustomItemInteractionListener {
    fun onWidgetSelected(customDaFitIcons: CustomDaFitIcons,position: Int)
}