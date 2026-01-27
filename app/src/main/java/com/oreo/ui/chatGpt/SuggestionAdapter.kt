package com.oreo.ui.chatGpt

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowSuggestionChipBinding
import com.oreo.data.model.ai.TopQuestions

class SuggestionAdapter(
    private val recyclerView: RecyclerView,
    val onQuesClicked: (ques: String) -> Unit
) :
    RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<TopQuestions>()

    inner class ViewHolder(val binding: RowSuggestionChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun TextView.isEllipsized(): Boolean {
            val l = layout ?: return false
            val last = lineCount - 1
            if (last < 0) return false
            return l.getEllipsisCount(last) > 0
        }

        fun bind(data: TopQuestions) {
            binding.tvQues.text = data.question

            val rvWidth = recyclerView.width - recyclerView.paddingStart - recyclerView.paddingEnd

            if (rvWidth > 0) {
                val itemParent = binding.root
                val applyWidth = {
                    val minW = (rvWidth / 3f).toInt()
                    val maxW = (rvWidth * .41f).toInt()

                    fun setCardWidth(w: Int) {
                        itemParent.layoutParams = itemParent.layoutParams.apply { width = w }
                    }

                    // 1) Try min width first
                    setCardWidth(minW)

                    // Need layout pass to know if ellipsized
                    itemParent.post {
                        // If text is NOT ellipsized at min width => keep it
                        if (!binding.tvQues.isEllipsized()) return@post

                        // 2) Otherwise bump to max width
                        setCardWidth(maxW)
                    }
                }

                // If RV not measured yet, run after layout.
                if (recyclerView.width == 0) recyclerView.post { applyWidth() } else applyWidth()
            }

            binding.root.setOnClickListener {
                onQuesClicked(data.question?:"")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            RowSuggestionChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(it: List<TopQuestions>) {
        mDataSet.clear()
        mDataSet.addAll(it)
        notifyDataSetChanged()

    }

}