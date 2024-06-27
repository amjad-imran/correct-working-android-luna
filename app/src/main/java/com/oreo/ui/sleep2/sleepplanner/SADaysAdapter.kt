package com.oreo.ui.sleep2.sleepplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemActiveDaysBinding
import com.noisefit.luna.databinding.ItemAlarmDaysBinding
import com.oreo.data.model.FHFlowIconsModel

class SADaysAdapter() :
    RecyclerView.Adapter<SADaysAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<String>()

    inner class ViewHolder(val binding: ItemAlarmDaysBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {

            binding.tvTitle.text=data

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemAlarmDaysBinding.inflate(
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


    fun setData(resultData: List<String>?) {
        mDataSet.clear()
        notifyDataSetChanged()
        if (resultData != null) {
            mDataSet.addAll(resultData)
        }
        notifyDataSetChanged()
    }



}



