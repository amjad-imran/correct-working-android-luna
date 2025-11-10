package com.oreo.ui.lifeos.onboarding.quesChildFrags

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemLifeosOnboardCheckboxTextBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.lifeos.onboarding.AnswerX
import com.oreo.ui.lifeos.onboarding.LifeOsOnboardingQuesViewModel.States

class AnswersWithCheckboxAdapter(
    private val isOther: (AnswerX) -> Boolean = { it.addOntext=="1" },
    private val isNone:  (AnswerX) -> Boolean = { it.text.equals("None",  ignoreCase = true) },
    private val onSelectionChanged: ((List<AnswerX>) -> Unit)? = null
) : RecyclerView.Adapter<AnswersWithCheckboxAdapter.VH>() {

    // ✅ proper list
    private val items = mutableListOf<AnswerX>()

    inner class VH(val binding: ItemLifeosOnboardCheckboxTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentWatcher: TextWatcher? = null

        fun bind(position: Int) {
            val item = items[position]
            with(binding) {
                tvText.text = item.text

                if(item.isSelected){
                    ivCheckBox.setImageResource(R.drawable.ic_hm_check_mark)
                }else{
                    ivCheckBox.setImageResource(R.drawable.ic_lifeos_onboard_chechbox_empty)
                }

                // ✅ show/hide include
//                lytInputField.root.isVisible = item.state==States.OTHER && item.isSelected

                // Clicks anywhere on the row

                LOGS.d("akldncacstate : ${item.text}-${item.state}, addOntext-${item.addOntext}")

                lytCheckBox.setOnClickListener { onRowClick(position, item.state) }

                // --- EditText lookup (robust to either exposing edit directly or via TextInputLayout) ---
                val et: EditText? = when {
                    item.state == States.OTHER ->
                        binding.lytInputField.descInputLayout
                    else -> null
                }

                // ✅ remove previous watcher to avoid duplication on recycle
                currentWatcher?.let { et?.removeTextChangedListener(it) }
                currentWatcher = null

                // Pre-fill
                et?.setText(item.userInputText.orEmpty())
                et?.setSelection(et.text?.length ?: 0)

                // Attach watcher only when visible
                if (et != null && lytInputField.root.isVisible) {
                    val watcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val p = bindingAdapterPosition
                            if (p != RecyclerView.NO_POSITION) {
                                items[p].userInputText = s?.toString()
                                onSelectionChanged?.invoke(items)
                            }
                        }
                    }
                    et.addTextChangedListener(watcher)
                    currentWatcher = watcher
                }

                /*
                // ✅ keyboard handling: only for this holder
                if (isOther(item) && item.isSelected && et != null) {
                    if (!et.hasFocus()) et.requestFocus()
                    showKeyboard(et)
                } else {
                    // hide only if our holder owns the focus
                    if (et != null && et.isFocused) {
                        et.clearFocus()
                        hideKeyboard(et)
                    }
                }*/
            }
        }

        private fun onRowClick(position: Int, state: States?) {
            if (position !in 0..(items.size-1)) return
            val clicked = items[position]

            when(state){
                States.OTHER -> {
                    val nowSelected = !clicked.isSelected
                    val et = binding.lytInputField.descInputLayout
                    if(nowSelected){
                        binding.lytInputField.root.visible()
                        if (!et.hasFocus()) et.requestFocus()
                        showKeyboard(et)
                    }else{
                        et.clearFocus()
                        hideKeyboard(et)
                        binding.lytInputField.root.gone()
                    }

                    clicked.isSelected = nowSelected
                    // turn off None if it was on
                    val noneIndex = items.indexOfFirst { isNone(it) }
                    if (noneIndex >= 0 && items[noneIndex].isSelected) {
                        items[noneIndex].isSelected = false
                        notifyItemChanged(noneIndex)
                    }
                    notifyItemChanged(position)
                }

                States.NONE -> {
                    val nowSelected = !clicked.isSelected
                    if(nowSelected){
                        items.forEachIndexed { index, a ->
                            if(position!=index && a.isSelected) {
                                a.isSelected = false
                                notifyItemChanged(index)
                            }
                        }
                    }
                    clicked.isSelected = nowSelected
                    notifyItemChanged(position)
                }

                /*States.NORMAL,*/
                else -> {
                    val nowSelected = !clicked.isSelected
                    if(nowSelected){
                        items.filter {
                            it.state == States.NONE && it.isSelected
                        }.forEachIndexed { index, x ->
                            x.isSelected = false
                            notifyItemChanged(index)
                        }
                    }
                    clicked.isSelected = nowSelected
                    notifyItemChanged(position)
                }

            }

            onSelectionChanged?.invoke(items)
        }

        private fun showKeyboard(view: View) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            view.post { imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT) }
        }

        private fun hideKeyboard(view: View) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLifeosOnboardCheckboxTextBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getSelected(): List<AnswerX> {
        return items.filter { it.isSelected }
    }

    fun updateDataSet(newItems: List<AnswerX>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}