package com.noisefit.ui.watchface2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.data.remote.response.WatchFaceCategory2
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.luna.databinding.RowWatchfaceHCatBinding
import com.noisefit_commans.utils.StringUtils.capitalizeWords


class WatchfaceHorizontalCategoryAdapter(private val onCategoryClicked: (WatchFaceCategory2) -> Unit) :
    RecyclerView.Adapter<WatchfaceHorizontalCategoryAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<WatchFaceCategory2>()
    private var selectedCatId: Int = -1

    inner class ViewHolder(val binding: RowWatchfaceHCatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: WatchFaceCategory2) {

            if (selectedCatId == category.id) {
                binding.tvCatName.setBackgroundResource(R.drawable.back_modal_solid_purple)
            } else {
                binding.tvCatName.setBackgroundResource(R.drawable.back_modal_10)
            }

            binding.tvCatName.text = category.name.capitalizeWords()


            binding.root.setOnClickListener {
                selectedCatId = category.id
                onCategoryClicked(category)
                notifyDataSetChanged()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowWatchfaceHCatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(list: List<WatchFaceCategory2>, catId: Int) {
        selectedCatId = catId
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }

    fun getItemById(selectedId: Int): WatchFaceCategory2? {
        return mDataSet.firstOrNull {
            it.id == selectedId
        }
    }

    fun getItemDetailsById(selectedId: Int): Pair<WatchFaceCategory2?, Int> {
        val item = mDataSet.firstOrNull {
            it.id == selectedId
        } ?: return Pair(null, 0)


        val index = mDataSet.indexOf(item)
        return Pair(item,index)

    }
}