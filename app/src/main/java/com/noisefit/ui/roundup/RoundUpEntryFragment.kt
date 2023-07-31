package com.noisefit.ui.roundup

import android.animation.Animator
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieDrawable
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.noisefit.luna.R
import com.noisefit_commans.data.model.RoundUpResponse
import com.noisefit.luna.databinding.FragmentRoundUpEntryBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.util.ImageUtil
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.RoundEndOptions
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.noisefit_commans.utils.prettyCount
import dagger.hilt.android.AndroidEntryPoint
import java.io.InputStream
import java.util.*
import javax.inject.Inject

private const val PAGE_INDEX = "PAGE_INDEX"
private const val DATA_RESPONSE = "DATA_RESPONSE"

@AndroidEntryPoint
class RoundUpEntryFragment :
    BaseFragment<FragmentRoundUpEntryBinding>(FragmentRoundUpEntryBinding::inflate), RefreshData {
    private val viewModel: RoundUpViewModel by viewModels()

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager: SessionManager


    companion object {
        @JvmStatic
        fun newInstance(pageIndex: Int, data: RoundUpResponse?) = RoundUpEntryFragment().apply {
            arguments = Bundle().apply {
                putInt(PAGE_INDEX, pageIndex)
                putSerializable(DATA_RESPONSE, data)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.setResponseData(it.get(DATA_RESPONSE) as RoundUpResponse)
            viewModel.setEntryType(it.getInt(PAGE_INDEX), viewModel.response)
        }
    }

    fun fadeInBackground(view: View, drawable: Int) {
        view.alpha = 0f
        view.background =
            ContextCompat.getDrawable(requireContext(), drawable)
        view.animate().apply {
            interpolator = LinearInterpolator()
            duration = 200
            alpha(1f)
            startDelay = 200
            start()
        }
    }


    private fun updateUI() {
        var mTitle = ""

        if (viewModel.response == null) return

        when (viewModel.entryType) {
            EntryType.INTRO -> {
                fadeInBackground(binding.ivBackgroundLayer, R.drawable.ic_round_up_entry_back)

                binding.lytSummaryContent.root.gone()
                binding.lytCtaContent.root.gone()
                binding.lytAnimation.root.gone()
                binding.layout.root.visible()
                binding.layout.tvMsg.gone()
                binding.layout.tvTitle.gone()
                val replaceByName = viewModel.getFirstName()

                val jsonString = try {
                    val inputStream: InputStream =
                        resources.openRawResource(R.raw.anim_round_up_intro)
                    val b = ByteArray(inputStream.available())
                    inputStream.read(b)
                    String(b).replace("\$replaceByName", replaceByName)
                } catch (e: Exception) {
                    ""
                }

                binding.layout.ivAnimation.setAnimationFromJson(
                    jsonString,
                    "round_up_$replaceByName"
                )
                binding.layout.ivAnimation.playAnimation()
                binding.layout.ivAnimation.repeatCount = 0

            }
            EntryType.STEPS -> {
                binding.lytCtaContent.root.gone()
                binding.lytAnimation.root.gone()
                binding.lytSummaryContent.root.visible()
                binding.tvMsg.gone()
                binding.layout.root.gone()

                fadeInBackground(binding.ivBackgroundLayer, R.drawable.ic_round_up_step_back)

                /*binding.lytContainer.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_up_step_back)*/
                binding.lytSummaryContent.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_step_color
                    )
                )

                formattedString(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_step_color
                    ), viewModel.response?.steps?.subtitle.toString()
                )
                mTitle = viewModel.response?.steps?.title.toString()
                setTextWithSpan(
                    binding.lytSummaryContent.tvMsg,
                    viewModel.response?.steps?.totalMsg.toString(),
                    viewModel.response?.steps?.totalValue ?: "",
                )

                when (viewModel.response?.steps?.status?.lowercase()) {
                    RoundUpStatus.HIGH.name.lowercase() -> showAnimation(R.raw.anim_roundup_step_high)
                    RoundUpStatus.MEDIUM.name.lowercase() -> showAnimation(R.raw.anim_roundup_step_high)
                    RoundUpStatus.LOW.name.lowercase() -> showAnimation(R.raw.anim_roundup_step_low)
                }
            }
            EntryType.WORKOUT -> {
                binding.lytCtaContent.root.gone()
                binding.lytAnimation.root.gone()
                binding.lytSummaryContent.root.visible()
                binding.tvMsg.gone()
                binding.layout.root.gone()
                fadeInBackground(binding.ivBackgroundLayer, R.drawable.ic_round_up_workout_back)

                /*binding.lytContainer.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_up_workout_back)*/
                binding.lytSummaryContent.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_workout_color
                    )
                )
                formattedString(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_workout_color
                    ), viewModel.response?.workout?.subtitle.toString()
                )
                mTitle = viewModel.response?.workout?.title.toString()
                setTextWithSpan(
                    binding.lytSummaryContent.tvMsg,
                    viewModel.response?.workout?.totalMsg.toString(),
                    viewModel.response?.workout?.totalValue ?: "",
                    EntryType.WORKOUT
                )
                if (viewModel.response?.workout?.status?.lowercase().equals(
                        RoundUpStatus.FOUND.toString().lowercase()
                    )
                ) {
                    showAnimation(R.raw.anim_roundup_workout_found)

                    binding.lytSummaryContent.lytWorkoutImage.root.alpha = 0f
                    binding.lytSummaryContent.lytWorkoutImage.ivWorkout.setImageResource(
                        ImageUtil().getImageFromActivity(
                            viewModel.response?.workout?.totalValue ?: ""
                        )
                    )
                    binding.lytSummaryContent.lytWorkoutImage.root.visible()

                    binding.lytSummaryContent.lytWorkoutImage.root.animate().apply {
                        interpolator = LinearInterpolator()
                        duration = 300
                        alpha(1f)
                        startDelay = 1000
                        start()
                    }

                } else {
                    binding.lytSummaryContent.lvAnim.playAnimation(
                        0,
                        R.raw.anim_roundup_workout_non
                    )
                }
            }
            EntryType.CALORIES -> {
                binding.lytCtaContent.root.gone()
                binding.lytAnimation.root.gone()
                binding.lytSummaryContent.root.visible()
                binding.tvMsg.gone()
                binding.layout.root.gone()
                fadeInBackground(binding.ivBackgroundLayer, R.drawable.ic_round_up_calories_back)

                /*binding.lytContainer.background = ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_round_up_calories_back
                )*/
                binding.lytSummaryContent.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_calories_color
                    )
                )
                formattedString(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_calories_color
                    ), viewModel.response?.calories?.subtitle.toString()
                )
                mTitle = viewModel.response?.calories?.title.toString()
                setTextWithSpan(
                    binding.lytSummaryContent.tvMsg,
                    viewModel.response?.calories?.totalMsg.toString(),
                    viewModel.response?.calories?.totalValue ?: "",
                    EntryType.CALORIES
                )
                when (viewModel.response?.calories?.status?.lowercase()) {
                    RoundUpStatus.HIGH.name.lowercase() -> showAnimation(R.raw.anim_roundup_calories_high)
                    RoundUpStatus.MEDIUM.name.lowercase() -> showAnimation(R.raw.anim_roundup_calories_med)
                    RoundUpStatus.LOW.name.lowercase() -> showAnimation(R.raw.anim_roundup_calories_low)
                }

            }
            EntryType.CHALLENGE -> {
                binding.lytSummaryContent.root.visible()
                binding.lytCtaContent.root.gone()
                binding.lytAnimation.root.gone()
                binding.tvMsg.gone()
                binding.layout.root.gone()
                fadeInBackground(binding.ivBackgroundLayer, R.drawable.ic_round_up_challenge_back)

                /*binding.lytContainer.background = ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_round_up_challenge_back
                )*/
                binding.lytSummaryContent.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_challenges_color
                    )
                )
                formattedString(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_challenges_color
                    ), viewModel.response?.challenges?.subtitle.toString()
                )
                mTitle = viewModel.response?.challenges?.title.toString()
                if (viewModel.response?.challenges?.status?.lowercase(Locale.getDefault()) == "none") binding.lytSummaryContent.tvMsg.text =
                    viewModel.response?.challenges?.totalMsg.toString()
                else setTextWithSpan(
                    binding.lytSummaryContent.tvMsg,
                    viewModel.response?.challenges?.totalMsg.toString(),
                    viewModel.response?.challenges?.totalValue ?: "",
                )
                when (viewModel.response?.challenges?.status?.lowercase()) {
                    RoundUpStatus.HIGH.name.lowercase() -> showAnimation(R.raw.anim_roundup_challenge_high)
                    RoundUpStatus.MEDIUM.name.lowercase() -> showAnimation(R.raw.anim_roundup_challenge_med)
                    RoundUpStatus.LOW.name.lowercase() -> showAnimation(R.raw.anim_roundup_challenge_med)
                    else -> showAnimation(R.raw.anim_roundup_challenge_non)
                }

            }
            EntryType.SLEEP -> {
                binding.lytSummaryContent.root.visible()
                binding.lytCtaContent.root.gone()
                binding.lytAnimation.root.gone()
                binding.tvMsg.gone()
                binding.layout.root.gone()
                fadeInBackground(binding.ivBackgroundLayer, R.drawable.ic_round_up_sleep_back)

                /*binding.lytContainer.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_up_sleep_back)*/
                binding.lytSummaryContent.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_sleep_color
                    )
                )
                formattedString(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_sleep_color
                    ), viewModel.response?.sleep?.subtitle.toString()
                )
                mTitle = viewModel.response?.sleep?.title.toString()
                if (viewModel.response?.sleep?.status?.lowercase(Locale.getDefault()) == "none") binding.lytSummaryContent.tvMsg.text =
                    viewModel.response?.sleep?.totalMsg.toString()
                else {
                    setTextWithSpan(
                        binding.lytSummaryContent.tvMsg,
                        viewModel.response?.sleep?.totalMsg.toString(),
                        viewModel.response?.sleep?.totalValue ?: "",
                        EntryType.SLEEP
                    )
                }

                when (viewModel.response?.sleep?.status?.lowercase()) {
                    RoundUpStatus.HIGH.name.lowercase() -> showAnimation(R.raw.anim_roundup_sleeper_high)
                    RoundUpStatus.MEDIUM.name.lowercase() -> showAnimation(R.raw.anim_roundup_sleeper_med)
                    RoundUpStatus.LOW.name.lowercase() -> showAnimation(R.raw.anim_roundup_sleeper_low)
                    else -> showAnimation(R.raw.anim_roundup_sleeper_non)
                }

            }
            EntryType.OVERALL -> {
                binding.lytSummaryContent.root.visible()
                binding.lytCtaContent.root.gone()
                binding.lytAnimation.root.gone()
                binding.tvMsg.gone()
                binding.layout.root.gone()
                fadeInBackground(binding.ivBackgroundLayer, R.drawable.ic_round_up_overall_back)

                /*binding.lytContainer.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_up_overall_back)*/
                binding.lytSummaryContent.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_overall_color
                    )
                )
                formattedString(
                    ContextCompat.getColor(
                        requireContext(), R.color.round_up_overall_color
                    ), viewModel.response?.overall?.subtitle.toString()
                )
                mTitle = viewModel.response?.overall?.title.toString()
                setTextWithSpan(
                    binding.lytSummaryContent.tvMsg,
                    viewModel.response?.overall?.totalMsg.toString(),
                    viewModel.response?.overall?.totalValue ?: "",
                    EntryType.OVERALL
                )
                when (viewModel.response?.overall?.status?.lowercase()) {
                    RoundUpStatus.HIGH.name.lowercase() -> showAnimation(R.raw.anim_roundup_overall_high)
                    RoundUpStatus.MEDIUM.name.lowercase() -> showAnimation(R.raw.anim_roundup_overall_med)
                    RoundUpStatus.LOW.name.lowercase() -> showAnimation(R.raw.anim_roundup_overall_low)
                }

            }
            EntryType.END -> {
                if (viewModel.localDataStore.getYearlyEndGoal().isNullOrEmpty()) {

                    fadeInBackground(binding.ivBackgroundLayer, R.drawable.ic_round_up_entry_back)

                    /* binding.lytContainer.background = ContextCompat.getDrawable(
                         requireContext(), R.drawable.ic_round_up_entry_back
                     )*/
                    binding.lytSummaryContent.root.gone()
                    binding.tvMsg.gone()
                    binding.layout.root.visible()
                    binding.layout.tvMsg.gone()
                    binding.layout.tvTitle.gone()
                    var animationToDisplay = 0
                    if (viewModel.response?.roundUpEnd?.status?.lowercase()
                            .equals(RoundUpStatus.HIGH.toString().lowercase())
                    ) {
                        animationToDisplay = R.raw.anim_roundup_exit_high
                    } else if (viewModel.response?.roundUpEnd?.status?.lowercase()
                            .equals(RoundUpStatus.MEDIUM.toString().lowercase())
                    ) {
                        animationToDisplay = R.raw.anim_roundup_exit_med
                    } else if (viewModel.response?.roundUpEnd?.status?.lowercase()
                            .equals(RoundUpStatus.LOW.toString().lowercase())
                    ) {
                        animationToDisplay = R.raw.anim_roundup_exit_low
                    } else {
                        animationToDisplay = R.raw.anim_roundup_exit_none
                    }

                    binding.layout.ivAnimation.playAnimation(
                        0,
                        animationToDisplay
                    )

                    binding.layout.root.visible()
                    binding.lytCtaContent.root.gone()
                    binding.layout.ivAnimation.addAnimatorListener(object :
                        Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {
                            nullableBinding?.layout?.root?.visible()
                            nullableBinding?.lytCtaContent?.root?.gone()

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            nullableBinding?.layout?.root?.gone()
                            nullableBinding?.lytCtaContent?.root?.visible()
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationRepeat(animation: Animator?) {

                        }
                    })
                } else {
                    fadeInBackground(binding.ivBackgroundLayer, R.drawable.ic_round_up_entry_back)

                    binding.lytCtaContent.root.gone()
                    binding.lytSummaryContent.root.gone()
                    binding.tvMsg.gone()
                    binding.layout.root.visible()
                    binding.layout.tvMsg.gone()
                    binding.layout.tvTitle.gone()
                    binding.lytAnimation.root.visible()
                    binding.lytAnimation.tvSubTitle.text =
                        viewModel.localDataStore.getYearlyEndGoal()


                    val animationToDisplayExit: Int =
                        if (viewModel.response?.roundUpEnd?.status?.lowercase()
                                .equals(RoundUpStatus.HIGH.toString().lowercase())
                        ) {
                            R.raw.anim_roundup_exit_high
                        } else if (viewModel.response?.roundUpEnd?.status?.lowercase()
                                .equals(RoundUpStatus.MEDIUM.toString().lowercase())
                        ) {
                            R.raw.anim_roundup_exit_med
                        } else if (viewModel.response?.roundUpEnd?.status?.lowercase()
                                .equals(RoundUpStatus.LOW.toString().lowercase())
                        ) {
                            R.raw.anim_roundup_exit_low
                        } else {

                            R.raw.anim_roundup_exit_none
                        }
                    binding.layout.ivAnimation.playAnimation(
                        0,
                        animationToDisplayExit
                    )

                    binding.lytAnimation.root.gone()
                    binding.layout.root.visible()


                    binding.layout.ivAnimation.addAnimatorListener(object :
                        Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {


                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            nullableBinding?.layout?.root?.gone()
                            nullableBinding?.lytCtaContent?.root?.gone()
                            nullableBinding?.lytAnimation?.root?.visible()

                            var animationToDisplay = 0
                            var background = 0

                            when (viewModel.localDataStore.getYearlyEndGoal()) {
                                RoundEndOptions.WEIGHT_LOSS.type -> {
                                    background = R.drawable.ic_round_up_calories_back
                                    animationToDisplay = R.raw.anim_roundup_goal_weight_loss
                                }
                                RoundEndOptions.GAIN_MUSCLE.type -> {
                                    background = R.drawable.ic_round_up_workout_back
                                    animationToDisplay = R.raw.anim_roundup_goal_gain_muscle
                                }
                                RoundEndOptions.INCREASE_PRODUCTIVITY.type -> {
                                    background = R.drawable.ic_round_up_overall_back
                                    animationToDisplay =
                                        R.raw.anim_roundup_goal_increase_productivity
                                }
                                RoundEndOptions.BETTER_SLEEP.type -> {
                                    background = R.drawable.ic_round_up_sleep_back
                                    animationToDisplay = R.raw.anim_roundup_goal_better_sleep
                                }
                            }
                            if (background != 0) {
                                if (nullableBinding != null) {
                                    fadeInBackground(binding.ivBackgroundLayer, background)
                                }
                            }

                            nullableBinding?.lytAnimation?.lvAnim?.playAnimation(
                                LottieDrawable.INFINITE,
                                animationToDisplay
                            )
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationRepeat(animation: Animator?) {

                        }
                    })

                }


            }
            else -> {
                throw IllegalArgumentException("Max 8 elements")
            }
        }

        binding.lytSummaryContent.tvTitle.text = mTitle
    }

    override fun onPause() {
        super.onPause()
        if (binding.layout.ivAnimation.isAnimating)
            binding.layout.ivAnimation.pauseAnimation()
        if (binding.lytAnimation.lvAnim.isAnimating)
            binding.lytAnimation.lvAnim.pauseAnimation()
    }

    private fun showAnimation(animation: Int) {
        binding.lytSummaryContent.lvAnim.playAnimation(
            LottieDrawable.INFINITE,
            animation
        )
    }


    private fun formattedString(color: Int, msg: String) {
        binding.lytSummaryContent.tvSubTitle1.setTextColor(
            color
        )
        if (msg.isNotEmpty()) {
            val temp = msg.split("\n").toTypedArray()
            binding.lytSummaryContent.tvSubTitle.text = temp[0]
            try {
                binding.lytSummaryContent.tvSubTitle1.text = temp[1]
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }


    override fun initListener() {
        binding.lytCtaContent.btnSave.setOnClickListener {
            if (viewModel.roundEndOptions.value?.type == null) {
                requireContext().showShortToast("Please choose your year end goal")
            } else {

                sessionManager.addUserAttributeToInsider(false, HashMap<String, Any>().apply {
                    this["yearly_goal"] = viewModel.roundEndOptions.value?.type.toString()
                })

                viewModel.localDataStore.setYearlyEndGoal(viewModel.roundEndOptions.value?.type.toString())
                Firebase.analytics.setUserProperty(
                    "yearly_goal",
                    viewModel.roundEndOptions.value?.type.toString()
                )
                var animationToDisplay = 0
                if (viewModel.roundEndOptions.value?.type.toString() == RoundEndOptions.WEIGHT_LOSS.type) {
                    binding.lytContainer.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_round_up_calories_back
                    )
                    animationToDisplay = R.raw.anim_roundup_goal_weight_loss
                } else if (viewModel.roundEndOptions.value?.type.toString() == RoundEndOptions.GAIN_MUSCLE.type) {
                    binding.lytContainer.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_round_up_workout_back
                    )
                    animationToDisplay = R.raw.anim_roundup_goal_gain_muscle
                } else if (viewModel.roundEndOptions.value?.type.toString() == RoundEndOptions.INCREASE_PRODUCTIVITY.type) {
                    binding.lytContainer.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_round_up_overall_back
                    )
                    animationToDisplay = R.raw.anim_roundup_goal_increase_productivity
                } else if (viewModel.roundEndOptions.value?.type.toString() == RoundEndOptions.BETTER_SLEEP.type) {
                    binding.lytContainer.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_round_up_sleep_back
                    )
                    animationToDisplay = R.raw.anim_roundup_goal_better_sleep
                }
                binding.lytCtaContent.root.gone()
                binding.lytAnimation.root.visible()
                binding.lytAnimation.tvSubTitle.text =
                    viewModel.roundEndOptions.value?.type.toString()

                binding.lytAnimation.lvAnim.playAnimation(
                    LottieDrawable.INFINITE,
                    animationToDisplay
                )

            }

        }

        binding.lytCtaContent.radioRoundUpGoal.tvRound1.setOnClickListener {
            viewModel.setRoundOptions(RoundEndOptions.WEIGHT_LOSS)
        }
        binding.lytCtaContent.radioRoundUpGoal.tvRound2.setOnClickListener {
            viewModel.setRoundOptions(RoundEndOptions.GAIN_MUSCLE)
        }
        binding.lytCtaContent.radioRoundUpGoal.tvRound3.setOnClickListener {
            viewModel.setRoundOptions(RoundEndOptions.INCREASE_PRODUCTIVITY)
        }
        binding.lytCtaContent.radioRoundUpGoal.tvRound4.setOnClickListener {
            viewModel.setRoundOptions(RoundEndOptions.BETTER_SLEEP)
        }

    }

    override fun subscribeObservers() {

        viewModel.roundEndOptions.observe(this) {
            when (it) {
                RoundEndOptions.WEIGHT_LOSS -> setSelectedGoal(0)
                RoundEndOptions.GAIN_MUSCLE -> setSelectedGoal(1)
                RoundEndOptions.INCREASE_PRODUCTIVITY -> setSelectedGoal(2)
                RoundEndOptions.BETTER_SLEEP -> setSelectedGoal(3)
                else -> {}
            }


        }
    }

    fun setTextWithSpan(
        textView: TextView, text: String, spanText: String, entryType: EntryType = EntryType.ELSE
    ) {

        if (spanText.isEmpty()) {
            textView.text = text
            return
        }

        try {
            val start = text.indexOf(spanText)
            val end = start + spanText.length

            val formattedValue = when (entryType) {
                EntryType.SLEEP -> {
                    formatTime(spanText.toIntOrNull() ?: 0)
                }
                EntryType.WORKOUT -> {
                    getFormattedActivityName(spanText)
                }
                EntryType.OVERALL -> {
                    spanText
                }
                EntryType.CALORIES -> {
                    try {
                        val valueArray = spanText.split(" ")

                        val longValue = valueArray.first().toLong()
                        if (longValue < 100000) {
                            longValue.numberFormatter() + " ${valueArray[1]}"
                        } else {
                            longValue.prettyCount() + " ${valueArray[1]}"
                        }
                    } catch (exp: NumberFormatException) {
                        exp.printStackTrace()
                        spanText
                    }
                }
                else -> {
                    try {
                        val longValue = spanText.toLong()
                        if (longValue < 100000) {
                            longValue.numberFormatter()
                        } else {
                            longValue.prettyCount()
                        }
                    } catch (exp: NumberFormatException) {
                        exp.printStackTrace()
                        spanText
                    }
                }
            }

            val sb = SpannableStringBuilder().append(text.substring(0, start)).bold {
                color(requireContext().getColor(android.R.color.white)) {
                    append(
                        formattedValue
                    )
                }
            }

            if (end < text.length) {
                sb.append(text.substring(end, text.length))
            }

            textView.text = sb
        } catch (exp: Exception) {
            textView.text = text
        }
    }

    fun formatTime(value: Int): String {
        val hrs = value / 60
        val minutes = value % 60

        if (hrs == 0) {
            return "$minutes mins"
        }
        return if (minutes == 0) {
            "$hrs hrs"
        } else {
            "$hrs hrs $minutes mins"
        }
    }


    /**
     * 0->Weight Loss
     * 1->Gain Muscle
     * 2->Increase Productivity
     * 3->Better Sleep
     */
    private fun setSelectedGoal(selectedGender: Int) {
        when (selectedGender) {
            0 -> {
                binding.lytCtaContent.radioRoundUpGoal.ivRound1.isChecked = true
                binding.lytCtaContent.radioRoundUpGoal.ivRound2.isChecked = false
                binding.lytCtaContent.radioRoundUpGoal.ivRound3.isChecked = false
                binding.lytCtaContent.radioRoundUpGoal.ivRound4.isChecked = false
            }
            1 -> {
                binding.lytCtaContent.radioRoundUpGoal.ivRound1.isChecked = false
                binding.lytCtaContent.radioRoundUpGoal.ivRound2.isChecked = true
                binding.lytCtaContent.radioRoundUpGoal.ivRound3.isChecked = false
                binding.lytCtaContent.radioRoundUpGoal.ivRound4.isChecked = false
            }
            2 -> {
                binding.lytCtaContent.radioRoundUpGoal.ivRound1.isChecked = false
                binding.lytCtaContent.radioRoundUpGoal.ivRound2.isChecked = false
                binding.lytCtaContent.radioRoundUpGoal.ivRound3.isChecked = true
                binding.lytCtaContent.radioRoundUpGoal.ivRound4.isChecked = false
            }
            3 -> {
                binding.lytCtaContent.radioRoundUpGoal.ivRound1.isChecked = false
                binding.lytCtaContent.radioRoundUpGoal.ivRound2.isChecked = false
                binding.lytCtaContent.radioRoundUpGoal.ivRound3.isChecked = false
                binding.lytCtaContent.radioRoundUpGoal.ivRound4.isChecked = true
            }

        }
    }

    override fun refresh() {
        updateUI()
    }

}

fun getFormattedActivityName(activityName: String): String {
    val actNameTemp = activityName.replace("_", " ")
    return actNameTemp.capitalizeWords()
}


enum class EntryType {
    INTRO, STEPS, WORKOUT, SLEEP, OVERALL, CALORIES, CHALLENGE, END, ELSE
}


enum class RoundUpStatus {
    LOW, HIGH, MEDIUM, NON, FOUND
}