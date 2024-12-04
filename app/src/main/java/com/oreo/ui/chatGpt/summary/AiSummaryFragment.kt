package com.oreo.ui.chatGpt.summary

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit.luna.R
import com.noisefit.luna.databinding.AiShareTemplate1Binding
import com.noisefit.luna.databinding.FragmentAiSummaryBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.utils.CommonConstants.FILE_PROVIDER
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.AiDailySummaryModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt


@AndroidEntryPoint
class AiSummaryFragment :
    BaseFragment<FragmentAiSummaryBinding>(FragmentAiSummaryBinding::inflate) {

    private var gestureDetector: GestureDetector? = null
    private val viewModel: AiSummaryViewModel by viewModels()
    private val storyHandler: Handler = Handler()
    private var storyRunnable: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGestures()
        viewModel.getSummaryData()
    }

    override fun subscribeObservers() {
        viewModel.dailySummaryData.observe(this) {
            it.getContent()?.let {

                viewModel.currentStoryIndex = 0
                viewModel.isPaused = false
                setUpViewPager(it)
            }
        }
    }

    private fun setUpViewPager(data: List<AiDailySummaryModel>) {
        val adapter = AiSummaryPagerAdapter(childFragmentManager, lifecycle)
        binding.storyViewPager.setAdapter(adapter)
        binding.storyViewPager.setUserInputEnabled(false)

        val dataSet = ArrayList<Fragment>()
        data.forEach {
            dataSet.add(AiSummaryDataFragment.getInstance(it))
        }
        adapter.setDataSet(dataSet)

        val spacing = 8
        val height = 2f.dpToPixel().roundToInt()

        val inflater = LayoutInflater.from(context)

        binding.progressLayout.removeAllViews()
        viewModel.progressIndicators.clear()

        for (i in 0 until dataSet.size) {
            val progressIndicator =
                inflater.inflate(
                    R.layout.progress_bar_horizontal,
                    binding.progressLayout,
                    false
                ) as ProgressBar

            progressIndicator.layoutParams = LinearLayout.LayoutParams(
                0, height, 1f
            ).apply {
                if (i > 0) {
                    this.setMargins(spacing, 0, 0, 0)
                }
            }

            progressIndicator.max = 100
            binding.progressLayout.addView(progressIndicator)

            viewModel.progressIndicators.add(progressIndicator)
        }

        startStoryProgress()
    }

    override fun onPause() {
        super.onPause()
        pauseStoryProgress()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.isPaused = false
        clearRunnable()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        var isLongPressActive = false
        gestureDetector = GestureDetector(
            this@AiSummaryFragment.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    val width: Int = binding.vGestureOverlay.width
                    if (e.x < width / 2) {
                        clearRunnable()
                        goToPreviousStory()
                    } else {
                        clearRunnable()
                        goToNextStory()
                    }
                    return true
                }


                override fun onLongPress(e: MotionEvent) {
                    isLongPressActive = true
                    pauseStoryProgress()
                }
            })

        binding.vGestureOverlay.setOnTouchListener { v, event ->
            gestureDetector?.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                if (isLongPressActive) {
                    isLongPressActive = false
                    resumeStoryProgress()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        if(viewModel.isPaused){
            resumeStoryProgress()
        }
    }

    private fun startStoryProgress() {
        storyRunnable = object : Runnable {
            override fun run() {
                if (!viewModel.isPaused) {
                    val progress =
                        viewModel.progressIndicators[viewModel.currentStoryIndex].progress
                    if (progress < 100) {
                        viewModel.progressIndicators[viewModel.currentStoryIndex].progress =
                            progress + 1
                        storyHandler.postDelayed(this, viewModel.storyDuration / 100)
                    } else {
                        clearRunnable()
                        goToNextStory()
                    }
                }
            }
        }
        storyRunnable?.let {
            storyHandler.post(it)
        }
    }

    private fun pauseStoryProgress() {
        viewModel.isPaused = true
        storyRunnable?.let {
            storyHandler.removeCallbacks(it)
        }
    }

    private fun resumeStoryProgress() {
        viewModel.isPaused = false
        storyRunnable?.let {
            storyHandler.post(it)
        }
    }

    private fun goToNextStory() {
        if (viewModel.currentStoryIndex < viewModel.progressIndicators.size - 1) {
            viewModel.progressIndicators[viewModel.currentStoryIndex].progress = 100
            viewModel.currentStoryIndex += 1
            binding.storyViewPager.setCurrentItem(viewModel.currentStoryIndex, true)
            startStoryProgress()
        } else {
            navigateUpSafe()
            // finish() // End of stories
        }
    }

    fun clearRunnable(){
        storyRunnable?.let {
            storyHandler.removeCallbacks(it)
        }
    }

    private fun goToPreviousStory() {
        if (viewModel.currentStoryIndex > 0) {
            viewModel.progressIndicators[viewModel.currentStoryIndex].progress = 0
            viewModel.currentStoryIndex -= 1
            viewModel.progressIndicators[viewModel.currentStoryIndex].progress = 0

            binding.storyViewPager.setCurrentItem(viewModel.currentStoryIndex, true)
            startStoryProgress()
        }
    }

    override fun initListener() {
        binding.ivClose.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivShare.setOnClickListener {
            viewModel.getCurrentStoryData()?.let {
                navigate(R.id.shareSummaryFragment, bundleOf("data" to it))
            }

            return@setOnClickListener


        }
    }


    private inner class AiSummaryPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        val fragments = ArrayList<Fragment>()
        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]

        fun setDataSet(fragments: List<Fragment>) {
            this.fragments.clear()
            this.fragments.addAll(fragments)
            notifyDataSetChanged()
        }

    }

}