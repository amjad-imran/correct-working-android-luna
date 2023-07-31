package com.noisefit.ui.onboarding.pairing.pair

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowScanWatchBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadWatchImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.PromotionalUtil
import com.noisefit_commans.models.ColorFitDevice


class NearbyDevicesAdapter(private val listener: NearbyDevicesClickListener?) :
    RecyclerView.Adapter<NearbyDevicesAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<ColorFitDevice>()

    inner class ViewHolder(private val binding: RowScanWatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(colorFitDevice: ColorFitDevice) {

            val colorInfo = if (colorFitDevice.ringInfo != null) {
                " (${colorFitDevice.ringInfo?.color}, Size ${colorFitDevice.ringInfo?.size})"
            } else {
                null
            }

            val string = SpannableStringBuilder()
                .append("${colorFitDevice.bluetoothName}")

            if (!colorInfo.isNullOrEmpty()) {
                string.color(binding.tvWatchName.context.getColor(R.color.white_64)) {
                    append(
                        colorInfo
                    )
                }
            }

            binding.tvWatchName.text = string

            //colorFitDevice.bluetoothName + "${if (!colorInfo.isNullOrEmpty()) "($colorInfo)" else ""}"


            binding.tvWatchMacAddress.text = "MAC ${colorFitDevice.address}"


            if (colorFitDevice.ringInfo?.image.isNullOrEmpty()) {
                binding.ivWatchImage.loadWatchImage(
                    itemView.context,
                    colorFitDevice.url,
                    R.drawable.watch_default
                )
            } else {
                binding.ivWatchImage.loadWatchImage(
                    itemView.context,
                    colorFitDevice.ringInfo?.image ?: "",
                    R.drawable.watch_default
                )
            }

            binding.root.setOnClickListener {
                listener?.onDeviceClicked(colorFitDevice)
            }

            if (PromotionalUtil.showPromotionalBanner(colorFitDevice)) {
                binding.ivBg.loadImage(binding.ivBg.context, R.drawable.bg_pulse_scan)
                binding.ivBg.visible()
            } else {
                binding.ivBg.setImageResource(0)
                binding.ivBg.gone()
            }

            if (bindingAdapterPosition == (mDataSet.size - 1)) {
                binding.vDivider.gone()
            } else {
                binding.vDivider.visible()
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowScanWatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }


    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<ColorFitDevice>) {
        mDataSet = dataSet as ArrayList<ColorFitDevice>
        notifyDataSetChanged()
    }

    fun add(colorFitDevice: ColorFitDevice) {
        val itemCount = this.itemCount
        mDataSet.add(itemCount, colorFitDevice)
        notifyItemInserted(itemCount)

    }
}

interface NearbyDevicesClickListener {
    fun onDeviceClicked(colorFitDevice: ColorFitDevice)
}