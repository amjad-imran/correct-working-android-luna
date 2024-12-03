package com.oreo.ui.chatGpt.summary

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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.noisefit.luna.databinding.AiShareTemplate1Binding
import com.noisefit.luna.databinding.FragmentAiSummaryBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.utils.CommonConstants.FILE_PROVIDER
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
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val adapter = AiSummaryPagerAdapter(childFragmentManager, lifecycle)
        binding.storyViewPager.setAdapter(adapter)
        binding.storyViewPager.setUserInputEnabled(false); // Disable swipe gestures

        val dataSet = arrayListOf(
            AiSummaryDataFragment.getInstance("One"), AiSummaryDataFragment.getInstance("Two"),
            AiSummaryDataFragment.getInstance("Three"), AiSummaryDataFragment.getInstance("Four")
        )

        adapter.setDataSet(
            dataSet
        )

        val spacing = 8

        for (i in 0 until dataSet.size) {
            val progressIndicator = LinearProgressIndicator(
                requireContext()
            )
            progressIndicator.layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
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

    private fun setupGestures() {
        var isLongPressActive = false
        gestureDetector = GestureDetector(
            this@AiSummaryFragment.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    val width: Int = binding.vGestureOverlay.width
                    if (e.x < width / 2) {
                        goToPreviousStory()
                    } else {
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
            /*if (event.getAction() === MotionEvent.ACTION_UP) {
                resumeStoryProgress()
            }*/

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (isLongPressActive) {
                    isLongPressActive = false; // Reset the flag
                    resumeStoryProgress()
                }
                resumeStoryProgress();
            }
            true
        }
    }

    private fun startStoryProgress() {
        storyRunnable = object : Runnable {
            override fun run() {
                if (!viewModel.isPaused) {
                    val progress =
                        viewModel.progressIndicators[viewModel.currentStoryIndex].progress
                    if (progress < 100) {
                        viewModel.progressIndicators.get(viewModel.currentStoryIndex)
                            .setProgress(progress + 1)
                        storyHandler.postDelayed(this, viewModel.storyDuration / 100)
                    } else {
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
            storyRunnable?.let {
                storyHandler.removeCallbacks(it)
            }
            viewModel.progressIndicators[viewModel.currentStoryIndex].setProgress(100)
            viewModel.currentStoryIndex += 1
            binding.storyViewPager.setCurrentItem(viewModel.currentStoryIndex, true)
            startStoryProgress()
        } else {
            // finish() // End of stories
        }
    }

    private fun goToPreviousStory() {
        if (viewModel.currentStoryIndex > 0) {
            storyRunnable?.let {
                storyHandler.removeCallbacks(it)
            }
            viewModel.progressIndicators[viewModel.currentStoryIndex].setProgress(0)
            viewModel.currentStoryIndex -= 1
            viewModel.progressIndicators[viewModel.currentStoryIndex].setProgress(0)

            binding.storyViewPager.setCurrentItem(viewModel.currentStoryIndex, true)
            startStoryProgress()
        }
    }

    override fun initListener() {
        binding.ivShare.setOnClickListener {

            context?.let {
                val bitmap = createImageFromLayout(it)
                val file = saveBitmapToFile(it, bitmap)
                shareImage(it, file)
            }

        }
    }

    override fun subscribeObservers() {

    }

    private fun shareImage(context: Context, imageFile: File?) {
        val uri = FileProvider.getUriForFile(
            context, FILE_PROVIDER,
            imageFile!!
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("image/png")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(intent, "Share Image"))
    }

    private fun createImageFromLayout(context: Context): Bitmap {
        val binding = AiShareTemplate1Binding.inflate(LayoutInflater.from(context))

        binding.title.text = "Custom Title"
        binding.data.text = "Dynamic data to include in the image."

        val width = 500f.dpToPixel().roundToInt()
        val height = 500f.dpToPixel().roundToInt()
        binding.root.layoutParams = ViewGroup.LayoutParams(width, height)
        binding.root.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        binding.root.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        binding.root.draw(canvas)

        return bitmap
    }

    //Move to BG thread
    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.getExternalFilesDir(null), "ai_shared.png")
        try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
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