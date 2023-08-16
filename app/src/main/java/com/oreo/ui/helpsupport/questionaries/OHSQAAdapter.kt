package com.oreo.ui.helpsupport.questionaries

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.OreoHsQuestionariesItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OHSQuestionariesResponseModel

class OHSQAAdapter :
    RecyclerView.Adapter<OHSQAAdapter.ViewHolder>() {
    private val mDataset = ArrayList<OHSQuestionariesResponseModel>()
    var lastSelectedPos: Int = -1

    inner class ViewHolder(val binding: OreoHsQuestionariesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: OHSQuestionariesResponseModel) {
            binding.tvTitle.text = result.question
            binding.tvDesc.text = fromHtml(result.answer)

            if (lastSelectedPos == bindingAdapterPosition) {
                binding.tvDesc.visible()
                binding.ivExpand.setImageResource(R.drawable.ic_hs_collapse)
            } else {
                binding.ivExpand.setImageResource(R.drawable.ic_hs_expand)
                binding.tvDesc.gone()
            }
            binding.root.setOnClickListener {
                val lastPos = lastSelectedPos
                lastSelectedPos = bindingAdapterPosition
                if(lastPos==lastSelectedPos){
                    lastSelectedPos = -1
                    notifyItemChanged(lastPos)
                }else{
                    notifyItemChanged(lastPos)
                    notifyItemChanged(lastSelectedPos)
                }

            }

        }
    }
    fun fromHtml(source: String?): Spanned? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(source)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = OreoHsQuestionariesItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(view)

    }

    override fun getItemCount() = mDataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataset[position])
    }

    fun setData(dataList: ArrayList<OHSQuestionariesResponseModel>) {
        mDataset.clear()
        mDataset.addAll(dataList)
        notifyDataSetChanged()
    }

}