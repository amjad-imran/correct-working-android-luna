package com.oreo.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowOreoAboutDeviceBinding
import com.noisefit_commans.common.copyToClipBoard
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class AboutDeviceAdapter : RecyclerView.Adapter<AboutDeviceAdapter.ViewHolder>() {

    var mDataSet = ArrayList<AboutDeviceData>()

    inner class ViewHolder(val binding: RowOreoAboutDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AboutDeviceData) {
            binding.tvTitle.text = data.title
            binding.tvValue.text = data.value

            val context = binding.root.context
            if (data.title.equals(context.getString(R.string.text_serial_number))){
                binding.ivCopy.visible()
            }else{
                binding.ivCopy.gone()
            }

            if ((bindingAdapterPosition + 1) == itemCount) {
                binding.divider.root.gone()
            } else {
                binding.divider.root.visible()
            }

            binding.ivCopy.setOnClickListener {
                data.value.copyToClipBoard(
                    context.getString(R.string.text_copied)
                )
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