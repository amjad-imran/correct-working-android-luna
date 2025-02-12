package com.oreo.ui.sleep2.sleepplanner.planner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.SAGoalDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemSaGoalViewBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class SAGoalAdapter(val listener: OnGoalItemClick) :
    RecyclerView.Adapter<SAGoalAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SAGoalDataModel>()

    inner class ViewHolder(val binding: ItemSaGoalViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SAGoalDataModel) {

            if (data.isChecked)
                binding.ivChecked.setImageResource(R.drawable.ic_goal_selected_rb)
            else
                binding.ivChecked.setImageResource(R.drawable.ic_goal_unselect_rb)
            binding.tvTitle.text = data.title

            binding.root.setOnClickListener {
                if(data.isChecked) return@setOnClickListener

                updateItem(bindingAdapterPosition)
                listener.onItemClick(data, bindingAdapterPosition)
            }
            if (bindingAdapterPosition == mDataSet.size - 1) {
                binding.include6.root.gone()
            } else
                binding.include6.root.visible()

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemSaGoalViewBinding.inflate(
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


    fun setData(resultData: List<SAGoalDataModel>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

    fun updateItem(position: Int) {
        mDataSet.forEachIndexed { index, saGoalData ->
            if (index == position) {
                saGoalData.isChecked = saGoalData.isChecked.not()
            } else {
                saGoalData.isChecked = false
            }
        }
        notifyDataSetChanged()
    }

    fun selectByKey(key: String) :Boolean{
        val index = mDataSet.indexOfFirst { it.key.equals(key, true) }
        if (index != -1) {
            updateItem(index)
            return true
        }
        return false
    }

}

interface OnGoalItemClick {
    fun onItemClick(data: SAGoalDataModel, position: Int)
}



