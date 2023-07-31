package com.noisefit.ui.watchface2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.noisefit.luna.R
import com.noisefit.data.model.WatchFace2CategoryModal
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.luna.databinding.LayoutHavingAnIssueBinding
import com.noisefit.luna.databinding.LayoutMainCategoryListBinding
import com.noisefit.luna.databinding.LayoutMainCreateYourOwnBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadCircleCacheWithProgress
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit.ui.watchface2.adapter.HorizontalWatchfaceAdapter
import com.noisefit.ui.watchface2.sub.WF2SubListFrom
import com.noisefit.ui.watchface2.sub.Watchface2InteractionListener
import com.noisefit.watch.WatchForm
import com.noisefit_commans.utils.StringUtils.capitalizeWords

sealed class Watchface2ClickEnum {
    data class CreateYourOwnClick(val view: View) : Watchface2ClickEnum()

    data class HavingAnIssueClick(val view: View) : Watchface2ClickEnum()

    data class FilterClick(val view: View, val wF2SubListFrom: WF2SubListFrom) :
        Watchface2ClickEnum()

    data class CategoryClicked(val catId: Int, val catName: String, val isTitle: Boolean) :
        Watchface2ClickEnum()

    data class OnWatchfaceClicked(val watchface: Watchface2, val position: Int) :
        Watchface2ClickEnum()

    data class OnWatchMarkedFavourite(val favourite: Boolean, val watchface: Watchface2) :
        Watchface2ClickEnum()

    data class LoadMoreWatchFaces(val data: WatchFace2CategoryModal.CategoryList) :
        Watchface2ClickEnum()
}

class Watchface2CategoryAdapter :
    RecyclerView.Adapter<Watchface2ViewHolder>() {


    private var items = listOf<WatchFace2CategoryModal>()

    fun submitData(items: ArrayList<WatchFace2CategoryModal>, listToRefresh: ArrayList<Int>) {
        this.items = items
        listToRefresh.forEach {
            notifyItemChanged(it)
        }

    }

    fun submitData(items: ArrayList<WatchFace2CategoryModal>) {
        this.items = items
        notifyDataSetChanged()
    }

    var itemClickListener: ((item: Watchface2ClickEnum) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Watchface2ViewHolder {
        return when (viewType) {

            R.layout.layout_main_create_your_own -> Watchface2ViewHolder.CreateYourOwnViewHolder(
                LayoutMainCreateYourOwnBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )


            R.layout.layout_main_category_list -> Watchface2ViewHolder.CategoryListViewHolder(
                LayoutMainCategoryListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.layout_having_an_issue -> Watchface2ViewHolder.HavingAnIssueViewHolder(
                LayoutHavingAnIssueBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )


            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: Watchface2ViewHolder, position: Int) {
        holder.itemClickListener = itemClickListener

        when (holder) {

            is Watchface2ViewHolder.CreateYourOwnViewHolder -> holder.bind(
                items[position] as WatchFace2CategoryModal.CreateYourOwn,
                position
            )


            is Watchface2ViewHolder.CategoryListViewHolder -> holder.bind(
                items[position] as WatchFace2CategoryModal.CategoryList
            )

            is Watchface2ViewHolder.HavingAnIssueViewHolder -> holder.bind(
                items[position] as WatchFace2CategoryModal.HavingAnIssue
            )


        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is WatchFace2CategoryModal.CategoryList -> R.layout.layout_main_category_list
            is WatchFace2CategoryModal.CreateYourOwn -> R.layout.layout_main_create_your_own
            is WatchFace2CategoryModal.HavingAnIssue -> R.layout.layout_having_an_issue
        }
    }


}

sealed class Watchface2ViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((item: Watchface2ClickEnum) -> Unit)? = null

    class HavingAnIssueViewHolder(private val binding: LayoutHavingAnIssueBinding) :
        Watchface2ViewHolder(binding) {
        fun bind(
            data: WatchFace2CategoryModal.HavingAnIssue
        ) {
            binding.btnHavingAnIssue.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.HavingAnIssueClick(
                        binding.root
                    )
                )
            }
        }
    }

    class CreateYourOwnViewHolder(private val binding: LayoutMainCreateYourOwnBinding) :
        Watchface2ViewHolder(binding) {
        fun bind(
            data: WatchFace2CategoryModal.CreateYourOwn,
            position: Int
        ) {

            if (data.isWatchSupportDiy) {
                if (data.screenType == WatchForm.CIRCLE) {
                    binding.lytCreate.bgImv.apply {
                        loadCircleCacheWithProgress(
                            this.context,
                            data.url.ifEmpty { R.drawable.circle_create_your_own })
                    }
                } else {
                    binding.lytCreate.bgImv.apply {
                        loadImageCacheWithProgress(
                            this.context,
                            data.url.ifEmpty { R.drawable.rectangle_create_your_own })
                    }
                }

            } else {
                binding.containerMyCreation.gone()
                binding.lytCreate.tvDesc.text =
                    "Show your creativity and design your own watch faces"
                if (data.screenType == WatchForm.CIRCLE) {
                    binding.lytCreate.bgImv.apply {
                        loadCircleCacheWithProgress(
                            this.context,
                            R.drawable.ic_watchface_noisefit
                        )
                    }
                } else {
                    binding.lytCreate.bgImv.apply {
                        loadImageCacheWithProgress(
                            this.context,
                            R.drawable.ic_watchface_noisefit
                        )
                    }
                }
            }


            binding.tvMyCreation.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.FilterClick(
                        binding.root, WF2SubListFrom.MyCreation
                    )
                )
            }
            binding.imvMyCreation.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.FilterClick(
                        binding.root, WF2SubListFrom.MyCreation
                    )
                )
            }

            binding.tvTrending.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.FilterClick(
                        binding.root, WF2SubListFrom.Trending
                    )
                )
            }
            binding.imvTrending.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.FilterClick(
                        binding.root, WF2SubListFrom.Trending
                    )
                )
            }

            binding.tvNew.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.FilterClick(
                        binding.root, WF2SubListFrom.Newly
                    )
                )
            }
            binding.imvNew.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.FilterClick(
                        binding.root, WF2SubListFrom.Newly
                    )
                )
            }

            binding.tvPopular.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.FilterClick(
                        binding.root, WF2SubListFrom.Popular
                    )
                )
            }
            binding.imvPopular.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.FilterClick(
                        binding.root, WF2SubListFrom.Popular
                    )
                )
            }
            binding.lytCreate.btnCreate.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.CreateYourOwnClick(
                        binding.root
                    )
                )
            }

        }
    }

    class CategoryListViewHolder(private val binding: LayoutMainCategoryListBinding) :
        Watchface2ViewHolder(binding) {
        fun bind(
            data: WatchFace2CategoryModal.CategoryList
        ) {

            binding.tvCatTitle.text = data.categoryData.name.capitalizeWords()

            binding.rvWatchFaces.apply {
                layoutManager = LinearLayoutManager(
                    binding.rvWatchFaces.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }

            val adapter = HorizontalWatchfaceAdapter(object : Watchface2InteractionListener {
                override fun onWatchFaceClicked(
                    watchFaceId: Int,
                    watchface: Watchface2,
                    position: Int
                ) {
                    itemClickListener?.invoke(
                        Watchface2ClickEnum.OnWatchfaceClicked(
                            watchface,
                            bindingAdapterPosition
                        )
                    )
                }

                override fun onMarkFavouriteClicked(
                    favourite: Boolean,
                    watchface: Watchface2,
                    position: Int
                ) {
                    itemClickListener?.invoke(
                        Watchface2ClickEnum.OnWatchMarkedFavourite(
                            favourite,
                            watchface
                        )
                    )
                }

            })
            binding.rvWatchFaces.adapter = adapter
            adapter.setDataSet(data.categoryData.faces ?: ArrayList(), data.screenType)
            binding.container.setOnClickListener {
                itemClickListener?.invoke(
                    Watchface2ClickEnum.CategoryClicked(
                        data.categoryData.id,
                        data.categoryData.name,
                        isTitle = true
                    )
                )
            }


            if ((data.currentSubListScrollPos ?: 0) > 0) {
                binding.rvWatchFaces.scrollToPosition(data.currentSubListScrollPos ?: 0)
            }

            binding.rvWatchFaces.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                        if (!data.hasMoreData) return

                        binding.rvWatchFaces.let {
                            val visibleItemCount = it.layoutManager?.childCount ?: 0
                            val totalItemCount = it.layoutManager?.itemCount ?: 0
                            val firstVisibleItemPosition =
                                (it.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            data.currentSubListScrollPos = firstVisibleItemPosition
                            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                                itemClickListener?.invoke(
                                    Watchface2ClickEnum.LoadMoreWatchFaces(
                                        data
                                    )
                                )
                            }
                        }
                    }
                }

            })

        }
    }


}


