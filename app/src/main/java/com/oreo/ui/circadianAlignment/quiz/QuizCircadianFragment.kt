package com.oreo.ui.circadianAlignment.quiz

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentQuizCircadianBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.circadian.CircadianQuizResponseModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizCircadianFragment :
    BaseFragment<FragmentQuizCircadianBinding>(FragmentQuizCircadianBinding::inflate) {

    private val viewModel: QuizCircadianViewModel by viewModels()

    private val mainViewModel: OreoMainViewModel by activityViewModels()

    var isBlocked = false

    private var questions: List<CircadianQuizResponseModel> = emptyList()

    private fun updateNavForPosition(position: Int) {
        val adapterCount = binding.viewPager.adapter?.itemCount ?: 0
        val isFirst = position == 0
        val isLast = position == adapterCount - 1 && adapterCount > 0

        if (isFirst) binding.ivBack.gone() else binding.ivBack.visible()

        val selected = questions.getOrNull(position)?.selectedOptionId
        if (selected != null) {
            if (isLast) {
                binding.btnNext.text = if (viewModel.localDataStore.isCircadianOnboardShown())
                    getString(R.string.text_done) else getString(R.string.text_get_started)
            } else {
                binding.btnNext.text = getString(R.string.text_next)
            }
            binding.btnNext.visible()
        } else {
            binding.btnNext.gone()
        }
    }

    private fun handleQuizOptionClick(pair: Pair<Int, Int>) {
        isBlocked = true
        /*Handler(Looper.getMainLooper()).postDelayed({*/
        viewModel.handleQuizOptionClick(pair)
        isBlocked = false
        /*}, 400
    )*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
        viewModel.getQuizData()
        setAdapter()
    }

    private fun setUi() {
        if (!viewModel.localDataStore.isCircadianOnboardShown()) {
            val tvSkip = binding.tvSkip
            tvSkip.paintFlags = tvSkip.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            tvSkip.visible()
        } else {
            binding.tvSkip.gone()
        }
    }

    private fun setAdapter() {

        /*val layoutManager = object : LinearLayoutManager(context, RecyclerView.VERTICAL, false) {
             override fun smoothScrollToPosition(
                 recyclerView: RecyclerView,
                 state: RecyclerView.State,
                 position: Int
             ) {

                 val smoothScroller = object : LinearSmoothScroller(recyclerView.context){
                     override fun getVerticalSnapPreference(): Int {
                         return SNAP_TO_START
                     }
                     override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                         return 100f / displayMetrics.densityDpi
                     }
                 }

                 smoothScroller.targetPosition = position
                 startSmoothScroll(smoothScroller)

             }
         }*/
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }


        binding.tvSkip.setOnClickListener {
            viewModel.submitQuizQuesAndAnswers(true)
        }

        binding.ivBack.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current > 0) {
                binding.viewPager.setCurrentItem(current - 1, true)
            }
        }

        binding.btnNext.setOnClickListener {
            if(binding.btnNext.text.equals(getString(R.string.text_get_started))){
                mainViewModel.sessionManager.logMoEngageAppEvent(
                    MoEngageLunaAppEvents.quiz_resaved,
                    hashMapOf(
                        "source" to "circadian"
                    )
                )
            }
            val current = binding.viewPager.currentItem
            val lastIndex = (binding.viewPager.adapter?.itemCount ?: 1) - 1
            if (current < lastIndex) {
                binding.viewPager.setCurrentItem(current + 1, true)
            }
            if(current==lastIndex){
                viewModel.submitQuizQuesAndAnswers()

            }
        }
    }

    override fun subscribeObservers() {
        viewModel.quizData.observe(this) {
            val list: List<CircadianQuizResponseModel> = it
            questions = list
            val adapter = QuizFragmentAdapter(this, list) { pair ->
                handleQuizOptionClick(pair)
            }
            binding.viewPager.adapter = adapter

            binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
            binding.viewPager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT

            binding.viewPager.isUserInputEnabled = false

            binding.viewPager.setPageTransformer { page, position ->
                when {
                    position < -1 -> {
                        page.alpha = 0f
                        page.scaleX = 0.8f
                        page.scaleY = 0.8f
                    }

                    position <= -0.1 -> {
                        // Page is slightly off-screen (peek from top)
                        page.alpha = 0.7f + (0.3f * (1 + position / 0.9f))
                        val scaleFactor = 0.8f + (0.2f * (1 + position / 0.9f))
                        page.scaleX = scaleFactor
                        page.scaleY = scaleFactor
                        page.translationY = -50 * (1 + position)
                    }

                    position <= 0.1 -> {
                        // Current page (center)
                        page.alpha = 1f
                        page.scaleX = 1f
                        page.scaleY = 1f
                        page.translationY = 0f
                    }

                    position <= 1 -> {
                        // Page is slightly off-screen (peek from bottom)
                        page.alpha = 0.7f + (0.3f * (1 - position / 0.9f))
                        val scaleFactor = 0.8f + (0.2f * (1 - position / 0.9f))
                        page.scaleX = scaleFactor
                        page.scaleY = scaleFactor
                        page.translationY = 50 * position
                    }

                    else -> {
                        // Page is way off-screen to the bottom
                        page.alpha = 0f
                        page.scaleX = 0.8f
                        page.scaleY = 0.8f
                    }
                }
            }

            updateNavForPosition(0)
        }

        viewModel.optionSelectedLiveData.observe(this) {
            updateNavForPosition(binding.viewPager.currentItem)
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavForPosition(position)
            }
        })


        viewModel.quizDataSubmitted.observe(this) {
            it.getContent()?.let {
                mainViewModel.reloadTodaysData()
                if (viewModel.localDataStore.isCircadianOnboardShown()) {
                    navigateUpSafe()
                } else {
                    viewModel.localDataStore.setCircadianOnboardShown()
                    navigate(QuizCircadianFragmentDirections.actionQuizCircadianFragmentToCircadianAlignmentFragment())
                }
            }
        }

        // --
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

    }

}
