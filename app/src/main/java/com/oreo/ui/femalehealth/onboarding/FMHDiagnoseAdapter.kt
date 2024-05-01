package com.oreo.ui.femalehealth.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.DiagnoseDataItem
import com.noisefit.luna.R
import com.noisefit.luna.databinding.LayoutFmhDiagnoseItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class FMHDiagnoseAdapter(val mListener: OnItemClickListener) :
    RecyclerView.Adapter<FMHDiagnoseAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<DiagnoseDataItem>()

    inner class ViewHolder(val binding: LayoutFmhDiagnoseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DiagnoseDataItem) {
            binding.tvHeader.text = data.title
            if (data.isChecked) {
                binding.ivChecked.visible()
                binding.container.setBackgroundResource(R.drawable.back_modal_new_fmh_selected)
            } else {
                binding.container.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                binding.ivChecked.gone()
            }
            binding.root.setOnClickListener {
                mListener.onItemClick(data, bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutFmhDiagnoseItemBinding.inflate(
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

    fun setData(resultData: List<DiagnoseDataItem>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(data: DiagnoseDataItem, position: Int)
    }

    fun updateItem(pos: Int, data: DiagnoseDataItem) {
        data.isChecked = !data.isChecked
        mDataSet[pos] = data
        notifyItemChanged(pos)
    }
}

