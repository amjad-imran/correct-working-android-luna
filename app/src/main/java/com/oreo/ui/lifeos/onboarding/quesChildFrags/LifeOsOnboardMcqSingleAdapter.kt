package com.oreo.ui.lifeos.onboarding.quesChildFrags

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemLifeosOnboardCheckboxTextBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.data.model.lifeos.onboarding.AnswerX
import com.noisefit_commans.data.model.lifeos.onboarding.LifeOSOnboardMCQquesStates

class LifeOsOnboardMcqSingleAdapter(val mListener: OnMcqItemClicked) :
    RecyclerView.Adapter<LifeOsOnboardMcqSingleAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<AnswerX>()

    inner class ViewHolder(val binding: ItemLifeosOnboardCheckboxTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AnswerX) {

            binding.tvText.text = data.text
            binding.ivCheckBox.setImageResource(
                if (data.isSelected) R.drawable.ic_checked_lifeos_onboard
                else R.drawable.ic_lifeos_onboard_chechbox_empty
            )

            if(data.state== LifeOSOnboardMCQquesStates.OTHER){
                binding.lytInputField.root.setVisibilityByCondition(data.isSelected)

                val watcher = object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        val p = bindingAdapterPosition
                        if (p != RecyclerView.NO_POSITION) {
                            mDataSet[p].userInputText = s?.toString()
                        }
                    }
                }
            }else{
                binding.lytInputField.root.gone()
            }

            binding.lytCheckBox.setOnClickListener {
                mListener.onItemClick(data, bindingAdapterPosition)
                notifyDataSetChanged()
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemLifeosOnboardCheckboxTextBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun getSelectedValue(): AnswerX? {
        mDataSet.forEach {
            if (it.isSelected) {
                return it
            }
        }
        return null
    }

    fun setData(resultData: List<AnswerX>?) {
        mDataSet.clear()
        notifyDataSetChanged()
        if (resultData != null) {
            mDataSet.addAll(resultData)
        }
        notifyDataSetChanged()
    }

    fun updateItem(data: AnswerX, position: Int) {
        mDataSet.forEachIndexed { index, answerModel ->
            if (index == position) {
                answerModel.isSelected = !data.isSelected
            } else {
                answerModel.isSelected = false
            }
        }
        notifyDataSetChanged()
//        mDataSet[position].isChecked = !data.isChecked
//        notifyItemChanged(position)
    }


}

interface OnMcqItemClicked {
    fun onItemClick(data: AnswerX, position: Int)
}