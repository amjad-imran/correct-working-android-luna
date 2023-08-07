package com.oreo.ui.helpsupport.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoHsParentItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OHSModel

class OHealthSupportAdapter : RecyclerView.Adapter<OHealthSupportAdapter.ViewHolder>() {
    val mDataSet = ArrayList<OHSModel>()

    inner class ViewHolder(val binding: OreoHsParentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: OHSModel) {
            binding.tvTitle.text = resultData.title

            if (bindingAdapterPosition == mDataSet.size - 1)
                binding.divider.root.gone() else
                binding.divider.root.visible()

        }
    }

    interface OHSClickListener {
        fun onItemClickListener(id: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoHsParentItemBinding.inflate(
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

    fun setData(listData: ArrayList<OHSModel>) {
        mDataSet.clear()
        mDataSet.addAll(listData)
        notifyDataSetChanged()
    }
}