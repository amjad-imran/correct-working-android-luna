package com.noisefit.ui.dashboard.healthOverview.edit

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.HealthOverViewList
import com.noisefit.luna.databinding.ItemEditDashboardBinding
import com.noisefit_commans.ui.loadImage

class EditDashBoardAdapter(
    val contactInteractionListener: EditDashBoardInteractionListener
) : RecyclerView.Adapter<EditDashBoardAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<HealthOverViewList>()

    inner class ViewHolder(val binding: ItemEditDashboardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detailData: HealthOverViewList, position: Int) {

            binding.title.text = detailData.title
            binding.cb.isChecked = detailData.visible
            binding.imv.loadImage(binding.imv.context, detailData.icon)

            binding.cb.setOnClickListener { view ->
                val isChecked = (view as CompoundButton).isChecked
                contactInteractionListener.onEditDashboardClick(
                    detailData,
                    isChecked,
                    position,
                    binding.cb
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEditDashboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(data: List<HealthOverViewList>) {
        mDataSet = data as ArrayList<HealthOverViewList>
        notifyDataSetChanged()
    }

    fun updateDashboard(selected: Boolean, position: Int) {
        mDataSet[position].visible = selected
        notifyItemChanged(position)
    }

    interface EditDashBoardInteractionListener {
        fun onEditDashboardClick(
            healthOverViewList: HealthOverViewList,
            isChecked: Boolean,
            position: Int,
            checkBox: CheckBox
        )
    }

}