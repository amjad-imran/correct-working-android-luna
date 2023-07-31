package com.noisefit.ui.watchfacenew.custom

import alirezat775.lib.carouselview.CarouselAdapter
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.noisefit.R
import com.noisefit.databinding.RowWatchFaceCustomBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.models.CustomWatchFace
import com.noisefit_commans.utils.LOGS


class CustomWatchfaceSliderAdapter() : CarouselAdapter() {
    private var mDataSet = ArrayList<CustomWatchFace>()
    private var deviceType: String? = null
    private var backgroundImage: Uri? = null
    private var fontColor: Int = 0
    private var isCircleDial = false
    private var isSquareWithRadius = false

    fun setDeviceType(deviceType: String, isCircleDial: Boolean, isSquareWithRadius: Boolean) {
        this.deviceType = deviceType
        this.isCircleDial = isCircleDial
        this.isSquareWithRadius = isSquareWithRadius
    }

    inner class ViewHolder(private val binding: RowWatchFaceCustomBinding) :
        CarouselViewHolder(binding.root) {

        fun bind(watchFace: CustomWatchFace) {

            if (isCircleDial) {
                binding.ivBackgroundLayerCircle.visible()
                binding.ivBackgroundLayer.gone()
                if (backgroundImage == null) {
                    binding.ivBackgroundLayerCircle.setImageResource(watchFace.backgroundLayer)
                } else {
                    binding.ivBackgroundLayerCircle.setImageURI(null)
                    binding.ivBackgroundLayerCircle.setImageURI(backgroundImage)
                }
            } else {
                binding.ivBackgroundLayerCircle.gone()
                binding.ivBackgroundLayer.visible()
                if (isSquareWithRadius) {
                    val radius = binding.ivBackgroundLayer.context.resources.getDimension(R.dimen.dimen_30dp)
                    val shapeAppearanceModel = binding.ivBackgroundLayer.shapeAppearanceModel.toBuilder()
                        .setAllCornerSizes(radius)
                        .build()
                    binding.ivBackgroundLayer.shapeAppearanceModel = shapeAppearanceModel

                }

                if (backgroundImage == null) {
                    binding.ivBackgroundLayer.setImageResource(watchFace.backgroundLayer)
                } else {
                    binding.ivBackgroundLayer.setImageURI(null)
                    binding.ivBackgroundLayer.setImageURI(backgroundImage)
                }
            }
            binding.ivTextLayer.setImageResource(watchFace.textLayer)

            if (fontColor == 0) {
                binding.ivTextLayer.setColorFilter(Color.argb(255, 255, 255, 255))//WHITE
            } else {
                binding.ivTextLayer.setColorFilter(fontColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {

        val binding =
            RowWatchFaceCustomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int = mDataSet.size
    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        (holder as ViewHolder).bind(mDataSet[position])

    }

    fun setDataSet(dataSet: List<CustomWatchFace>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun setBackgroundImage(resultUri: Uri) {
        backgroundImage = resultUri
        notifyDataSetChanged()
    }

    fun setFontColor(color: Int) {
        fontColor = color
        notifyDataSetChanged()
    }
}