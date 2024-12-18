package com.oreo.ui.chatGpt.functions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAiWorkoutPlanBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiMealPlanFragment :
    BaseFragment<FragmentAiWorkoutPlanBinding>(FragmentAiWorkoutPlanBinding::inflate) {

    private val viewModel: MealPlanViewModel by viewModels()

    private val nutrientsAdapter: NutrientsAdapter by lazy {
        NutrientsAdapter()
    }

    private val mealsAdapter: MealsAdapter by lazy {
        MealsAdapter(onMealSelected = { meal ->
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                null,
                null,
                AITopics.GENERAL,
                meal
            )
            navigate(frag, bundle)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_diet_plan)
        viewModel.getMealPlans()

        setRecycler()
    }


    override fun initListener() {
        binding.lytWeek.tvMon.setOnClickListener(weekListener)
        binding.lytWeek.tvTue.setOnClickListener(weekListener)
        binding.lytWeek.tvWed.setOnClickListener(weekListener)
        binding.lytWeek.tvThu.setOnClickListener(weekListener)
        binding.lytWeek.tvFri.setOnClickListener(weekListener)
        binding.lytWeek.tvSat.setOnClickListener(weekListener)
        binding.lytWeek.tvSun.setOnClickListener(weekListener)

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvEditDietPlan.setOnClickListener {
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                null,
                null,
                AITopics.GENERAL
            )
            navigate(frag, bundle)
        }

    }

    override fun subscribeObservers() {

        viewModel.selectedPosition.observe(this) {
            showSelected(it)
        }

        viewModel.dayNutrients.observe(this) {
            nutrientsAdapter.setDataSet(it)
        }

        viewModel.dayMealList.observe(this) {
            mealsAdapter.setDataSet(it)
        }


        viewModel.currentSelectedWeekDayPosition.observe(this) { selectedPos ->
            val main = binding.lytWeek

            val views = arrayListOf(
                main.selection1,
                main.selection2,
                main.selection3,
                main.selection4,
                main.selection5,
                main.selection6,
                main.selection7
            )
            views.forEach {
                if (it.tag.toString().toInt() == selectedPos) {
                    it.visible()
                } else {
                    it.invisible()
                }
            }

        }

    }

    private fun setRecycler() {
        binding.rvNutrients.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvNutrients.adapter = nutrientsAdapter

        binding.rvMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeals.adapter = mealsAdapter
    }


    private val weekListener = View.OnClickListener { v ->
        val tag = v?.tag?.toString()?.toIntOrNull()
        tag?.let {
            viewModel.setSelectedPosition(it)
        }
    }

    private fun showSelected(selectedTag: Int) {
        val main = binding.lytWeek
        val layouts = arrayListOf(
            main.tvMon,
            main.tvTue,
            main.tvWed,
            main.tvThu,
            main.tvFri,
            main.tvSat,
            main.tvSun
        )
        layouts.forEach {
            if (it.tag.toString().toInt() == selectedTag) {
                it.setBackgroundResource(R.drawable.bg_week_selected)
            } else {
                it.setBackgroundResource(0)
            }
        }
    }
}