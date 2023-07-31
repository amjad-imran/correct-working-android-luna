package com.noisefit.ui.watchface.mode2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.noisefit.R
import com.noisefit.data.model.WatchFaceListModal
import com.noisefit.databinding.LayoutListCreateYourOwnBinding
import com.noisefit.databinding.LayoutListWfSupBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.watch.WatchForm
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.ui.loadCircleImageWithoutCache
import com.noisefit_commans.ui.loadImageWithoutCache
import com.noisefit_commans.utils.LOGS


interface WatchFaceMainActions {
    fun onWatchFaceClicked(watchFace: WatchFace)
    fun onCreateWFClick()
}


class WatchFaceMainListAdapter :
    RecyclerView.Adapter<WatchFaceListViewHolder>() {


    private var items = listOf<WatchFaceListModal>()

    fun submitData(items: ArrayList<WatchFaceListModal>, listToRefresh: ArrayList<Int>) {
        this.items = items
        listToRefresh.forEach {
            notifyItemChanged(it)
        }

    }

    fun submitData(items: List<WatchFaceListModal>) {
        this.items = items
        notifyDataSetChanged()
    }

    var itemClickListener: WatchFaceMainActions? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchFaceListViewHolder {
        return when (viewType) {

            R.layout.layout_list_create_your_own -> WatchFaceListViewHolder.CreateYourOwnViewHolder(
                LayoutListCreateYourOwnBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )


            R.layout.layout_list_wf_sup -> WatchFaceListViewHolder.CategoryListViewHolder(
                LayoutListWfSupBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )


            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: WatchFaceListViewHolder, position: Int) {
        holder.itemClickListener = itemClickListener

        when (holder) {

            is WatchFaceListViewHolder.CreateYourOwnViewHolder -> holder.bind(
                items[position] as WatchFaceListModal.CreateYourOwn,
                position
            )


            is WatchFaceListViewHolder.CategoryListViewHolder -> holder.bind(
                items[position] as WatchFaceListModal.CategoryList
            )


        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is WatchFaceListModal.CategoryList -> R.layout.layout_list_wf_sup
            is WatchFaceListModal.CreateYourOwn -> R.layout.layout_list_create_your_own
        }
    }


}

sealed class WatchFaceListViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: WatchFaceMainActions? = null


    class CreateYourOwnViewHolder(private val binding: LayoutListCreateYourOwnBinding) :
        WatchFaceListViewHolder(binding) {
        fun bind(
            data: WatchFaceListModal.CreateYourOwn,
            position: Int
        ) {

            val context = binding.ivCurrentWatchface.context
            if (data.screenType == WatchForm.CIRCLE) {
                binding.ivCurrentWatchface.loadCircleImageWithoutCache(
                    context,
                    data.uri,
                    R.drawable.ic_watchface_noisefit
                )

            } else {
                binding.ivCurrentWatchface.loadImageWithoutCache(
                    context,
                    data.uri,
                    R.drawable.ic_watchface_noisefit
                )
            }

            if (!data.showCustWfState.first) {
                binding.tvHeader.gone()
            }

            if (!data.showCustWfState.second) {
                binding.ivCurrentWatchface.gone()
            }
            if (!data.showCustWfState.third) {
                binding.bCustomise.gone()
            }


            binding.bCustomise.setOnClickListener {
                itemClickListener?.onCreateWFClick()
            }

        }
    }

    class CategoryListViewHolder(private val binding: LayoutListWfSupBinding) :
        WatchFaceListViewHolder(binding) {
        fun bind(
            data: WatchFaceListModal.CategoryList
        ) {

            if (data.dataList.isEmpty()) {
                binding.textView41.gone()
                binding.textView42.gone()
            } else {
                binding.textView41.visible()
                binding.textView42.visible()
            }


            binding.rv.apply {
                layoutManager = GridLayoutManager(binding.rv.context, 2)
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }
            val adapter = WatchFaceListAdapter(object : WatchFaceActions {

                override fun onWatchFaceClicked(watchFace: WatchFace) {
                    LOGS.d("Saddsaasddsa count e1132123231")
                    itemClickListener?.onWatchFaceClicked(watchFace)
                }

            })

            binding.rv.adapter = adapter
            adapter.setDataSet(data.dataList)


        }
    }


}
