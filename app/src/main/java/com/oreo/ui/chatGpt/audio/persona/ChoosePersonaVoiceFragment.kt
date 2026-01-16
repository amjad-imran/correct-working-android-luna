package com.oreo.ui.chatGpt.audio.persona

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentChoosePersonaVoiceBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChoosePersonaVoiceFragment :
    BaseFragment<FragmentChoosePersonaVoiceBinding>(FragmentChoosePersonaVoiceBinding::inflate) {
    private val viewModel: ChoosePersonaVoiceViewModel by viewModels()
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var currentPersona: String
    private lateinit var mAdapter: ChoosePersonaVoiceVpAdapter
    lateinit var exoPlayer : ExoPlayer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadPersonaData()
        exoPlayer =  ExoPlayer.Builder(requireContext()).build()
        mAdapter = ChoosePersonaVoiceVpAdapter()
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
                        viewModel.personaData.value?.getOrNull(position)?.let { persona ->
                            persona.persona_ai?.let { currentPersona = it }
                            persona.voiceUrl?.let { playMusic(it) }
                        }
                    }
                }
            )
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3

            setPadding(255, 40, 255, 40)
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
            enablePaddingSwipe()
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
        }
    }

    override fun initListener() {
        binding.ivCross.setOnClickListener {
            navigateUpSafe()
        }
        binding.tvSelect.setOnClickListener {
            viewModel.saveUserPersona(currentPersona)
            navigateUpSafe()
            if(arguments?.getBoolean("isFromVoiceChat", false) == false)
                navigate(R.id.lifeOsVoiceChatFragment)
        }
    }

    override fun subscribeObservers() {
        viewModel.personaData.observe(this){
            it.firstOrNull()?.voiceUrl?.let { url ->
                exoPlayer.apply {
                    setMediaItem(MediaItem.fromUri(url))
                    playWhenReady = true
                    prepare()
                }
            }
            mAdapter.updateDataSet(it)
        }
    }

    override fun onPause() {
        mediaPlayer?.stop()
        super.onPause()
    }

    override fun onStop() {
        mediaPlayer?.stop()
        super.onStop()
    }
    fun playMusic(url: String) {
        exoPlayer.apply {
            stop()
            setMediaItem(MediaItem.fromUri(url))
            playWhenReady = true
            prepare()
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
fun ViewPager2.enablePaddingSwipe() {
    val recyclerView = getChildAt(0) as RecyclerView
    setOnTouchListener { _, event ->
        recyclerView.dispatchTouchEvent(event)
        true
    }
}