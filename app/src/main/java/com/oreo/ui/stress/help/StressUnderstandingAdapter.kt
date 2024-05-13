package com.oreo.ui.stress.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemStressImageWithTextListBinding
import com.noisefit.luna.databinding.ItemStressLottieWithTextListBinding
import com.noisefit.luna.databinding.ItemStressTextWithAdapterListBinding
import com.noisefit_commans.ui.html
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.oreo.data.model.StressUnderstandingOverview


class StressUnderstandingAdapter :
    RecyclerView.Adapter<HomeRecyclerViewHolder>() {


    var items = listOf<StressUnderstandingOverview>()
        set(value) {
            field = value
//            if(refreshPosition != null && refreshPosition != -1){
//                notifyItemChanged(refreshPosition!!)
//            }else{
//                notifyDataSetChanged()
//            }
            notifyDataSetChanged()

        }

    var itemClickListener: ((view: View, item: StressUnderstandingOverview, position: Int) -> Unit)? =
        null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        return when (viewType) {
            R.layout.item_stress_image_with_text_list -> HomeRecyclerViewHolder.ImageWithTextViewHolder(
                ItemStressImageWithTextListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_stress_lottie_with_text_list -> HomeRecyclerViewHolder.LottieWithTextViewHolder(
                ItemStressLottieWithTextListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_stress_text_with_adapter_list -> HomeRecyclerViewHolder.TextWithAdapterViewHolder(
                ItemStressTextWithAdapterListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        holder.itemClickListener = itemClickListener
        when (holder) {
            is HomeRecyclerViewHolder.ImageWithTextViewHolder -> holder.bind(
                items[position] as StressUnderstandingOverview.ImageWithText,
                position
            )

            is HomeRecyclerViewHolder.TextWithAdapterViewHolder -> holder.bind(
                items[position] as StressUnderstandingOverview.TextWithAdapter,
                position
            )

            is HomeRecyclerViewHolder.LottieWithTextViewHolder -> holder.bind(
                items[position] as StressUnderstandingOverview.LottieWithText,
                position
            )
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is StressUnderstandingOverview.ImageWithText -> R.layout.item_stress_image_with_text_list
            is StressUnderstandingOverview.LottieWithText -> R.layout.item_stress_lottie_with_text_list
            is StressUnderstandingOverview.TextWithAdapter -> R.layout.item_stress_text_with_adapter_list

        }
    }
}


sealed class HomeRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((view: View, item: StressUnderstandingOverview, position: Int) -> Unit)? =
        null

    class ImageWithTextViewHolder(private val binding: ItemStressImageWithTextListBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: StressUnderstandingOverview.ImageWithText,
            position: Int
        ) {

            binding.tvTitle.text = data.title
            binding.tvDesc.text = data.description.html()
            binding.imv.loadImage(binding.imv.context, data.image)

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, data, position)
            }
        }
    }

    class LottieWithTextViewHolder(private val binding: ItemStressLottieWithTextListBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: StressUnderstandingOverview.LottieWithText,
            position: Int
        ) {

            binding.tvTitle.text = data.title
            binding.tvDesc.text = data.description.html()

            binding.lottieAnimationView.repeatCount = LottieDrawable.INFINITE
            binding.lottieAnimationView.setAnimation(data.lottie)
            binding.lottieAnimationView.playAnimation()

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, data, position)
            }
        }
    }


    class TextWithAdapterViewHolder(private val binding: ItemStressTextWithAdapterListBinding) :
        HomeRecyclerViewHolder(binding) {
        fun bind(
            data: StressUnderstandingOverview.TextWithAdapter,
            position: Int
        ) {

            binding.tvTitle.text = data.title
            binding.tvDesc.text = data.description.html()

            binding.rv.apply {
                layoutManager = LinearLayoutManager(
                    binding.rv.context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }


            val adapter = StressUnderstandingSubAdapter()
            adapter.setDataSet(data.subList)
            binding.rv.adapter = adapter

            if (data.showBottomLine) {
                binding.line.root.visible()
            } else {
                binding.line.root.invisible()
            }
            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, data, position)
            }
        }
    }


}
