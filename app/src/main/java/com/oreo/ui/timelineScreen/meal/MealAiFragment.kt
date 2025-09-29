package com.oreo.ui.timelineScreen.meal

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.noisefit.luna.databinding.FragmentMealAiBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MealAiFragment : BaseFragment<FragmentMealAiBinding>(FragmentMealAiBinding::inflate) {

    private val viewModel: AiMealViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etInput.requestFocus()
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener { navigateUpSafe() }

        binding.btnSend.setOnClickListener {
            val text = binding.etInput.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                animateTextToTopAndFade()
                binding.etInput.clearFocus()
                binding.etInput.isEnabled = false
                hideKeyboard()
            }
        }

        binding.etInput.doAfterTextChanged {
            binding.btnSend.isEnabled = !it.isNullOrBlank()
            binding.btnSend.alpha = if (binding.btnSend.isEnabled) 1f else 0.4f
        }
    }

    override fun subscribeObservers() {

    }

    private  fun showAnalysingState(){

    }

    private fun animateTextToTopAndFade() {
        val root = binding.rootConstraint
        val text = binding.etInput.text?.toString().orEmpty()
        binding.tvTopText.text = text

        binding.ivMeal.visible()
        binding.tvTopText.visibility = View.INVISIBLE

        root.post {
            val locRoot = IntArray(2)
            val locEt = IntArray(2)
            val locTop = IntArray(2)
            root.getLocationOnScreen(locRoot)
            binding.etInput.getLocationOnScreen(locEt)
            binding.tvTopText.getLocationOnScreen(locTop)

            val startX = (locEt[0] - locRoot[0]).toFloat()
            val startY = (locEt[1] - locRoot[1]).toFloat()
            val endX = (locTop[0] - locRoot[0]).toFloat()
            val endY = (locTop[1] - locRoot[1]).toFloat()

            val floating = android.widget.TextView(requireContext()).apply {
                setText(binding.etInput.text.toString())
                setTextColor(resources.getColor(android.R.color.white))
                textSize = 16f
                alpha = 1f
            }

            val lp = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                binding.etInput.width,
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            root.addView(floating, lp)
            floating.x = startX
            floating.y = startY

            listOf(binding.tvHeader, binding.tvSub, binding.cardInput).forEach { v ->
                v.animate().alpha(0f).setDuration(250).start()
            }

            floating.animate()
                .x(endX)
                .y(endY)
                .setDuration(450)
                .withEndAction {
                    root.removeView(floating)
                    binding.tvTopText.alpha = 1f
                    binding.tvTopText.visible()
                    //binding.tvTopText.animate().alpha(1f).setDuration(150).start()
                    showAnalysingState()
                    viewModel.getNutritionFromText(text)
                }
                .start()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
