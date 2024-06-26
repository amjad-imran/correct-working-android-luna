package com.oreo.ui.internal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemHmInternalBinding
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
            binding.ivIcon.loadImage(binding.ivIcon.context, resultData.icon)
            binding.tvTitle.text = resultData.title
            if (resultData.value != null) {
                binding.lytWithHmValue.root.visible()
                binding.ivForward.gone()
                binding.lytWithHmValue.tvValue.text = resultData.value
                binding.lytWithHmValue.tvUnit.text = resultData.unit
                binding.lytWithHmValue.tvRangeValue.text = resultData.rangeValue
            } else {
                binding.lytWithHmValue.root.gone()
                binding.ivForward.visible()
            }
            binding.ivForward.setOnClickListener {
                listener.onItemClick(resultData, bindingAdapterPosition)
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

