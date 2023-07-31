package com.noisefit.ui.settings.helpAndSupport.details

import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowOtherInfoBinding
import com.noisefit_commans.ui.html


class OtherInfoAdapter() :
    RecyclerView.Adapter<OtherInfoAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<String>()


    inner class ViewHolder(private val binding: RowOtherInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: String) {
            binding.tvOtherDesc.text = clearHtmlText(value)
            binding.tvOtherDesc.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun clearHtmlText(text: String?): Spanned? {
        return text?.replace("<p>", "")?.replace("</p>", "")?.replace("\\", "")?.html()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): OtherInfoAdapter.ViewHolder {
        val binding = RowOtherInfoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: OtherInfoAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataList: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size


}
