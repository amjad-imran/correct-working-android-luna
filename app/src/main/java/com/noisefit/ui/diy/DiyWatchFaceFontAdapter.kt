package com.noisefit.ui.diy

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.data.model.DiyCustomWatchFaceBg
import com.noisefit.luna.databinding.LayoutDiySubFontListBinding
import com.noisefit_commans.ui.loadCircleCacheWithProgress
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.ui.visible
import com.noisefit.watch.WatchForm


class DiyWatchFaceFontAdapter(val diyWatchFaceBgListener: DiyWatchFaceBgListener) :
    RecyclerView.Adapter<DiyWatchFaceFontAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<DiyCustomWatchFaceBg>()
    private var screenType: WatchForm = WatchForm.SQUARE

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

    inner class ViewHolder(private val binding: LayoutDiySubFontListBinding) :
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

            } else {
                binding.vCircleBackSelector.setBackgroundResource(R.color.transparent)
                binding.vBackSelector.setBackgroundResource(R.color.transparent)
            }
            if (screenType == WatchForm.CIRCLE) {
                loadImage(binding.bgImv,R.drawable.back_modal_solid_circle)
            }else{
                loadImage(binding.bgImv,R.drawable.back_modal_solid_10)
            }


//            LOGS.d("sdadsadsadasads ${Gson().toJson(data)}")
            if (data.bgLink != null ) {
                binding.bgLayerImv.visible()
                loadImage(binding.bgLayerImv, data.bgLink)
                if (data.color == 0) {
                    binding.bgLayerImv.setColorFilter(Color.argb(255, 255, 255, 255))//WHITE
                } else {
                    binding.bgLayerImv.setColorFilter(data.color)
                }
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
    ): DiyWatchFaceFontAdapter.ViewHolder {
        val binding = LayoutDiySubFontListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiyWatchFaceFontAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    fun setDataSet(dataList: List<DiyCustomWatchFaceBg>, screenType: WatchForm) {
        this.screenType = screenType
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
