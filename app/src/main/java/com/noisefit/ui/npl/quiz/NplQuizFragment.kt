package com.noisefit.ui.npl.quiz

import android.animation.Animator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentNplQuizBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NplQuizFragment : BaseFragment<FragmentNplQuizBinding>(FragmentNplQuizBinding::inflate) {
    private val mViewModel: NplQuizViewModel by viewModels()

    private var mediaPlayer: MediaPlayer? = null
    private val mAdapter: NplQuizAIAdaptor by lazy {
        NplQuizAIAdaptor()
    }

    private fun playMusic(soundId: Int) {
        try {
            mediaPlayer = MediaPlayer.create(context, soundId)
            mediaPlayer?.isLooping = false
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.getQuizOfflineData()
        setRecycler()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mViewModel.nplQuizData.value?.questions.isNullOrEmpty()) {
                    navigateUpSafe()
                } else {
                    showQuitConfirmationDialog()
                }
            }
        }


    private fun showQuitConfirmationDialog() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            QUIT_KEY,
            this
        ) { key, bundle ->
            val isSelected = bundle.getBoolean("isSelected")
            if (isSelected) {
                if (mViewModel.timer != null) {
                    mViewModel.timer?.cancel()
                }
                mViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_QUIZ_CLOSER_QUIT_CLICK)
                navigateUpSafe()
            }
        }

        navigate(NplQuizFragmentDirections.actionNplQuizFragmentToNplQQuitBottomSheet())

    }

    override fun initListener() {
        setDefaultData()
        binding.lytToolbar.backBtn.setOnClickListener {
            mViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_QUIZ_BACK_ICON_CLICK)
            if (mViewModel.nplQuizData.value?.questions.isNullOrEmpty()) {
                navigateUpSafe()
            } else {
                showQuitConfirmationDialog()
            }
        }

        binding.lytSubData.lytWithOptions.ivQ1.setOnClickListener {
            //stop timer
            if (mViewModel.timer != null) {
                mViewModel.timer?.cancel()
            }
            mViewModel.isIdle = false
            if (mViewModel.nplQuizData.value?.questions == null)
                return@setOnClickListener
            val questionData = try {
                mViewModel.nplQuizData.value?.questions!![mViewModel.selectedQuesPos]
            } catch (exp: Exception) {
                exp.printStackTrace()
                return@setOnClickListener
            }
            mViewModel.setUserAnswer(
                questionData.quesId,
                questionData.listOptions[0].optionId
            )
            when (questionData.correctAns) {
                questionData.listOptions[0].optionId -> {
                    //show winner animation
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 1)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Correct", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(1)
                    binding.lytSubData.lytWithOptions.ivQ1.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                }
                questionData.listOptions[1].optionId -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ2.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ1.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
                questionData.listOptions[2].optionId -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ3.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ1.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
                else -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ4.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ1.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
            }
            setClickViewDisable(false)
        }
        binding.lytSubData.lytWithOptions.ivQ2.setOnClickListener {
            //stop timer
            if (mViewModel.timer != null) {
                mViewModel.timer?.cancel()
            }
            mViewModel.isIdle = false
            if (mViewModel.nplQuizData.value?.questions == null)
                return@setOnClickListener
            val questionData = try {
                mViewModel.nplQuizData.value?.questions!![mViewModel.selectedQuesPos]
            } catch (exp: Exception) {
                exp.printStackTrace()
                return@setOnClickListener
            }
            mViewModel.setUserAnswer(
                questionData.quesId,
                questionData.listOptions[1].optionId
            )
            when (questionData.correctAns) {
                questionData.listOptions[1].optionId -> {
                    //show winner animation
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 1)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("correct", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(1)
                    binding.lytSubData.lytWithOptions.ivQ2.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                }
                questionData.listOptions[0].optionId -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ1.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ2.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
                questionData.listOptions[2].optionId -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ3.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ2.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
                else -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ4.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ2.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
            }
            setClickViewDisable(false)
        }
        binding.lytSubData.lytWithOptions.ivQ3.setOnClickListener {
            //stop timer
            if (mViewModel.timer != null) {
                mViewModel.timer?.cancel()
            }
            mViewModel.isIdle = false
            if (mViewModel.nplQuizData.value?.questions == null)
                return@setOnClickListener
            val questionData = try {
                mViewModel.nplQuizData.value?.questions!![mViewModel.selectedQuesPos]
            } catch (exp: Exception) {
                exp.printStackTrace()
                return@setOnClickListener
            }
            mViewModel.setUserAnswer(
                questionData.quesId,
                questionData.listOptions[2].optionId
            )
            when (questionData.correctAns) {
                questionData.listOptions[2].optionId -> {
                    //show winner animation
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 1)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("correct", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(1)
                    binding.lytSubData.lytWithOptions.ivQ3.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                }
                questionData.listOptions[0].optionId -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ1.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ3.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
                questionData.listOptions[1].optionId -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ2.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ3.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
                else -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ4.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ3.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
            }
            setClickViewDisable(false)
        }
        binding.lytSubData.lytWithOptions.ivQ4.setOnClickListener {
            //stop timer
            if (mViewModel.timer != null) {
                mViewModel.timer?.cancel()
            }
            mViewModel.isIdle = false
            if (mViewModel.nplQuizData.value?.questions == null)
                return@setOnClickListener
            val questionData = try {
                mViewModel.nplQuizData.value?.questions!![mViewModel.selectedQuesPos]
            } catch (exp: Exception) {
                exp.printStackTrace()
                return@setOnClickListener
            }
            mViewModel.setUserAnswer(
                questionData.quesId,
                questionData.listOptions[3].optionId
            )
            when (questionData.correctAns) {
                questionData.listOptions[3].optionId -> {
                    //show winner animation
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 1)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("correct", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(1)
                    binding.lytSubData.lytWithOptions.ivQ4.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                }
                questionData.listOptions[0].optionId -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ1.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ4.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
                questionData.listOptions[1].optionId -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ2.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ4.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
                else -> {
                    mAdapter.updateItem(mViewModel.selectedQuesPos, 2)
                    binding.lytSubData.lytWithOptions.tvQuestion.gone()
                    binding.lytSubData.lytWithOptions.animViewAnswer.visible()
                    mViewModel.logOptionEvent("Incorrect", questionData.quesId)

                    mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                    showAnimAfterAnswer(0)
                    binding.lytSubData.lytWithOptions.ivQ3.setBackgroundResource(R.drawable.ic_correct_qq_chips)
                    binding.lytSubData.lytWithOptions.ivQ4.setBackgroundResource(R.drawable.ic_wrong_qq_chips)

                }
            }

            setClickViewDisable(false)
        }

        binding.lytSubData.lytDone.tvDone.setOnClickListener {
            mViewModel.logWinLoseEvent()
            showUpcomingQuizTimer()
        }
    }

    override fun subscribeObservers() {
        mViewModel.submitFailed.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }
        mViewModel.answerData.observe(this) {
            binding.lytSubData.lytWithOptions.root.gone()
            binding.lytSubData.lytAnimOnly.root.gone()
            binding.lytSubData.lytNextQuiz.root.gone()
            binding.lytSubData.lytDone.root.visible()
            showWinLooseAnim()

        }
        mViewModel.nplQuizData.observe(this) {
            if (it.questions.isNullOrEmpty()) {
                mViewModel.elapsedTimeAfterSubmitAnswer = it.elapsedTime
                showUpcomingQuizTimer()
            } else {
                if (mViewModel.hasStartedQuiz()) {
                    mViewModel.selectedQuesPos = mViewModel.lastAnsweredQuesPos()
                    if (mViewModel.selectedQuesPos >= ((mViewModel.nplQuizData.value?.questions?.size?.minus(
                            1
                        ))
                            ?: 0)
                    ) {
                        mViewModel.submitAnswer()
                    } else {
                        showAnimation()
                    }
                } else
                    showAnimation()

            }
            mAdapter.setData(mViewModel.getIndicatorData())
        }

        mViewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        mViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        mViewModel.nextQuizTimer.observe(this) {
            updateNextQuizTicker(it)
        }

        mViewModel.nextQuizTimerFinish.observe(this) {
            if (it) {
                if (mViewModel.timerLeft != null) {
                    mViewModel.timerLeft?.cancel()
                }
                binding.lytSubData.lytNextQuiz.tvTimeLeft.text = "00 : 00 : 00"
            }
        }
        mViewModel.quizTimer.observe(this) {
            updateQuizTicker(it)
        }
        mViewModel.quizTimerFinish.observe(this) {
            binding.lytSubData.lytTimer.tvTimer.text = "00"
            binding.lytSubData.lytWithOptions.tvQuestion.gone()
            binding.lytSubData.lytWithOptions.animViewAnswer.visible()
            try {
                val questionData =
                    mViewModel.nplQuizData.value?.questions!![mViewModel.selectedQuesPos]
                mViewModel.setUserAnswer(questionData.quesId, -1L)
                mAdapter.updateItem(mViewModel.selectedQuesPos, 0)
                mViewModel.selectedQuesPos = mViewModel.selectedQuesPos + 1
                setClickViewDisable(false)
                showAnimAfterAnswer(-1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun updateQuizTicker(it: Long?) {
        binding.lytSubData.lytTimer.tvTimer.text = mViewModel.parseQuizTickerTime(it)
    }

    private fun updateNextQuizTicker(it: Long?) {
        binding.lytSubData.lytNextQuiz.tvTimeLeft.text = mViewModel.parseNextQuizTickerTime(it)
    }

    private fun setRecycler() {
        with(binding.rvAnswerIndicator) {
            adapter = mAdapter
        }

    }

    private fun showUpcomingQuizTimer() {
        binding.lytSubData.lytWithOptions.root.gone()
        binding.lytSubData.lytAnimOnly.root.gone()
        binding.lytSubData.lytDone.root.gone()
        binding.lytSubData.lytNextQuiz.root.visible()
        binding.lytSubData.lytTimer.tvTimer.text = "00"
        mAdapter.setData(mViewModel.getIndicatorData())
    }


    private fun updateOptionUiDefaultState() {
        mViewModel.isIdle = true
        binding.lytSubData.lytWithOptions.ivQ1.setBackgroundResource(R.drawable.ic_default_qq_chips)
        binding.lytSubData.lytWithOptions.ivQ2.setBackgroundResource(R.drawable.ic_default_qq_chips)
        binding.lytSubData.lytWithOptions.ivQ3.setBackgroundResource(R.drawable.ic_default_qq_chips)
        binding.lytSubData.lytWithOptions.ivQ4.setBackgroundResource(R.drawable.ic_default_qq_chips)
        setClickViewDisable(true)

    }

    private fun setDefaultData() {
        binding.lytSubData.lytTimer.tvTimer.text = "15"
        binding.lytSubData.lytAnimOnly.root.visible()
        binding.lytSubData.lytWithOptions.root.gone()
    }


    private fun moveToNextQuestion() {
        if (mViewModel.selectedQuesPos >= ((mViewModel.nplQuizData.value?.questions?.size)
                ?: 0)
        ) {
            if (mViewModel.timer != null) {
                mViewModel.timer?.cancel()
                mViewModel.timer = null
                mViewModel.submitAnswer()

            }
        } else
            updateQuestionOnUI()
    }

    private fun showAnimation() {
        playMusic(R.raw.quiz_countdown)
        binding.lytSubData.lytAnimOnly.animView.addAnimatorListener(object :
            Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                if (mediaPlayer != null) {
                    mediaPlayer?.stop()
                }
                binding.lytSubData.lytAnimOnly.animView.gone()
                binding.lytSubData.lytWithOptions.root.visible()
                mViewModel.startTimer()
                moveToNextQuestion()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
        binding.lytSubData.lytAnimOnly.animView.setAnimation(R.raw.anim_3_2_1_go)
        binding.lytSubData.lytAnimOnly.animView.playAnimation()
        binding.lytSubData.lytAnimOnly.animView.repeatCount = 0
    }

    private fun showAnimAfterAnswer(resultType: Int) {
        binding.lytSubData.lytWithOptions.animViewAnswer.addAnimatorListener(object :
            Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                mediaPlayer?.stop()
                binding.lytSubData.lytWithOptions.animViewAnswer.gone()
                binding.lytSubData.lytWithOptions.tvQuestion.visible()
                updateOptionUiDefaultState()
                moveToNextQuestion()
                if (mViewModel.selectedQuesPos < ((mViewModel.nplQuizData.value?.questions?.size)
                        ?: 0)
                )
                    mViewModel.startTimer()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
        val (anim,sound) = mViewModel.getWLTAnimFiles(
            resultType
        )

        binding.lytSubData.lytWithOptions.animViewAnswer.setAnimation(
            anim
        )
        playMusic(sound)
        binding.lytSubData.lytWithOptions.animViewAnswer.playAnimation()
        binding.lytSubData.lytWithOptions.animViewAnswer.repeatCount = 0
    }

    private fun showWinLooseAnim() {
        binding.lytSubData.lytDone.tvPoints.text =
            mViewModel.getDisplayMessageWinLoss(binding.lytSubData.lytDone.tvPoints)
        binding.lytSubData.lytDone.animViewDone.addAnimatorListener(object :
            Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                mediaPlayer?.stop()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
        val (anim,sound) = mViewModel.getAnimFilesBasedOnWinLoss()
        binding.lytSubData.lytDone.animViewDone.setAnimation(anim)
        playMusic(sound)
        binding.lytSubData.lytDone.animViewDone.playAnimation()
        binding.lytSubData.lytDone.animViewDone.repeatCount = 0
    }

    private fun setClickViewDisable(isEnable: Boolean) {
        binding.lytSubData.lytWithOptions.ivQ1.isEnabled = isEnable
        binding.lytSubData.lytWithOptions.ivQ2.isEnabled = isEnable
        binding.lytSubData.lytWithOptions.ivQ3.isEnabled = isEnable
        binding.lytSubData.lytWithOptions.ivQ4.isEnabled = isEnable

    }


    private fun updateQuestionOnUI() {
        if (mViewModel.nplQuizData.value?.questions == null)
            return
        try {
            val questionData = mViewModel.nplQuizData.value?.questions!![mViewModel.selectedQuesPos]
            mAdapter.updateItem(mViewModel.selectedQuesPos, 3)
            binding.lytSubData.lytWithOptions.tvQuestion.text =
                questionData.title
            binding.lytSubData.lytWithOptions.tvOption1.text =
                questionData.listOptions[0].title
            binding.lytSubData.lytWithOptions.tvOption2.text =
                questionData.listOptions[1].title
            binding.lytSubData.lytWithOptions.tvOption3.text =
                questionData.listOptions[2].title
            binding.lytSubData.lytWithOptions.tvOption4.text =
                questionData.listOptions[3].title
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onStop() {
        super.onStop()
        try {
            val questionData = mViewModel.nplQuizData.value?.questions!![mViewModel.selectedQuesPos]
            mViewModel.setUserAnswer(questionData.quesId, -1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (mViewModel.timer != null) {
            mViewModel.timer?.cancel()
            mViewModel.timer = null
        }
        if (mViewModel.timerLeft != null) {
            mViewModel.timerLeft?.cancel()
            mViewModel.timerLeft = null
        }
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
        }
    }


}