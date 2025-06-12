package com.oreo.ui.chatGpt.functions

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAiWorkoutPlanBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiMealPlanFragment :
    BaseFragment<FragmentAiWorkoutPlanBinding>(FragmentAiWorkoutPlanBinding::inflate) {

    private val viewModel: MealPlanViewModel by viewModels()

    private val nutrientsAdapter: NutrientsAdapter by lazy {
        NutrientsAdapter()
    }

    private val mealsAdapter: MealsAdapter by lazy {
        MealsAdapter(onMealSelected = { view,meal, mealName ->
           /* findNavController().navigate(R.id.aiMealDetailFragment,
                args = bundleOf("meal" to meal, "mealName" to mealName),
                navOptions = null,
                navigatorExtras = FragmentNavigatorExtras(view to "sharedImage_"))*/

            navigate(R.id.aiMealDetailFragment, bundleOf("meal" to meal, "mealName" to mealName))
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_nutrition_plan)
        binding.toolbar.tvTitle.setTextColor(Color.parseColor("#8ACA88"))
        viewModel.getMealPlans()

        setRecycler()
    }

    private fun setUi(){
        binding.btnSwitch.post {
            binding.btnSwitch.paint.shader = viewModel.getTextShaderForGradient(
                binding.btnSwitch.measuredWidth.toFloat(),
                "#AAADFF".toColorInt(),
                "#FFFFFF".toColorInt()
            )
        }
    }

    private fun displayComfortFood(){
        binding.lytCreateComfortFood.apply {
            tvTitle.text = getString(R.string.text_comfort_food_for_you)
            tvTitle.post {
                tvTitle.paint.shader = viewModel.getTextShaderForGradient(
                    tvTitle.measuredWidth.toFloat(),
                        "#9BC5FF".toColorInt(),
                        "#FFFFFF".toColorInt()
                    )
            }

            tvDesc.text = getString(R.string.text_plan_nourishing_meals_to_help_you_feel_your_best)

            btnCreate.post{
                btnCreate.paint.shader = viewModel.getTextShaderForGradient(
                    btnCreate.measuredWidth.toFloat(),
                    "#9E93FF".toColorInt(),
                    "#FFFFFF".toColorInt()
                )
            }

            root.visible()
        }
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

        /*  binding.ivMic.setOnClickListener {
              val (frag, bundle) = AudioAiFragment.getStartData(
                  PlanType.DIET
              )
              navigate(frag, bundle)
          }*/

        binding.ivEdit.setOnClickListener {
            navigate(
                AiMealPlanFragmentDirections.actionAiMealPlanFragmentToChatGptFragment(
                    "",
                    "",
                    getString(R.string.text_build_me_a_weekly_diet_plan),
                    "",
                    AITopics.GENERAL,
                    PlanType.DIET
                )
            )
        }

        binding.btnSwitch.setOnClickListener {
            val isRegularState = viewModel.dietState.value==MealPlanViewModel.DietState.REGULAR
            val nextState = if (isRegularState) MealPlanViewModel.DietState.COMFORT
                            else MealPlanViewModel.DietState.REGULAR
            viewModel.dietState.postValue(nextState)
        }
    }

    override fun subscribeObservers() {

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.selectedPosition.observe(this) {
            showSelected(it)
        }

        viewModel.dayNutrients.observe(this) {
            nutrientsAdapter.setDataSet(it)
        }

        viewModel.dayMealList.observe(this) {
            mealsAdapter.setDataSet(it ?: ArrayList())
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

        viewModel.dietState.observe(this){
            val isLowDietPlanAndWorkout = viewModel.localDataStore.isLowDietPlanAndWorkout()
            val isLowDietPlanAndWorkoutSetUp = viewModel.localDataStore.isLowDietPlanAndWorkoutSetUp()
            if(
                isLowDietPlanAndWorkout &&
                isLowDietPlanAndWorkoutSetUp
                )
            {
                binding.btnSwitch.visible()
            }else{
                binding.btnSwitch.gone()
            }

            when(it){
                MealPlanViewModel.DietState.REGULAR -> {
                    if(isLowDietPlanAndWorkoutSetUp){
                        binding.btnSwitch.visible()
                    }else{
                        binding.lytCreateComfortFood.root.visible()
                        binding.btnSwitch.gone()
                    }
                    viewModel.getMealPlans()
                }
                MealPlanViewModel.DietState.COMFORT -> {
                    if(isLowDietPlanAndWorkoutSetUp){
                        viewModel.getComfortMealPlans()
                    }else{
                        binding.lytCreateComfortFood.root.gone()
                        binding.btnSwitch.gone()
                        viewModel.getMealPlans()
                    }
                }
            }
        }

        viewModel.boosterFood.observe(this){
            if(it==null){
                binding.lytBoosterFoods.root.gone()
            }else{
                // Code for booster food - set ui
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