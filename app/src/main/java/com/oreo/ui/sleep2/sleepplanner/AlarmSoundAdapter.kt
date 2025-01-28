package com.oreo.ui.sleep2.sleepplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.AlarmSoundDataModel
import com.noisefit.data.model.SAGoalDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemAlarmSoundBinding
import com.noisefit.luna.databinding.ItemSaGoalViewBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class AlarmSoundAdapter(val listener: OnSoundItemClick) :
    RecyclerView.Adapter<AlarmSoundAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<AlarmSoundDataModel>()
    private var selectedKey = 1

    inner class ViewHolder(val binding: ItemAlarmSoundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AlarmSoundDataModel) {

            if (data.key == selectedKey)
                binding.ivChecked.setImageResource(R.drawable.ic_goal_selected_rb)
            else
                binding.ivChecked.setImageResource(R.drawable.ic_goal_unselect_rb)

            binding.tvTitle.text = data.title

            binding.root.setOnClickListener {
                selectedKey = data.key
                notifyDataSetChanged()

                listener.onItemClick(data, bindingAdapterPosition)
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemAlarmSoundBinding.inflate(
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


    fun setData(resultData: List<AlarmSoundDataModel>,selectedKey:Int) {
        this.selectedKey = selectedKey
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }
}

interface OnSoundItemClick {
    fun onItemClick(data: AlarmSoundDataModel, position: Int)
}



