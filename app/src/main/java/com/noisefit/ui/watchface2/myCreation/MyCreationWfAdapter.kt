package com.noisefit.ui.watchface2.myCreation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.imageview.ShapeableImageView
import com.noisefit.data.remote.response.DiyMyCreation
import com.noisefit.databinding.ItemMyCreationListBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.FilterUtils
import com.noisefit.watch.WatchForm
import com.noisefit_commans.ui.invisible


class MyCreationWfAdapter(private val listener: MyCreationInteractionListener) :
    RecyclerView.Adapter<MyCreationWfAdapter.ViewHolder>() {
    private var screenType: WatchForm = WatchForm.SQUARE
    private var mDataSet = ArrayList<DiyMyCreation>()
    private var isEdit = false
    private var widthHeight: Pair<Int, Int> = Pair(0, 0)


    fun setFilter(
        data: DiyMyCreation,
        bgImv: ShapeableImageView,
        link: String?,
        isTextLayer: Boolean,

        ) {

        Glide.with(bgImv.context)
            .asBitmap()
            .load(link)
            .apply(
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(widthHeight.first, widthHeight.second)
                    .dontTransform()
            )
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {

                    if (!resource.isRecycled) {
                        val rawBitmap = Bitmap.createBitmap(resource)
                        if (isTextLayer) {
                            data.textBitmap = rawBitmap
                            ApplicationUtils.loadImageWC(bgImv, data.textBitmap, screenType)
                        } else {

                            if (data.filter.isNotEmpty()) {

                                val paint = Paint()
                                paint.colorFilter =
                                    FilterUtils().getFilter(
                                        data.filter,
                                        data.filterIntensity
                                    )?.let {
                                        ColorMatrixColorFilter(
                                            it
                                        )
                                    }
                                val canvas = Canvas(rawBitmap)
                                canvas.drawBitmap(rawBitmap, 0f, 0f, paint)
                                data.bgBitmap = rawBitmap

                            } else {
                                data.bgBitmap = rawBitmap
                            }

                            ApplicationUtils.loadImageWC(bgImv, data.bgBitmap, screenType)

                        }
                    } else {
                        // The bitmap is recycled, so we need to load it again before displaying it
                        setFilter(data, bgImv, link, isTextLayer)
                    }


                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })


    }

    inner class ViewHolder(val binding: ItemMyCreationListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DiyMyCreation, position: Int) {


            if (isEdit) {
                binding.btnDelete.visible()
            } else {
                binding.btnDelete.invisible()
            }

            if (data.backgroundUrl.isNotEmpty()) {
                setFilter(data, binding.bgImv, data.backgroundUrl, false)
            }
            if (data.textLayerLink.isNotEmpty()) {
                binding.bgLayerImv.visible()
                setFilter(
                    data,
                    binding.bgLayerImv,
                    data.textLayerLink,
                    true
                )
            } else {
                binding.bgLayerImv.gone()
            }


            if (data.colour.isEmpty()) {
                binding.bgLayerImv.setColorFilter(Color.argb(255, 255, 255, 255))//WHITE
            } else {
                binding.bgLayerImv.setColorFilter(Color.parseColor(data.colour))
            }


            binding.btnDelete.setOnClickListener {
                listener.onDeleteClicked(data, position)
            }

            binding.root.setOnClickListener {
                listener.onWatchFaceClicked(data.id, data)
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMyCreationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(
        widthHeight: Pair<Int, Int>,
        list: List<DiyMyCreation>,
        isEdit: Boolean,
        screenType: WatchForm
    ) {
        this.widthHeight = widthHeight
        mDataSet.clear()
        this.screenType = screenType
        this.isEdit = isEdit
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }

    fun setEditMode(status: Boolean) {
        this.isEdit = status
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        mDataSet.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mDataSet.size)
    }


}

interface MyCreationInteractionListener {
    fun onWatchFaceClicked(watchFaceId: Int, watchface: DiyMyCreation)
    fun onDeleteClicked(watchface: DiyMyCreation, position: Int)
}