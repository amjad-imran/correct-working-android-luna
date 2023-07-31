package com.noisefit.ui.diy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.noisefit.luna.R
import com.noisefit.data.model.DiyCustomWatchColor
import com.noisefit.data.model.DiyCustomWatchFaceBg
import com.noisefit.data.model.DiyWatchFaceModal
import com.noisefit.luna.databinding.LayoutDiyBgImageListBinding
import com.noisefit.luna.databinding.LayoutDiyColorListBinding
import com.noisefit.luna.databinding.LayoutDiyFeaturedImageListBinding
import com.noisefit.luna.databinding.LayoutDiyFilterImageListBinding
import com.noisefit.luna.databinding.LayoutDiyFontListBinding
import com.noisefit.luna.databinding.LayoutDiyPlacementImageListBinding
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.LOGS


sealed  class DiyClickEnum {
    data class BgListImageClick(val view: View, val data: DiyCustomWatchFaceBg, val position: Int) :
        DiyClickEnum()

    data class FeaturedImageClick(
        val view: View,
        val data: DiyCustomWatchFaceBg,
        val position: Int
    ) :
        DiyClickEnum()

    data class FilterImageClick(val view: View, val data: DiyCustomWatchFaceBg, val position: Int) :
        DiyClickEnum()

    data class FilterIntensityClick(val view: View, val percentage: Int, val position: Int) :
        DiyClickEnum()

    data class PlacementImageClick(
        val view: View,
        val data: DiyCustomWatchFaceBg,
        val position: Int
    ) :
        DiyClickEnum()

    data class FontClick(
        val view: View,
        val data: DiyCustomWatchFaceBg,
        val position: Int
    ) :
        DiyClickEnum()

    data class ColorClick(val view: View, val color: Int, val position: Int) :
        DiyClickEnum()

}

class DiyWatchFaceAdapter :
    RecyclerView.Adapter<DiyRecyclerViewHolder>() {


    private var items = listOf<DiyWatchFaceModal>()

    fun submitData(items: ArrayList<DiyWatchFaceModal>, listToRefresh: ArrayList<Int>) {
        this.items = items
        LOGS.d("filter click submit data")
        listToRefresh.forEach {
            notifyItemChanged(it)
        }

    }

    fun submitData(items: ArrayList<DiyWatchFaceModal>) {
        this.items = items
        notifyDataSetChanged()
    }

    var itemClickListener: ((item: DiyClickEnum) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiyRecyclerViewHolder {
        return when (viewType) {

            R.layout.layout_diy_bg_image_list -> DiyRecyclerViewHolder.BgListViewHolder(
                LayoutDiyBgImageListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.layout_diy_filter_image_list -> DiyRecyclerViewHolder.FilterListViewHolder(
                LayoutDiyFilterImageListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.layout_diy_placement_image_list -> DiyRecyclerViewHolder.PlacementListViewHolder(
                LayoutDiyPlacementImageListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.layout_diy_font_list -> DiyRecyclerViewHolder.FontStyleViewHolder(
                LayoutDiyFontListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.layout_diy_color_list -> DiyRecyclerViewHolder.ColorListViewHolder(
                LayoutDiyColorListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.layout_diy_featured_image_list -> DiyRecyclerViewHolder.FeaturedListViewHolder(
                LayoutDiyFeaturedImageListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )


            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: DiyRecyclerViewHolder, position: Int) {
        holder.itemClickListener = itemClickListener

        when (holder) {

            is DiyRecyclerViewHolder.BgListViewHolder -> holder.bind(
                items[position] as DiyWatchFaceModal.BackgroundList,
                position
            )

            is DiyRecyclerViewHolder.FeaturedListViewHolder -> holder.bind(
                items[position] as DiyWatchFaceModal.FeaturedList,
                position
            )

            is DiyRecyclerViewHolder.FilterListViewHolder -> holder.bind(
                items[position] as DiyWatchFaceModal.FilterList,
                position
            )

            is DiyRecyclerViewHolder.PlacementListViewHolder -> holder.bind(
                items[position] as DiyWatchFaceModal.PlacementList,
                position
            )

            is DiyRecyclerViewHolder.ColorListViewHolder -> holder.bind(
                items[position] as DiyWatchFaceModal.ColorList,
                position
            )

            is DiyRecyclerViewHolder.FontStyleViewHolder -> holder.bind(
                items[position] as DiyWatchFaceModal.FontList,
                position
            )
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DiyWatchFaceModal.BackgroundList -> R.layout.layout_diy_bg_image_list
            is DiyWatchFaceModal.FeaturedList -> R.layout.layout_diy_featured_image_list
            is DiyWatchFaceModal.FilterList -> R.layout.layout_diy_filter_image_list
            is DiyWatchFaceModal.PlacementList -> R.layout.layout_diy_placement_image_list
            is DiyWatchFaceModal.FontList -> R.layout.layout_diy_font_list
            is DiyWatchFaceModal.ColorList -> R.layout.layout_diy_color_list
        }
    }


}

sealed class DiyRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((item: DiyClickEnum) -> Unit)? = null


    class BgListViewHolder(private val binding: LayoutDiyBgImageListBinding) :
        DiyRecyclerViewHolder(binding) {
        fun bind(
            data: DiyWatchFaceModal.BackgroundList,
            position: Int
        ) {

            val diyWatchFaceAdapter = DiyWatchFaceBgAdapter(object :
                DiyWatchFaceBgAdapter.DiyWatchFaceBgListener {
                override fun onWatchFaceClicked(diyCustomWatchFaceBg: DiyCustomWatchFaceBg,position: Int) {
                    itemClickListener?.invoke(
                        DiyClickEnum.BgListImageClick(
                            binding.root,
                            diyCustomWatchFaceBg,
                            position
                        )
                    )
                }

            })
            val layoutManager1 = LinearLayoutManager(
                binding.rv.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rv.apply {
                layoutManager = layoutManager1
                adapter = diyWatchFaceAdapter
                itemAnimator = null
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }
            binding.tvTitle.text = data.title
            diyWatchFaceAdapter.setDataSet(data.list, data.screenType, true)

            diyWatchFaceAdapter.getSelectedPosition().takeIf {
                it != -1
            }?.apply {
                tryCatch {
                    binding.rv.scrollToPosition(this)
                }
            }

        }
    }

    class FeaturedListViewHolder(private val binding: LayoutDiyFeaturedImageListBinding) :
        DiyRecyclerViewHolder(binding) {
        fun bind(
            data: DiyWatchFaceModal.FeaturedList,
            position: Int
        ) {

            val diyWatchFaceAdapter = DiyWatchFaceBgAdapter(object :
                DiyWatchFaceBgAdapter.DiyWatchFaceBgListener {
                override fun onWatchFaceClicked(diyCustomWatchFaceBg: DiyCustomWatchFaceBg,position: Int) {
                    itemClickListener?.invoke(
                        DiyClickEnum.FeaturedImageClick(
                            binding.root,
                            diyCustomWatchFaceBg,
                            position
                        )
                    )
                }

            })
            val layoutManager1 = LinearLayoutManager(
                binding.rv.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rv.apply {
                layoutManager = layoutManager1
                adapter = diyWatchFaceAdapter
                itemAnimator = null
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }
            binding.tvTitle.text = data.title
            diyWatchFaceAdapter.setDataSet(data.list, data.screenType, true)

            diyWatchFaceAdapter.getSelectedPosition().takeIf {
                it != -1
            }?.apply {
                tryCatch {
                    binding.rv.scrollToPosition(this)
                }
            }

        }
    }

    class PlacementListViewHolder(private val binding: LayoutDiyPlacementImageListBinding) :
        DiyRecyclerViewHolder(binding) {
        fun bind(
            data: DiyWatchFaceModal.PlacementList,
            position: Int
        ) {

            val diyWatchFaceAdapter = DiyWatchFaceBgAdapter(object :
                DiyWatchFaceBgAdapter.DiyWatchFaceBgListener {
                override fun onWatchFaceClicked(diyCustomWatchFaceBg: DiyCustomWatchFaceBg,position: Int) {
                    itemClickListener?.invoke(
                        DiyClickEnum.PlacementImageClick(
                            binding.root,
                            diyCustomWatchFaceBg,
                            position
                        )
                    )
                }

            })
            val layoutManager1 = LinearLayoutManager(
                binding.rv.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rv.apply {
                layoutManager = layoutManager1
                adapter = diyWatchFaceAdapter
                itemAnimator = null
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }
            binding.tvTitle.text = data.title
            diyWatchFaceAdapter.setDataSet(data.list, data.screenType, false)


            diyWatchFaceAdapter.getSelectedPosition().takeIf {
                it != -1
            }?.apply {
                tryCatch {
                    binding.rv.scrollToPosition(this)
                }
            }

        }
    }

    class FontStyleViewHolder(private val binding: LayoutDiyFontListBinding) :
        DiyRecyclerViewHolder(binding) {
        fun bind(
            data: DiyWatchFaceModal.FontList,
            position: Int
        ) {

            val diyWatchFaceAdapter = DiyWatchFaceFontAdapter(object :
                DiyWatchFaceFontAdapter.DiyWatchFaceBgListener {
                override fun onWatchFaceClicked(diyCustomWatchFaceBg: DiyCustomWatchFaceBg,position: Int) {
                    itemClickListener?.invoke(
                        DiyClickEnum.FontClick(
                            binding.root,
                            diyCustomWatchFaceBg,
                            position
                        )
                    )
                }

            })
            val layoutManager1 = LinearLayoutManager(
                binding.rv.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rv.apply {
                layoutManager = layoutManager1
                adapter = diyWatchFaceAdapter
                itemAnimator = null
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }
            binding.tvTitle.text = data.title
            diyWatchFaceAdapter.setDataSet(data.list, data.screenType)


            diyWatchFaceAdapter.getSelectedPosition().takeIf {
                it != -1
            }?.apply {
                tryCatch {
                    binding.rv.scrollToPosition(this)
                }
            }

        }
    }

    class ColorListViewHolder(private val binding: LayoutDiyColorListBinding) :
        DiyRecyclerViewHolder(binding) {
        fun bind(
            data: DiyWatchFaceModal.ColorList,
            position: Int
        ) {
            binding.tvTitle.text = data.title

            val diyWatchFaceAdapter = DiyColorAdapter(object :
                DiyColorAdapter.DiyWatchFaceBgListener {


                override fun onWatchFaceClicked(
                    diyCustomWatchFaceBg: DiyCustomWatchColor,
                    position: Int
                ) {
                    itemClickListener?.invoke(
                        DiyClickEnum.ColorClick(
                            binding.root,
                            diyCustomWatchFaceBg.color,
                            position
                        )
                    )
                }

            })
            val layoutManager1 = LinearLayoutManager(
                binding.rv.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rv.apply {
                layoutManager = layoutManager1
                adapter = diyWatchFaceAdapter
                itemAnimator = null
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }
            binding.tvTitle.text = data.title
            diyWatchFaceAdapter.setDataSet(data.colorList)


            diyWatchFaceAdapter.getSelectedPosition().takeIf {
                it != -1
            }?.apply {
                tryCatch {
                    binding.rv.scrollToPosition(this)
                }
            }

        }
    }


    class FilterListViewHolder(private val binding: LayoutDiyFilterImageListBinding) :
        DiyRecyclerViewHolder(binding) {

        private fun handleViewsDim(showDim: Boolean, binding: LayoutDiyFilterImageListBinding) {
            var alpha = 1f
            if (showDim) {
                alpha = .3f
            }
            binding.tvIntensityPer.alpha = alpha
            binding.tvIntensityTxt.alpha = alpha
            binding.seekbar.alpha = alpha
        }

        private fun checkNoneSelected(
            data: DiyWatchFaceModal.FilterList,
            binding: LayoutDiyFilterImageListBinding
        ) {
            val noneIndex =
                data.list.indexOfFirst { it.name.lowercase() == "none" && it.isSelected }

            if (noneIndex == -1) {
                binding.seekbar.enable()
                handleViewsDim(false, binding)
            } else {
                binding.seekbar.disable()
                handleViewsDim(true, binding)
            }
        }

        fun bind(
            data: DiyWatchFaceModal.FilterList,
            position: Int
        ) {

            val diyWatchFaceAdapter = DiyWatchFaceFilterBgAdapter(object :
                DiyWatchFaceFilterBgAdapter.DiyWatchFaceBgListener {
                override fun onWatchFaceClicked(watchFaceBg: DiyCustomWatchFaceBg) {
                    if (watchFaceBg.name.lowercase() == "none") {
                        binding.seekbar.disable()
                        handleViewsDim(true, binding)
                    } else {
                        binding.seekbar.enable()
                        handleViewsDim(false, binding)
                    }
                    itemClickListener?.invoke(
                        DiyClickEnum.FilterImageClick(
                            binding.root,
                            watchFaceBg,
                            position
                        )
                    )
                }

            })

            checkNoneSelected(data, binding)


            val percentage = "${data.colorIntensity}%"
            binding.tvIntensityPer.text = percentage
            binding.seekbar.progress = data.colorIntensity

            binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    // here, you react to the value being set in seekBar
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // you can probably leave this empty
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // you can probably leave this empty
//                    data.colorIntensity = seekBar.progress
//                    itemClickListener?.invoke(
//                        DiyClickEnum.FilterIntensityClick(
//                            binding.root,
//                            seekBar.progress,
//                            position
//                        )
//                    )
                    val percentage1 = "${data.colorIntensity}%"
                    binding.tvIntensityPer.text = percentage1
                    data.colorIntensity = seekBar.progress
                    itemClickListener?.invoke(
                        DiyClickEnum.FilterIntensityClick(
                            binding.root,
                            seekBar.progress,
                            position
                        )
                    )
                }
            })

            val layoutManager1 = LinearLayoutManager(
                binding.rv.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rv.apply {
                layoutManager = layoutManager1
                adapter = diyWatchFaceAdapter
                itemAnimator = null
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }
            binding.tvTitle.text = data.title
            diyWatchFaceAdapter.setDataSet(data.list, data.screenType)

            diyWatchFaceAdapter.getSelectedPosition().takeIf {
                it != -1
            }?.apply {
                tryCatch {
                    binding.rv.scrollToPosition(this)
                }
            }
        }
    }


}


