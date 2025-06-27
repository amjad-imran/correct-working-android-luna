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
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import com.oreo.ui.chatGpt.functions.MealPlanViewModel.DietState
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class AiMealPlanFragment :
    BaseFragment<FragmentAiWorkoutPlanBinding>(FragmentAiWorkoutPlanBinding::inflate) {

    private val viewModel: MealPlanViewModel by viewModels()

    private val nutrientsAdapter: NutrientsAdapter by lazy {
        NutrientsAdapter()
    }

    val currentDay = LocalDate.now().dayOfWeek.value;

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
        if(viewModel.dayMealList.value == null){
            viewModel.getMealPlans()
        }
        else{
            viewModel.setSelectedPosition(viewModel.selectedPosition.value ?: LocalDate.now().dayOfWeek.value)
        }


        setRecycler()
    }

    /*private fun setUi(){
        binding.btnSwitch.post {
            binding.btnSwitch.paint.shader = viewModel.getTextShaderForGradient(
                binding.btnSwitch.measuredWidth.toFloat(),
                "#AAADFF".toColorInt(),
                "#FFFFFF".toColorInt()
            )
        }
    }*/

    private fun displayComfortFoodCreationLyt(){
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
                    btnCreate.paint.measureText(btnCreate.text.toString()),
                    "#9E93FF".toColorInt(),
                    "#FFFFFF".toColorInt()
                )
                btnCreate.invalidate()
            }
            btnDismiss.text= getString(R.string.text_dismiss)
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
            val curDay = LocalDate.now().dayOfWeek.value
            val isRegularState =
                viewModel.dietState.value == DietState.REGULAR || viewModel.dietState.value == DietState.NORMAL
            if (isRegularState){
                viewModel.dietState.value = DietState.COMFORT
            }
            else{
                viewModel.dietState.value = DietState.REGULAR
            }
            viewModel.setSelectedPosition(curDay)
        }

        binding.lytCreateComfortFood.btnCreate.setOnClickListener {
            viewModel.getComfortMealPlans()
        }

        binding.lytCreateComfortFood.btnDismiss.setOnClickListener {
            viewModel.localDataStore.setIsLowDietPlanSetUp(false)
            binding.lytCreateComfortFood.root.gone()
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

            aisehi(it.second)
            LOGS.d("yashhhhhhhh : $it")
            mealsAdapter.setDataSet(it.first ?: ArrayList(), it.second)
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


        viewModel.currentDayBoosterMeals.observe(this){
            it?.let {
                /*viewModel.selectedPosition*/
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

    private fun aisehi(dietState: DietState){
        LOGS.d("ibvldskvsdjvn : $dietState")
        when(dietState){
            DietState.REGULAR -> {
                binding.root.setBackgroundResource(R.drawable.back_ai_nutrition)
                binding.toolbar.lytComfortTag.gone()
                binding.toolbar.tvTitle.setTextColor("#8ACA88".toColorInt())
                binding.btnSwitch.apply {
                    text = getString(R.string.text_switch_to_comfort_diet)
                    post {
                        paint.shader = viewModel.getTextShaderForGradient(
                            binding.btnSwitch.measuredWidth.toFloat(),
                            "#AAADFF".toColorInt(),
                            "#FFFFFF".toColorInt()
                        )
                    }
                }

                val isComfortDietSetup = viewModel.localDataStore.isLowDietPlanSetUp()
                /*
                null -> not setup
                true -> setup Done Already
                false -> setup dismiss
                */
                if(isComfortDietSetup == null){
                    displayComfortFoodCreationLyt()
                    binding.btnSwitch.gone()
                }
                else if(!isComfortDietSetup.isSetup){
                    binding.lytCreateComfortFood.root.gone()
                    binding.btnSwitch.gone()
                }else{
                    binding.lytCreateComfortFood.root.gone()
                    binding.btnSwitch.visible()
                }
            }


            DietState.COMFORT -> {
                binding.root.setBackgroundResource(R.drawable.back_ai_nutrition_comfort)

                binding.toolbar.apply {
                    tvTitle.setTextColor("#88A0CA".toColorInt())
                    tvComfortTag.apply {
                        text = getString(R.string.text_comfort)
                        paint.shader = viewModel.getTextShaderForGradient(
                            this.measuredWidth.toFloat(),
                            "#AAADFF".toColorInt(),
                            "#FFFFFF".toColorInt()
                        )
                    }
                    lytComfortTag.visible()
                }
                binding.lytCreateComfortFood.root.gone()
                binding.btnSwitch.apply {
                    text = getString(R.string.text_switch_to_regular_diet)
                    setTextColor("#E9E9E9".toColorInt())
                    visible()
                }
            }


            DietState.NORMAL -> {
                binding.root.setBackgroundResource(R.drawable.back_ai_nutrition)
                binding.toolbar.lytComfortTag.gone()
                binding.toolbar.tvTitle.setTextColor("#8ACA88".toColorInt())
                binding.lytCreateComfortFood.root.gone()
                binding.btnSwitch.gone()
            }
        }
    }

}