package com.oreo.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowOreoAboutDeviceBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class AboutDeviceAdapter : RecyclerView.Adapter<AboutDeviceAdapter.ViewHolder>() {

    var mDataSet = ArrayList<AboutDeviceData>()

    inner class ViewHolder(val binding: RowOreoAboutDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AboutDeviceData) {
            binding.tvTitle.text = data.title
            binding.tvValue.text = data.value

            if ((bindingAdapterPosition + 1) == itemCount) {
                binding.divider.root.gone()
            } else {
                binding.divider.root.visible()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowOreoAboutDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: List<AboutDeviceData>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }


}

data class AboutDeviceData(
    val title: String,
    val value: String
)