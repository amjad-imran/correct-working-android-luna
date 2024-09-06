package com.oreo.ui.info.troubleshoot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.noisefit.luna.databinding.RowTroubleShootBinding
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible

class TroubleshootAdapter(val listener: TroubleShootAction) :
    RecyclerView.Adapter<TroubleshootAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<TroubleShootData>()
    private var showLocation = true

    inner class ViewHolder(private val binding: RowTroubleShootBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: TroubleShootData) {

            binding.tvTitle.text = data.title
            binding.tvMessage.text = data.message
            binding.ivMain.setImageResource(data.image)
            binding.tvCta.text = data.ctaText

            if (data.action == TroubleShootActionType.LAST_LOCATION && showLocation.not()) {
                binding.tvCta.invisible()
            } else {
                binding.tvCta.visible()

                val shimmerBuilder = Shimmer.AlphaHighlightBuilder().apply {
                    setBaseAlpha(0.5f)
                    setDuration(1500L)
                    setDropoff(0.2f)
                    setIntensity(0.35f)
                    setShape(Shimmer.Shape.RADIAL)
                }

                binding.shimmerContainer.setShimmer(
                    shimmerBuilder.build()
                )
            }

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

    fun setDataSet(dataSet: ArrayList<TroubleShootData>, showLocation: Boolean) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        this.showLocation = showLocation
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