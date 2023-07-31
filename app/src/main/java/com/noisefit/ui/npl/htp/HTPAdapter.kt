package com.noisefit.ui.npl.htp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.ItemHowToPlayBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class HTPAdapter : RecyclerView.Adapter<HTPAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<HTPDataModel>()

    inner class ViewHolder(val binding: ItemHowToPlayBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(htpData: HTPDataModel) {
            binding.tvTitle.text = htpData.title
            binding.tvMsg1.text = htpData.msg1
            /*binding.imageView.loadImage(
                binding.imageView.context,
                htpData.image,
                R.drawable.ic_empty_rect_banner
            )*/
            binding.tvMsg2.text=htpData.msg2
            if (htpData.msg2.isEmpty())
                binding.tvMsg2.gone()
            else
                binding.tvMsg2.visible()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemHowToPlayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: ArrayList<HTPDataModel>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()

    }
}