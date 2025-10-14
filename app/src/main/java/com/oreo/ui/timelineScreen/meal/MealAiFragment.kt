package com.oreo.ui.timelineScreen.meal

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.noisefit_commans.data.model.timeline.MealAiFoods
import com.noisefit_commans.data.model.timeline.MealAiMacros
import com.noisefit_commans.data.model.timeline.MealAiResponse
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMealAiBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.getValue
import androidx.core.view.isVisible
import com.moengage.core.internal.utils.showToast

@AndroidEntryPoint
class MealAiFragment : BaseFragment<FragmentMealAiBinding>(FragmentMealAiBinding::inflate) {

    private val viewModel: AiMealViewModel by viewModels()
    private var foodAdapter: FoodAdapter? = null
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    private val navArgs: MealAiFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mealData = navArgs.mealData
        if (mealData != null) {

            val date = mealData.date
            if (date?.equals(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ) == true
            ) {
                viewModel.editMode = true
            } else {
                viewModel.viewMode = true
            }

            listOf(binding.tvHeader, binding.tvSub, binding.cardInput, binding.imageGreenBottomGlow).forEach { v ->
                v.alpha = 0f
                v.gone()
            }

            viewModel.mealAiResponse.postValue(mealData)
            val time = LocalTime.parse(mealData.time)
            viewModel.mealTime.postValue(time)

            viewModel.lastEnteredPrompt = mealData.prompt
            binding.ivMeal.visible()
            binding.tvTopText.visible()
            binding.imageGradientTop.visible()
            binding.tvTopText.alpha = 1f
            binding.tvTopText.text = mealData.prompt
            binding.ivEditMeal.visibility = View.INVISIBLE
            binding.textResult.gone()
            binding.tvRetryPrompt.gone()
            if (viewModel.editMode) {
                binding.ivDelete.visible()
                binding.lytContent.btnSaveMeal.visible()
            }
            if (viewModel.viewMode) {
                binding.lytContent.btnSaveMeal.gone()
                binding.tvTitle.text = getString(R.string.text_meal_intake)
            }
        } else {
            binding.etInput.requestFocus()
        }
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener { navigateUpSafe() }

        binding.ivDelete.setOnClickListener {
            viewModel.deleteMeal()
        }

        binding.lytContent.tvAddFood.setOnClickListener {
            showAddFoodSheet()
        }
        binding.lytContent.tvTime.setOnClickListener {
            if (viewModel.viewMode) return@setOnClickListener

            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                val time = LocalTime.of(hourOfDay, minute)
                if (time > LocalTime.now()) {
                    context.showShortToast("Time cannot be in future")
                    return@setFragmentResultListener
                }

                viewModel.mealTime.postValue(time)

            }


            navigate(
                R.id.timeBottomSheet,
                bundleOf(
                    "hour" to viewModel.mealTime.value!!.hour,
                    "minute" to viewModel.mealTime.value!!.minute,
                    "hourOther" to 0,
                    "minuteOther" to 0,
                    "isStart" to 1,
                    "unitPosition" to 1,
                    "title" to getString(R.string.text_time)
                )
            )

        }

        binding.lytContent.btnSaveMeal.setOnClickListener {
            val foods = foodAdapter?.getItems()

            if (foods.isNullOrEmpty()) {
                context.showShortToast(getString(R.string.text_add_food_item))
                return@setOnClickListener
            }


            viewModel.saveMeal(foods)
        }

        binding.lytContent.lytMacrosHeader.setOnClickListener { toggleMacros() }
        binding.lytContent.ivMacrosArrow.setOnClickListener { toggleMacros() }


        binding.tvRetryPrompt.setOnClickListener {
            onEditClicked()

            /*viewModel.mealAiResponse.value = null
            showAnalysingState()
            viewModel.lastEnteredPrompt?.let {
                viewModel.getNutritionFromText(it)
            }*/
        }

        binding.ivEditMeal.setOnClickListener {
            if (viewModel.mealAiResponse.value == null) return@setOnClickListener
            onEditClicked()
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

    fun onEditClicked(){
        viewModel.mealAiResponse.value = null
        binding.imageGradientTop.gone()
        binding.tvTopText.gone()
        binding.ivEditMeal.gone()
        binding.ivMeal.gone()

        binding.tvHeader.visible()
        binding.tvSub.visible()
        binding.cardInput.visible()
        binding.imageGreenBottomGlow.visible()

        listOf(binding.tvHeader, binding.tvSub, binding.cardInput, binding.imageGreenBottomGlow).forEach { v ->
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

    override fun subscribeObservers() {
        viewModel.mealAiResponse.observe(this) {
            if (it == null) {
                binding.svMain.gone()
            } else {
                binding.tvAnalysing.gone()

                if(it.foods.isNullOrEmpty()){
                    showToast(requireContext(),
                        getString(R.string.text_no_food_items_found_please_try_again))
                    onEditClicked()
                    return@observe
                }
                showDataState(it)
            }
        }
        viewModel.mealTime.observe(this) {
            binding.lytContent.tvTime.text = it.format(DateTimeFormatter.ofPattern("h:mma"))
            setMealType(it)
        }
        viewModel.onAddSuccess.observe(this) {
            it.getContent()?.let {
                mainViewModel.sessionManager.reloadOnResume = true

                navigateUpSafe()
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

    }

    private fun setMealType(time: LocalTime) {
        val selector = binding.lytContent.lytMealTypeSelector

        val tvBreakfast = selector.tvBreakfast
        val tvLunch = selector.tvLunch
        val tvDinner = selector.tvDinner
        val tvSnack = selector.tvSnack

        fun reset() {
            listOf(tvBreakfast, tvLunch, tvDinner, tvSnack).forEach {
                it.setBackgroundResource(0)
                it.setTextColor(android.graphics.Color.parseColor("#969696"))
            }
        }

        val breakfastStart = LocalTime.of(7, 0)
        val breakfastEnd = LocalTime.of(11, 0)
        val lunchStart = LocalTime.of(12, 0)
        val lunchEnd = LocalTime.of(15, 0)
        val dinnerStart = LocalTime.of(18, 0)
        val dinnerEnd = LocalTime.of(22, 0)

        reset()

        val target = when {
            !time.isBefore(breakfastStart) && !time.isAfter(breakfastEnd) -> tvBreakfast
            !time.isBefore(lunchStart) && !time.isAfter(lunchEnd) -> tvLunch
            !time.isBefore(dinnerStart) && !time.isAfter(dinnerEnd) -> tvDinner
            else -> tvSnack
        }

        target.setBackgroundResource(R.drawable.back_selected_meal_type)
        target.setTextColor(android.graphics.Color.WHITE)
    }

    private fun showDataState(data: MealAiResponse) {
        binding.svMain.visible()

        val mealBinding = binding.lytContent

        mealBinding.rvFoods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            foodAdapter = FoodAdapter(viewModel.viewMode) { items ->
                updateTotalCalories(items)
                if ((foodAdapter?.itemCount ?: 0) >= 10) {
                    binding.lytContent.tvAddFood.gone()
                } else {
                    binding.lytContent.tvAddFood.visible()
                }
            }
            adapter = foodAdapter
        }

        foodAdapter?.setData(data.foods ?: ArrayList())

        if ((data.foods?.size ?: 0) >= 10 || viewModel.viewMode) {
            binding.lytContent.tvAddFood.gone()
        } else {
            binding.lytContent.tvAddFood.visible()
        }

        mealBinding.tvDate.text = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM"))

        updateTotalCalories(foodAdapter?.getItems().orEmpty())

        populateMacros(data.macros)
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

            listOf(binding.tvHeader, binding.tvSub, binding.cardInput, binding.imageGreenBottomGlow).forEach { v ->
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
                    binding.imageGradientTop.visible()
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
            override fun onFoodAdded(name: String, calories: Int) {
                foodAdapter?.addItem(MealAiFoods(name, calories))
                if ((foodAdapter?.itemCount ?: 0) >= 10) {
                    binding.lytContent.tvAddFood.gone()
                } else {
                    binding.lytContent.tvAddFood.visible()
                }
            }
        }
        sheet.show(childFragmentManager, "AddFoodBottomSheet")
    }

    private fun updateTotalCalories(items: List<MealAiFoods>) {
        val total = items.mapNotNull { it.calories }.sum()
        binding.lytContent.tvTotalCalories.text = "${total}Kcal"
    }

    private fun populateMacros(macros: List<MealAiMacros>?) {
        val mealBinding = binding.lytContent
        val items = macros.orEmpty()
        val container = mealBinding.llMacrosItems
        container.removeAllViews()

        if (items.isEmpty()) {
            mealBinding.lytMacrosHeader.gone()
            mealBinding.lytMacrosContent.gone()
            mealBinding.viewMacrosDivider.gone()
            return
        } else {
            mealBinding.lytMacrosHeader.visible()
            mealBinding.lytMacrosContent.visible()
            if (viewModel.viewMode) {
                mealBinding.viewMacrosDivider.gone()
            } else {
                mealBinding.viewMacrosDivider.visible()
            }
            mealBinding.ivMacrosArrow.rotation = 180f
        }

        items.forEachIndexed { index, it ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (index == 0) 8.dp else 12.dp
                }
            }

            val tvName = TextView(requireContext()).apply {
                TextViewCompat.setTextAppearance(this, com.noisefit_commans.R.style.S12)
                setTextColor(android.graphics.Color.parseColor("#A3FFFFFF"))
                text = it.name.orEmpty()
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvValue = TextView(requireContext()).apply {
                TextViewCompat.setTextAppearance(this, com.noisefit_commans.R.style.S12)
                setTextColor(android.graphics.Color.parseColor("#FFFFFFFF"))
                text = it.value.orEmpty()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            row.addView(tvName)
            row.addView(tvValue)
            container.addView(row)
        }
    }

    private fun toggleMacros() {
        val mealBinding = binding.lytContent
        val isVisible = mealBinding.lytMacrosContent.isVisible
        val transition = AutoTransition().apply { duration = 200 }
        TransitionManager.beginDelayedTransition(mealBinding.root as ViewGroup, transition)
        mealBinding.lytMacrosContent.visibility = if (isVisible) View.GONE else View.VISIBLE
        val target = if (isVisible) 0f else 180f
        mealBinding.ivMacrosArrow.animate().rotation(target).setDuration(200).start()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
