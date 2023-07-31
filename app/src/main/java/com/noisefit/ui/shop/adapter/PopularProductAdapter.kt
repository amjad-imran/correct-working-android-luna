package com.noisefit.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit_commans.data.model.ShopProduct
import com.noisefit.luna.databinding.RowPopularProductBinding
import com.noisefit_commans.ui.getCircleProgressDrawable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class PopularProductAdapter(val listener: ProductAction) :
    RecyclerView.Adapter<PopularProductAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<ShopProduct>()

    inner class ViewHolder(val binding: RowPopularProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(shopProduct: ShopProduct) {

            binding.tvProductName.text = shopProduct.title

            val imageUrl = try {
                shopProduct.images[0].src
            } catch (exp: Exception) {
                ""
            }

            Glide.with(binding.ivMain.context)
                .load(imageUrl)
                .placeholder(getCircleProgressDrawable(binding.ivMain.context))
                .into(binding.ivMain)

            val price = shopProduct.getProductPrice()
            if (price.isEmpty()) {
                binding.tvPrice.gone()
            } else {
                binding.tvPrice.text = "₹$price"
                binding.tvPrice.visible()
            }


            binding.root.setOnClickListener {
                listener.onProductClicked(shopProduct)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowPopularProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(it: List<ShopProduct>) {
        mDataSet.clear()
        mDataSet.addAll(it)
        notifyDataSetChanged()
    }
}

interface ProductAction {
    fun onProductClicked(product: ShopProduct)
}