package com.noisefit.ui.dashboard.feature.alarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowAlarmToggleBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.models.AlarmAction
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.utils.DateFormats

class MyAlarmAdapter(val listener: AlarmRowAction) :
    RecyclerView.Adapter<MyAlarmAdapter.ViewHolder>() {
    private var deleteSet: Array<Int?>? = null
    private var mDataSet = ArrayList<AlarmsList.Alarm>()
    private var mEditMode = false
    private var disableDeleteMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }


    inner class ViewHolder(val binding: RowAlarmToggleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: AlarmsList.Alarm) {

            binding.tvAlarmTime.text = DateFormats.getAlarmDate(alarm.hour, alarm.minute)
            binding.sAlarmToggle.isChecked =
                mDataSet[bindingAdapterPosition].repeatDays?.get(0) ?: false


            binding.tvDays.text = ApplicationUtils.getRepeatDays(alarm.repeatDays)

            binding.root.setOnClickListener {
                listener.onItemClicked(bindingAdapterPosition, alarm)
            }

            binding.ivRemove.setOnClickListener {
                listener.onItemRemoved(bindingAdapterPosition, alarm)
            }

            binding.sAlarmToggle.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    mDataSet[bindingAdapterPosition].status = isChecked
                    (mDataSet[bindingAdapterPosition].repeatDays as ArrayList<Boolean>)[0] =
                        isChecked
                    listener.onSwitchClicked(isChecked, alarm)
                }
            }

            if (!disableDeleteMode) {
                if (mEditMode) {
                    binding.sAlarmToggle.gone()
                    binding.ivRemove.visible()
                } else {
                    binding.sAlarmToggle.visible()
                    binding.ivRemove.gone()
                }
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowAlarmToggleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun getCurrentDataSet(): List<AlarmsList.Alarm> {
        return mDataSet
    }

    fun removeItem(position: Int) {
        try {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        } catch (exp: ArrayIndexOutOfBoundsException) {
            exp.printStackTrace()
            //CASE : when Swap is in progress
        }

    }

    fun setDataSet(reminders: List<AlarmsList.Alarm>, disableDeleteMode: Boolean) {
        mDataSet = reminders as ArrayList<AlarmsList.Alarm>
        this.disableDeleteMode = disableDeleteMode
        deleteSet = arrayOfNulls(mDataSet.size)
        notifyDataSetChanged()
    }

    fun getSelectedCount(): Int {
        var selectedItems = 0
        if (deleteSet == null) return selectedItems
        for (item in deleteSet!!) {
            if (item != null) {
                selectedItems++
            }
        }
        return selectedItems
    }

    fun getRemainingAlarms(): List<AlarmsList.Alarm> {
        if (deleteSet == null) return mDataSet

        val dataSet = ArrayList<AlarmsList.Alarm>()
        deleteSet!!.forEachIndexed { index, i ->
            if (i == null) {
                dataSet.add(mDataSet[index])
            }
        }
        return dataSet
    }

    fun getAlarmsToDelete(): List<AlarmsList.Alarm> {
        if (deleteSet == null) return ArrayList()

        val dataSet = ArrayList<AlarmsList.Alarm>()
        deleteSet!!.forEachIndexed { index, i ->
            if (i == 1) {
                dataSet.add(mDataSet[index].apply {
                    this.status = false
                    this.alarmAction = AlarmAction.ALARM_DELETE
                })
            }
        }
        return dataSet
    }
}


interface AlarmRowAction {
    fun onSwitchClicked(state: Boolean, data: AlarmsList.Alarm)
    fun onItemClicked(position: Int, data: AlarmsList.Alarm)

    //    fun onLongPressed(position: Int)
//    fun onShortClicked(position: Int)
    fun onItemRemoved(position: Int, data: AlarmsList.Alarm)
}