package com.oreo.ui.chatGpt.functions

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.noisefit.data.model.AiMeal
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAiMealDetailBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.audio.AudioAiFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiMealDetailFragment :
    BaseFragment<FragmentAiMealDetailBinding>(FragmentAiMealDetailBinding::inflate) {

    private val navArgs: AiMealDetailFragmentArgs by navArgs()
    private val TIMER_DURATION = 3000L

    //move to viewModel
    private var timer: CountDownTimer? = null
    private val messagesStrings = ArrayList<String>()
    private val displayMessage = MutableLiveData<String>()
    private var currentPos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding.lytMealDetails.root.transitionName = "sharedImage_" // Same as in RecyclerView
        //startPostponedEnterTransition()

        setUI(navArgs.meal, navArgs.mealName, navArgs.dietState)
    }


    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivTextChat.setOnClickListener {
            val ques = getWorkoutAiString(currentPos)
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                ques,
                null,
                AITopics.GENERAL
            )
            navigate(frag, bundle)
        }

        binding.ivMic.setOnClickListener {
            val ques = getWorkoutAiString(currentPos)
            val (frag, bundle) = AudioAiFragment.getStartData(
                PlanType.DIET,
                ques,
            )
            navigate(frag, bundle)
        }
    }

    override fun subscribeObservers() {
        displayMessage.observe(this) {
            if (it.isNotEmpty()) {
                binding.tvMessages.text = it
            }
        }

    }

    private fun setUI(
        meal: AiMeal,
        mealName: String,
        dietState: DietState,
    ) {

        when(dietState){
            DietState.REGULAR, DietState.NORMAL -> {

                binding.root.setBackgroundResource(R.drawable.back_ai_nutrition)

                binding.toolbar.tvTitle.text = mealName
                binding.toolbar.tvTitle.setTextColor(Color.parseColor("#8ACA88"))

                binding.lytComfortMealDetails.root.gone()
                binding.lytBoosterMealDetails.root.gone()
                binding.lytMealDetails.apply {
                    tvMealName.text = meal.meal_name
                    tvDescription.text = meal.description
                    tvNutrition.text = "${meal.portion} | ${meal.calories}"

                    lytNutritionData.apply {
                        tvProtein.text = if (meal.protein.isNullOrEmpty()) "-" else meal.protein
                        tvFibre.text = if (meal.fibre.isNullOrEmpty()) "-" else meal.fibre
                        tvFat.text = if (meal.fat.isNullOrEmpty()) "-" else meal.fat
                        tvCarbs.text = if (meal.carbohydrate.isNullOrEmpty()) "-" else meal.carbohydrate
                    }

                    root.visible()
                }
            }

            DietState.COMFORT -> {
                binding.root.setBackgroundResource(R.drawable.back_ai_nutrition_comfort)

                binding.toolbar.tvTitle.text = mealName
                binding.toolbar.tvTitle.setTextColor(Color.parseColor("#88A0CA"))

                binding.lytMealDetails.root.gone()
                binding.lytBoosterMealDetails.root.gone()
                binding.lytComfortMealDetails.apply {

                    tvMealName.text = meal.meal_name
                    tvDescription.text = meal.description
                    tvNutrition.text = "${meal.portion} | ${meal.calories}"

                    lytNutritionData.apply {

                        tvProtein.apply {
                            setTextColor("#00298F".toColorInt())
                            text = if (meal.protein.isNullOrEmpty()) "-" else meal.protein
                        }

                        tvFibre.apply {
                            setTextColor("#00298F".toColorInt())
                            text = if (meal.fibre.isNullOrEmpty()) "-" else meal.fibre
                        }

                        tvFat.apply {
                            setTextColor("#00298F".toColorInt())
                            text = if (meal.fat.isNullOrEmpty()) "-" else meal.fat
                        }

                        tvCarbs.apply {
                            setTextColor("#00298F".toColorInt())
                            text = if (meal.carbohydrate.isNullOrEmpty()) "-" else meal.carbohydrate
                        }

                        textView148.setTextColor("#991863AD".toColorInt())
                        textView147.setTextColor("#991863AD".toColorInt())
                        textView149.setTextColor("#991863AD".toColorInt())
                        textView150.setTextColor("#991863AD".toColorInt())
                    }

                    root.visible()
                }
            }

            DietState.BOOSTER -> {
                binding.root.setBackgroundResource(R.drawable.back_ai_nutrition)

                binding.toolbar.tvTitle.text = meal.title
                binding.toolbar.tvTitle.setTextColor(Color.parseColor("#8ACA88"))

                binding.lytMealDetails.root.gone()
                binding.lytComfortMealDetails.root.gone()
                binding.lytBoosterMealDetails.apply {

                    tvMealName.text = meal.meal_name
                    tvDescription.text = meal.description
                    tvNutrition.text = "${meal.portion} | ${meal.calories}"

                    lytNutritionData.apply {

                        tvProtein.apply {
                            setTextColor("#A95D84".toColorInt())
                            text = if (meal.protein.isNullOrEmpty()) "-" else meal.protein
                        }

                        tvFibre.apply {
                            setTextColor("#A95D84".toColorInt())
                            text = if (meal.fibre.isNullOrEmpty()) "-" else meal.fibre
                        }

                        tvFat.apply {
                            setTextColor("#A95D84".toColorInt())
                            text = if (meal.fat.isNullOrEmpty()) "-" else meal.fat
                        }

                        tvCarbs.apply {
                            setTextColor("#A95D84".toColorInt())
                            text = if (meal.carbohydrate.isNullOrEmpty()) "-" else meal.carbohydrate
                        }

                        textView148.setTextColor("#98AA0066".toColorInt())
                        textView147.setTextColor("#98AA0066".toColorInt())
                        textView149.setTextColor("#98AA0066".toColorInt())
                        textView150.setTextColor("#98AA0066".toColorInt())
                    }

                    root.visible()
                }
            }

        }

        messagesStrings.add(
            getString(
                R.string.text_find_alternatives_of_for_my_diet_value,
                meal.meal_name
            )
        )
        messagesStrings.add(
            getString(
                R.string.text_what_are_the_nutritional_benefits_of_value,
                meal.meal_name
            )
        )
        messagesStrings.add(getString(R.string.text_give_me_a_recipe_for_value, meal.meal_name))

        displayMessage.postValue(getWorkoutAiString(currentPos))
        startTimer()

    }

    private fun getWorkoutAiString(position: Int): String {
        return messagesStrings.getOrNull(position) ?: ""
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                currentPos += 1

                if (currentPos > (messagesStrings.size - 1)) {
                    currentPos = 0
                }
                displayMessage.postValue(getWorkoutAiString(currentPos))
                startTimer()
            }
        }
        timer?.start()
    }
}