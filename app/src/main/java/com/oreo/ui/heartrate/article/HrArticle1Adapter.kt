package com.oreo.ui.heartrate.article

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowHrArticlePointsBinding


class HrArticle1Adapter(val dataSet: List<HrArticlePoint>) :
    RecyclerView.Adapter<HrArticle1Adapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowHrArticlePointsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: HrArticlePoint) {
            binding.tvNumber.text = data.pointText

            if (data.highlightString == null) {
                binding.tvContent.text = data.message
            } else {
                try {
                    val spannableStringBuilder = SpannableStringBuilder(data.message)
                    val start = data.message.indexOf(data.highlightString)
                    val end = start + data.highlightString.length
                    spannableStringBuilder.setSpan(
                        ForegroundColorSpan(Color.WHITE),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    binding.tvContent.text = spannableStringBuilder
                }catch (exp:Exception){
                    binding.tvContent.text = data.message
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = RowHrArticlePointsBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }
}

data class HrArticlePoint(
    val pointText: String,
    val message: String,
    val highlightString: String? = null
)