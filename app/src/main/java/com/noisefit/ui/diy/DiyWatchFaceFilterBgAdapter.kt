package com.noisefit.ui.diy

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.imageview.ShapeableImageView
import com.noisefit.luna.R
import com.noisefit.data.model.DiyCustomWatchFaceBg
import com.noisefit.luna.databinding.LayoutDiySubImageListBinding
import com.noisefit_commans.ui.loadCircleWCacheWithProgress
import com.noisefit_commans.ui.visible
import com.noisefit.watch.WatchForm
import com.noisefit_commans.ui.loadImageWCacheWithProgress


class DiyWatchFaceFilterBgAdapter(val diyWatchFaceBgListener: DiyWatchFaceBgListener) :
    RecyclerView.Adapter<DiyWatchFaceFilterBgAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<DiyCustomWatchFaceBg>()
    private var screenType: WatchForm = WatchForm.SQUARE


    private fun <T> loadImage(bgImv: ImageView, url: T?) {
        when (screenType) {
            WatchForm.CIRCLE -> {
                bgImv.loadCircleWCacheWithProgress(bgImv.context, url)
            }

            else -> {
                bgImv.loadImageWCacheWithProgress(bgImv.context, url)
            }
        }
    }

    private fun handleViewsDim(showDim: Boolean, binding: LayoutDiySubImageListBinding) {
        var alpha = 1f
        if (showDim) {
            alpha = .6f
        }
        binding.bgLayerImv.alpha = alpha
        binding.bgImv.alpha = alpha
        binding.tvTitle.alpha = alpha
    }

    inner class ViewHolder(val binding: LayoutDiySubImageListBinding) :
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

            if (data.textLayerLink != null && !data.bgLink.isNullOrEmpty()) {
                binding.bgLayerImv.visible()
                setFilter(
                    binding.bgImv,
                    data.bgLink,
                    data.colorMatrix
                )
                loadImage(
                    binding.bgLayerImv,
                    data.textLayerLink,

                    )
                if (data.color == 0) {
                    binding.bgLayerImv.setColorFilter(Color.argb(255, 255, 255, 255))//WHITE
                } else {
                    binding.bgLayerImv.setColorFilter(data.color)
                }
            }
            binding.tvTitle.text = data.name

            binding.root.setOnClickListener {
                if (data.isSelected) {
                    return@setOnClickListener
                }
                handleData(position)
                diyWatchFaceBgListener.onWatchFaceClicked(data)
            }
        }


    }


    private fun setFilter(
        bgImv: ShapeableImageView,
        link: String?,
        colorMatrix: ColorMatrix?
    ) {

        if (colorMatrix == null) {
            loadImage(bgImv, link)
            return
        }

        Glide.with(bgImv.context)
            .asBitmap()
            .load(link)
            .apply(
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .dontTransform()
            )
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val rawBitmap = Bitmap.createBitmap(resource)
                    val paint = Paint()
                    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
                    val canvas = Canvas(rawBitmap)
                    canvas.drawBitmap(rawBitmap, 0f, 0f, paint)
//                    val outputImage: Bitmap = filter.processFilter(rawBitmap)
                    loadImage(bgImv, rawBitmap)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
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
    ): DiyWatchFaceFilterBgAdapter.ViewHolder {
        val binding = LayoutDiySubImageListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiyWatchFaceFilterBgAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    fun setDataSet(dataList: List<DiyCustomWatchFaceBg>,screenType: WatchForm ) {
        mDataSet.clear()
        mDataSet.addAll(dataList)
        this.screenType = screenType
        notifyDataSetChanged()
    }

    fun getSelectedPosition(): Int {
        var lastPos = -1
        mDataSet.forEachIndexed { index, watchFaceBg ->
            if (watchFaceBg.isSelected) {
                lastPos = index
                return@forEachIndexed
            }
        }
        return lastPos
    }

    override fun getItemCount(): Int = mDataSet.size

    fun dpToPx(px: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }

    interface DiyWatchFaceBgListener {
        fun onWatchFaceClicked(watchFaceBg: DiyCustomWatchFaceBg)
    }
}
