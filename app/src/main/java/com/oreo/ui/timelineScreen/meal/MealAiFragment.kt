package com.oreo.ui.timelineScreen.meal

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.data.model.timeline.MealAiFoods
import com.noisefit.data.model.timeline.MealAiResponse
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMealAiBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MealAiFragment : BaseFragment<FragmentMealAiBinding>(FragmentMealAiBinding::inflate) {

    private val viewModel: AiMealViewModel by viewModels()
    private var foodAdapter: FoodAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etInput.requestFocus()

    }

    override fun initListener() {
        binding.ivBack.setOnClickListener { navigateUpSafe() }

        binding.lytContent.tvAddFood.setOnClickListener {
            showAddFoodSheet()
        }

        binding.lytContent.btnSaveMeal.setOnClickListener {
            val foods = foodAdapter?.getItems()

            if(foods.isNullOrEmpty()) {
                context.showShortToast(getString(R.string.text_add_food_item))
                return@setOnClickListener
            }


            viewModel.saveMeal(foods)
        }


        binding.tvRetryPrompt.setOnClickListener {
            viewModel.mealAiResponse.value = null
            showAnalysingState()
            viewModel.lastEnteredPrompt?.let {
                viewModel.getNutritionFromText(it)
            }
        }

        binding.ivEditMeal.setOnClickListener {
            if(viewModel.mealAiResponse.value == null) return@setOnClickListener

            viewModel.mealAiResponse.value = null
            binding.tvTopText.gone()
            binding.ivEditMeal.gone()
            binding.ivMeal.gone()

            binding.tvHeader.visible()
            binding.tvSub.visible()
            binding.cardInput.visible()

            listOf(binding.tvHeader, binding.tvSub, binding.cardInput).forEach { v ->
                v.animate().alpha(1f).setDuration(250).start()
            }

            binding.etInput.setText(viewModel.lastEnteredPrompt)

            binding.etInput.isEnabled = true
            binding.etInput.requestFocus()
            binding.etInput.setSelection(binding.etInput.text.length)
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etInput, InputMethodManager.SHOW_IMPLICIT)
            binding.etInput.setSelection(binding.etInput.text.length)

        }

        binding.btnSend.setOnClickListener {
            val text = binding.etInput.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                viewModel.lastEnteredPrompt = text

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
        viewModel.mealAiResponse.observe(this) {
            if (it == null) {
                binding.svMain.gone()
            } else {
                showDataState(it)
            }
        }
    }

    private fun showDataState(data: MealAiResponse) {
        binding.tvAnalysing.gone()
        binding.svMain.visible()

        val mealBinding = binding.lytContent

        mealBinding.rvFoods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            foodAdapter = FoodAdapter { items ->
                updateTotalCalories(items)
            }
            adapter = foodAdapter
        }

        foodAdapter?.setData(data.foods ?: ArrayList())

        mealBinding.tvTime.text = data.time ?: ""
        mealBinding.tvDate.text = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM"))

        /* when (data.mealType?.lowercase()){
             "breakfast" -> mealBinding?.cgMealType?.check(mealBinding?.chipBreakfast?.id ?: -1)
             "lunch" -> mealBinding?.cgMealType?.check(mealBinding?.chipLunch?.id ?: -1)
             "dinner" -> mealBinding?.cgMealType?.check(mealBinding?.chipDinner?.id ?: -1)
             "snack" -> mealBinding?.cgMealType?.check(mealBinding?.chipSnack?.id ?: -1)
         }*/

        updateTotalCalories(foodAdapter?.getItems().orEmpty())
    }

    private fun showAnalysingState() {
        binding.tvAnalysing.visible()
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
                    binding.ivEditMeal.visible()
                    //binding.tvTopText.animate().alpha(1f).setDuration(150).start()
                    showAnalysingState()
                    viewModel.getNutritionFromText(text)
                }
                .start()
        }
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showAddFoodSheet() {
        val sheet = AddFoodBottomSheet()
        sheet.callback = object : AddFoodBottomSheet.Callback {
            override fun onFoodAdded(name: String) {
                foodAdapter?.addItem(MealAiFoods(name, null))
            }
        }
        sheet.show(childFragmentManager, "AddFoodBottomSheet")
    }

    private fun updateTotalCalories(items: List<MealAiFoods>) {
        val total = items.mapNotNull { it.calories }.sum()
        binding.lytContent.tvTotalCalories.text = "${total}Kcal"
    }
}
