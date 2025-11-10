package com.oreo.ui.lifeos.onboarding.quesChildFrags

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemLifeosOnboardCheckboxTextBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.lifeos.onboarding.AnswerX
import com.oreo.ui.lifeos.onboarding.LifeOsOnboardingQuesViewModel.States

class AnswersWithCheckboxAdapter(
    private val isOther: (AnswerX) -> Boolean = { it.addOntext=="1" },
    private val isNone:  (AnswerX) -> Boolean = { it.text.equals("None",  ignoreCase = true) },
    private val onSelectionChanged: ((List<AnswerX>) -> Unit)? = null
) : RecyclerView.Adapter<AnswersWithCheckboxAdapter.VH>() {
    private val items = mutableListOf<AnswerX>()

    inner class VH(val binding: ItemLifeosOnboardCheckboxTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentWatcher: TextWatcher? = null

        fun bind(position: Int) {
            val item = items[position]
            with(binding) {
                tvText.text = item.text

                if(item.isSelected){
                    ivCheckBox.setImageResource(R.drawable.ic_checked_lifeos_onboard)
                }else{
                    ivCheckBox.setImageResource(R.drawable.ic_lifeos_onboard_chechbox_empty)
                }

                if(item.state != States.OTHER){
                    binding.lytInputField.root.gone()
                }else{
                    if (currentWatcher==null) {
                        val watcher = object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }

                            override fun afterTextChanged(s: Editable?) {
                                val p = bindingAdapterPosition
                                if (p != RecyclerView.NO_POSITION) {
                                    items[p].userInputText = s?.toString()
                                    onSelectionChanged?.invoke(items)
                                }
                            }
                        }
                        binding.lytInputField.descInputLayout.addTextChangedListener(watcher)
                        currentWatcher = watcher
                    }
                }

                lytCheckBox.setOnClickListener { onRowClick(position, item.state) }
            }
        }

        private fun onRowClick(position: Int, state: States?) {
            if (position !in 0..(items.size-1)) return
            val clicked = items[position]

            when(state){
                States.OTHER -> {
                    val nowSelected = !clicked.isSelected
                    val et = binding.lytInputField.descInputLayout
                    clicked.isSelected = nowSelected
                    notifyItemChanged(position)

                    if(nowSelected){
                        setNoneItemUnselected()
                        binding.lytInputField.root.visible()
                        if (!et.hasFocus()) et.requestFocus()
                        showKeyboard(et)
                    }else{
                        et.clearFocus()
                        hideKeyboard(et)
                        binding.lytInputField.root.gone()
                    }
                }

                States.NONE -> {
                    val view = binding.root
                    if(isKeyboardVisible(view)){
                        hideKeyboard(view)
                    }
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
                    val view = binding.root
                    if(isKeyboardVisible(view)){
                        hideKeyboard(view)
                    }

                    val nowSelected = !clicked.isSelected
                    if(nowSelected){
                        setNoneItemUnselected()
                    }
                    clicked.isSelected = nowSelected
                    notifyItemChanged(position)
                }

            }

            onSelectionChanged?.invoke(items)
        }

        private fun setNoneItemUnselected(){
            val noneItem = items.indexOfFirst {
                it.state == States.NONE && it.isSelected
            }
            if(noneItem == -1) return
            items[noneItem].isSelected = false
            notifyItemChanged(noneItem)
        }

        private fun showKeyboard(view: View) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            view.post { imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT) }
        }

        private fun hideKeyboard(view: View) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        private fun isKeyboardVisible(view: View): Boolean {
            val insets = ViewCompat.getRootWindowInsets(view) ?: return false
            return insets.isVisible(WindowInsetsCompat.Type.ime())
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