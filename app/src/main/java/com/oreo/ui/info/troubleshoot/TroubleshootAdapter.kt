package com.oreo.ui.info.troubleshoot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowTroubleShootBinding

class TroubleshootAdapter(val listener: TroubleShootAction) :
    RecyclerView.Adapter<TroubleshootAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<TroubleShootData>()

    inner class ViewHolder(private val binding: RowTroubleShootBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: TroubleShootData) {

            binding.tvTitle.text = data.title
            binding.tvMessage.text = data.message
            binding.ivMain.setImageResource(data.image)
            binding.tvCta.text = data.ctaText



            binding.tvCta.setOnClickListener {
                listener.onClicked(data.action)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            RowTroubleShootBinding.inflate(
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

    fun setDataSet(dataSet: ArrayList<TroubleShootData>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}

data class TroubleShootData(
    val title: String,
    val message: String,
    val image: Int,
    val ctaText: String,
    val action: TroubleShootActionType
)

enum class TroubleShootActionType {
    LAST_LOCATION, BLUETOOTH, CONTACT_US
}

interface TroubleShootAction {
    fun onClicked(action: TroubleShootActionType)
}