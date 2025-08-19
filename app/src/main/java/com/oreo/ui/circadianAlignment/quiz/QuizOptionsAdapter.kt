package com.oreo.ui.circadianAlignment.quiz

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemOptionQuizCircadianBinding
import com.oreo.data.model.circadian.CircadianQuizOptionsModel

class QuizOptionsAdapter(
    prevSelectedOption: Int?,
    val onOptionSelected: (CircadianQuizOptionsModel) -> Unit
):
    RecyclerView.Adapter<QuizOptionsAdapter.QuizOptionsViewHolder>() {

    private val mList: ArrayList<CircadianQuizOptionsModel> = ArrayList()
    private var selectedOption: Int? = prevSelectedOption

    inner class QuizOptionsViewHolder(val binding: ItemOptionQuizCircadianBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(option: CircadianQuizOptionsModel, pos: Int) {
            val context = binding.root.context

            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.bg_option_quiz_circadian) as GradientDrawable
            if (selectedOption==option.id) {
                backgroundDrawable.setStroke(2.dpToPx(), "#99CFA6FF".toColorInt())
            } else {
                backgroundDrawable.setStroke(1.dpToPx(), "#0FFFFFFF".toColorInt())
            }

            binding.root.background = backgroundDrawable
            binding.tvOption.text = option.text ?: ""

            binding.root.setOnClickListener {
                val prevSelected = selectedOption
                selectedOption = option.id
                prevSelected?.let {sO ->
                    val prev = mList.indexOfFirst { obj -> obj.id==sO }
                    if(prev != -1){
                        notifyItemChanged(prev)
                    }
                }
                notifyItemChanged(pos)
                onOptionSelected(option)
            }
        }

        fun Int.dpToPx(): Int {
            return (this * Resources.getSystem().displayMetrics.density).toInt()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizOptionsViewHolder {
        val binding = ItemOptionQuizCircadianBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuizOptionsViewHolder(binding)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: QuizOptionsViewHolder, position: Int) {
        holder.bind(mList[position], position)
    }

    fun updateDataSet(list: List<CircadianQuizOptionsModel>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

}