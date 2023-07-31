package com.noisefit.ui.watchface.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit.databinding.RowWatchFaceCategoryBinding
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.utils.DateFormats

class WatchFaceCategoryAdapter(private val listener: WatchCatActions) :
    RecyclerView.Adapter<WatchFaceCategoryAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<CatWiseWatchFacesItem>()

    inner class ViewHolder(val binding: RowWatchFaceCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: CatWiseWatchFacesItem) {

            if (category.id == -1 || (category.faces.size <= 2)) {
                binding.ivCatMoreBtn.invisible()
            } else {
                binding.ivCatMoreBtn.visible()
            }
            if (category.id == -4) { //Design Category
                binding.ivCatMoreBtn.visible()
            }


            binding.tvCatName.text =
                category.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(DateFormats.defaultLocale) else it.toString() }

            binding.tvCatName.setOnClickListener {
                listener.onCategoryClicked(category.id, category.name)
            }
            binding.ivCatMoreBtn.setOnClickListener {


                listener.onCategoryClicked(category.id, category.name)
            }

            binding.rvWatchFace.layoutManager = LinearLayoutManager(
                binding.rvWatchFace.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rvWatchFace.adapter = WatchFaceListAdapter(object : WatchFaceActions {
                override fun onWatchFaceClicked(watchFaceId: Int, watchface: WatchFace) {
                    listener.onWatchFaceClicked(watchFaceId, watchface)
                }

                override fun onMarkFavouriteClicked(
                    favourite: Boolean,
                    watchface: WatchFace,
                    position: Int
                ) {
                    listener.onMarkFavouriteClicked(
                        favourite,
                        watchface,
                        position,
                        bindingAdapterPosition
                    )
                }
            }).apply {
                this.setDataSet(
                    mDataSet[bindingAdapterPosition].faces,
                    mDataSet[bindingAdapterPosition].id
                )
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowWatchFaceCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])


    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<CatWiseWatchFacesItem>) {
        mDataSet.clear()
        mDataSet.addAll(list)
        //mDataSet = list as ArrayList<CatWiseWatchFacesItem>
        notifyDataSetChanged()
    }

    fun resetFavState(state: Boolean, innerPosition: Int, position: Int) {
        tryCatch {
            mDataSet[position].faces[innerPosition].is_favourite = if (state) "1" else "0"
            notifyItemChanged(position)
        }
    }
}

interface WatchCatActions {
    fun onCategoryClicked(categoryId: Int, categoryName: String)
    fun onWatchFaceClicked(watchFaceId: Int,watchFace: WatchFace)
    fun onMarkFavouriteClicked(
        favourite: Boolean,
        watchFace: WatchFace,
        innerPosition: Int,
        position: Int
    )
}