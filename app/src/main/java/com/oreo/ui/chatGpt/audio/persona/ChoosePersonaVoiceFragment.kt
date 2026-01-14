package com.oreo.ui.chatGpt.audio.persona

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.databinding.FragmentChoosePersonaVoiceBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChoosePersonaVoiceFragment :
    BaseFragment<FragmentChoosePersonaVoiceBinding>(FragmentChoosePersonaVoiceBinding::inflate) {
    private val viewModel: ChoosePersonaVoiceViewModel by viewModels()
    private var mediaPlayer: MediaPlayer? = null
    private val mAdapter by lazy {
        ChoosePersonaVoiceVpAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaPlayer = MediaPlayer()
        setUpViewPager()
    }
    private fun setUpViewPager() {
        binding.viewPager.apply {
            adapter = mAdapter
            (getChildAt(0) as RecyclerView).apply {
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
            registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        viewModel.personaData.value?.getOrNull(position)?.voiceUrl?.let { persona ->
                            playMusic(persona)
                        }
                    }
                }
            )
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3

            setPadding(255, 0, 255, 0)
            setPageTransformer(
                CompositePageTransformer().apply {
                    addTransformer(MarginPageTransformer(24))

                    addTransformer { page, position ->
                        val scale = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
                        page.scaleX = scale
                        page.scaleY = scale
                        page.alpha = 0.7f + (1 - kotlin.math.abs(position)) * 0.3f
                    }
                }
            )

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
        }
    }

    override fun initListener() {
        binding.ivCross.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.personaData.observe(this){
            mAdapter.updateDataSet(it)
        }
    }

    override fun onPause() {
        mediaPlayer?.stop()
        super.onPause()
    }

    private fun playMusic(songUrl: String) {
        mediaPlayer?.stop()
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(songUrl)
            setOnPreparedListener { start() }
            prepareAsync()
        }
    }
}