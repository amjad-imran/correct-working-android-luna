package com.noisefit.ui.diy

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.data.model.DiyCustomWatchFaceBg
import com.noisefit.databinding.LayoutDiySubImageListBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.watch.WatchForm
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadCircleCacheWithProgress
import com.noisefit_commans.ui.loadImageCacheWithProgress


class DiyWatchFaceBgAdapter(val diyWatchFaceBgListener: DiyWatchFaceBgListener) :
    RecyclerView.Adapter<DiyWatchFaceBgAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<DiyCustomWatchFaceBg>()
    private var screenType: WatchForm = WatchForm.SQUARE
    private var isMyImages: Boolean = false

    private fun <T> loadImage(bgImv: ImageView, url: T?) {
        when (screenType) {
            WatchForm.CIRCLE -> {

                bgImv.loadCircleCacheWithProgress(bgImv.context, url)
            }

            else -> {
                bgImv.loadImageCacheWithProgress(bgImv.context, url)
            }
        }
    }

    private fun handleViewsDim(
        showDim: Boolean,
        binding: LayoutDiySubImageListBinding,
    ) {

        if(isMyImages){
            return
        }
        var alpha = 1f
        if (showDim) {
            alpha = .6f
        }
        binding.bgLayerImv.alpha = alpha
        binding.bgImv.alpha = alpha
        binding.tvTitle.alpha = alpha
    }

    inner class ViewHolder(private val binding: LayoutDiySubImageListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DiyCustomWatchFaceBg, position: Int) {
            if (data.isSelected) {
                if (screenType == WatchForm.CIRCLE) {
                    binding.vCircleBackSelector.visible()
                    binding.vCircleBackSelector.setBackgroundResource(R.drawable.back_modal_circle_white)
                } else {
                    binding.vBackSelector.visible()
                    binding.vBackSelector.setBackgroundResource(R.drawable.back_modal_white)
                }
                handleViewsDim(false, binding)
            } else {
                handleViewsDim(true, binding)
                binding.vCircleBackSelector.setBackgroundResource(R.color.transparent)
                binding.vBackSelector.setBackgroundResource(R.color.transparent)
            }

            if (data.name.lowercase() == "upload") {
                binding.btnUpload.visible()
                loadImage(binding.btnUpload,R.drawable.ic_bg_upload_rectange)
                binding.bgImv.invisible()
                binding.bgLayerImv.invisible()
            } else {
                binding.btnUpload.gone()
                binding.bgImv.visible()
                binding.bgLayerImv.visible()
            }

            if (data.textLayerLink != null && !data.bgLink.isNullOrEmpty()) {
                binding.bgLayerImv.visible()
                loadImage(binding.bgImv, data.bgLink)
                loadImage(binding.bgLayerImv, data.textLayerLink)
                if (data.color == 0) {
                    binding.bgLayerImv.setColorFilter(Color.argb(255, 255, 255, 255))//WHITE
                } else {
                    binding.bgLayerImv.setColorFilter(data.color)
                }
            } else if (!data.bgLink.isNullOrEmpty()) {
                loadImage(binding.bgImv, data.bgLink)
            }

            var name = data.name
            if (name.lowercase() == "light") {
                name = "Rounded"
            }
            binding.tvTitle.text = name


            binding.root.setOnClickListener {
                if (data.isSelected) {
                    return@setOnClickListener
                }
                if (data.name.lowercase() != "upload") {
                    handleData(position)
                }

                diyWatchFaceBgListener.onWatchFaceClicked(data, position)
            }
        }

    }

    private fun handleData(position: Int) {
        var lastSelectedPosition = -1
        mDataSet.forEachIndexed { index, watchFaceBg ->
            if (watchFaceBg.isSelected) {
                lastSelectedPosition = index
                watchFaceBg.isSelected = false
            }
        }

        mDataSet[position].isSelected = true


        if (lastSelectedPosition != -1) {
            notifyItemChanged(lastSelectedPosition)
        }
        notifyItemChanged(position)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiyWatchFaceBgAdapter.ViewHolder {
        val binding = LayoutDiySubImageListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiyWatchFaceBgAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    fun setDataSet(
        dataList: List<DiyCustomWatchFaceBg>,
        screenType: WatchForm,
        isMyImages: Boolean
    ) {
        this.screenType = screenType
        this.isMyImages = isMyImages
        mDataSet.clear()
        mDataSet.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size

    fun getSelectedPosition(): Int {
        var lastPos = -1
        mDataSet.forEachIndexed { index, watchFaceBg ->
            if (watchFaceBg.isSelected) {
                if (index in 0..1) {
                    return lastPos
                }
                lastPos = index
                return@forEachIndexed
            }
        }
        return lastPos
    }

    fun dpToPx(px: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }

    interface DiyWatchFaceBgListener {
        fun onWatchFaceClicked(diyCustomWatchFaceBg: DiyCustomWatchFaceBg, position: Int)
    }
}
