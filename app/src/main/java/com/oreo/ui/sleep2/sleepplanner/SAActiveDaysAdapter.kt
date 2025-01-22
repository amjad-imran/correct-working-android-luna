package com.oreo.ui.sleep2.sleepplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemActiveDaysBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.visible

class SAActiveDaysAdapter(val mListener: OnActiveDayItemClick) :
    RecyclerView.Adapter<SAActiveDaysAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SAActiveDayDataModel>()

    inner class ViewHolder(val binding: ItemActiveDaysBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SAActiveDayDataModel) {
            if (data.isSelected && data.isPreSelected.not()) {
                binding.ivItem.setBackgroundResource(R.drawable.circle_select_sa)
            } else {
                binding.ivItem.setBackgroundResource(R.drawable.circle_unselect_sa)
            }
            binding.ivSelectedDot.setVisibilityByCondition(data.isPreSelected)

            binding.tvHour.text = data.name
            binding.ivItem.setOnClickListener {
                mListener.onItemClick(data, bindingAdapterPosition)
                notifyDataSetChanged()
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemActiveDaysBinding.inflate(
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


    fun setData(resultData: List<SAActiveDayDataModel>?) {
        mDataSet.clear()
        notifyDataSetChanged()
        if (resultData != null) {
            mDataSet.addAll(resultData)
        }
        notifyDataSetChanged()
    }

    fun updateItem(position: Int) {
        try {
            val data = mDataSet[position]

            data.isSelected = data.isSelected.not()
            data.isPreSelected = false

            notifyItemChanged(position)
        } catch (exp: Exception) {
            //Out of bound exception
        }
    }

    fun getSelectedValue(): ArrayList<String> {
        val listData = ArrayList<String>()
        mDataSet.forEach {
            if (it.isSelected) {
                listData.add(it.name)
            }
        }
        return listData
    }


}

interface OnActiveDayItemClick {
    fun onItemClick(data: SAActiveDayDataModel, position: Int)
}

