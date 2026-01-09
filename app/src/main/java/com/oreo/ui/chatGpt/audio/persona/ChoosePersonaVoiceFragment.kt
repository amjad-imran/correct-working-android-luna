package com.oreo.ui.chatGpt.audio.persona

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.databinding.FragmentChoosePersonaVoiceBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class ChoosePersonaVoiceFragment :
    BaseFragment<FragmentChoosePersonaVoiceBinding>(FragmentChoosePersonaVoiceBinding::inflate) {

    private val viewModel: ChoosePersonaVoiceViewModel by viewModels()
    private val mAdapter by lazy {
        ChoosePersonaVoiceVpAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewPager()
    }

    private fun setUpViewPager() {
        binding.viewPager.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })

            adapter = mAdapter


            // Get the RecyclerView and configure nested scrolling
            (getChildAt(0) as? RecyclerView)?.let { recyclerView ->
                recyclerView.apply {
                    isNestedScrollingEnabled = true

                    overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                    addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                        /*private var initialY = 0f

                        override fun onInterceptTouchEvent(
                            rv: RecyclerView,
                            e: MotionEvent
                        ): Boolean {
                            when (e.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    initialY = e.y

                                    parent.requestDisallowInterceptTouchEvent(false)
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    val dy = e.y - initialY
                                    if (abs(dy) > 10) {

                                        parent.requestDisallowInterceptTouchEvent(false)
                                    } else {

                                        parent.requestDisallowInterceptTouchEvent(true)
                                    }
                                }
                            }
                            return false
                        }

                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}*/
                        private var initialX = 0f
                        private var initialY = 0f
                        private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

                        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                            when (e.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    initialX = e.x
                                    initialY = e.y
                                    parent.requestDisallowInterceptTouchEvent(true)
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    val dx = abs(e.x - initialX)
                                    val dy = abs(e.y - initialY)

                                    // Only block parent if it's a horizontal gesture
                                    if (dx > touchSlop && dx > dy) {
                                        parent.requestDisallowInterceptTouchEvent(true) // Horizontal → block parent
                                    } else if (dy > touchSlop && dy > dx) {
                                        parent.requestDisallowInterceptTouchEvent(false) // Vertical → let parent handle
                                    }
                                }

                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    parent.requestDisallowInterceptTouchEvent(false)
                                }
                            }
                            return false
                        }

                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                    })
                }
            }


            //orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.personaData.observe(this){
            mAdapter.updateDataSet(it)
        }
    }

    /*private fun playMusic(songUrl: String){
        MediaPlayer().apply {
            setDataSource(songUrl)  // Set the URL of the song
            prepareAsync()  // Prepare the player asynchronously
            setOnPreparedListener {
                // Start playback once prepared
                start()
            }
        }.start()
    }*/

}