package com.oreo.ui.home.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowNapDashBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.health.Nap

class DashNapAdapter(private val napList: List<Nap>) :
    RecyclerView.Adapter<DashNapAdapter.ViewHolder>() {
    var listener: OnNapSelectedAction? = null

    inner class ViewHolder(val binding: RowNapDashBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(nap: Nap) {

            if (bindingAdapterPosition == (napList.size - 1)) {
                binding.divider.root.gone()
            } else {
                binding.divider.root.visible()
            }
            binding.root.setOnClickListener {
                listener?.onNapSelected(nap.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowNapDashBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = napList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(napList[position])
    }

    fun setOnNapSelectedListener(listener: OnNapSelectedAction) {
        this.listener = listener
    }


}

interface OnNapSelectedAction {
    fun onNapSelected(napId: String)
}