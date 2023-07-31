package com.noisefit.ui.dashboard.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.DashNotification
import com.noisefit.luna.databinding.RowDashAlertsBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class AlertAdapter :
    RecyclerView.Adapter<AlertAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<DashNotification>()
    private var mListener: AlertActions? = null

    inner class ViewHolder(val binding: RowDashAlertsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DashNotification) {
            binding.tvAlertMessage.text = data.message
            binding.root.setOnClickListener {
                mListener?.onAlertClicked(data, bindingAdapterPosition)
            }

            if (data.hideClose) {
                binding.ivCloseAlert.gone()
            } else {
                binding.ivCloseAlert.visible()
            }
            binding.ivCloseAlert.setOnClickListener {
                mListener?.onCloseClicked(data, bindingAdapterPosition)
            }


            if (itemCount == 1) {
                if(data.icon!=0){
                    binding.ivAlertImage.setImageResource(data.icon)
                }else{
                    binding.ivAlertImage.setImageResource(R.drawable.ic_alert_setup)
                }
                binding.divider.gone()
                binding.ivAlertImage.visible()
            } else {
                if(data.icon!=0){
                    binding.ivAlertImage.setImageResource(data.icon)
                    binding.ivAlertImage.visible()
                }else{
                    binding.divider.visible()
                    binding.ivAlertImage.gone()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowDashAlertsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(alerts: List<DashNotification>) {
        mDataSet.clear()
        mDataSet.addAll(alerts)
        notifyDataSetChanged()
    }

    fun setListeners(listener: AlertActions) {
        mListener = listener
    }

    fun removeItem(position: Int) {
        mDataSet.removeAt(position)
        notifyItemRemoved(position)
    }
}

interface AlertActions {
    fun onCloseClicked(data: DashNotification, position: Int)
    fun onAlertClicked(data: DashNotification, position: Int)
}