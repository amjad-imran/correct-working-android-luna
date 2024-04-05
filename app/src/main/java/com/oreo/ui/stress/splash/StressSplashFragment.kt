package com.oreo.ui.stress.splash

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentStressSplashBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.Contributors
import com.oreo.data.model.StressSplashModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StressSplashFragment :
    BaseFragment<FragmentStressSplashBinding>(FragmentStressSplashBinding::inflate) {

    private val stressSplashDescriptionAdapter by lazy {
        StressSplashDescriptionAdapter()
    }
    private var pos: Int = -1
    private var isLast: Boolean = false
    private var isFirst: Boolean = false
    private var dataList: ArrayList<StressSplashModel>  = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataList = getData()
        setViewpager()
    }

    private fun setViewpager() {
        binding.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            adapter = stressSplashDescriptionAdapter

        }
        TabLayoutMediator(
            binding.tabLayout,
            binding.vpImageSlider
        ) { _, _ -> }.attach()
        dataList.let { stressSplashDescriptionAdapter.setDataSet(it) }
        binding.vpImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                LOGS.d("onPageScrolled $position")
                isLast = position == dataList.size - 1
                isFirst = position == 0

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

        if (pos == 0) {
            isFirst = true
        }
        binding.vpImageSlider.setCurrentItem(pos, false)


    }

    private fun getItem(i: Int): Int {
        return binding.vpImageSlider.currentItem + i
    }

    override fun initListener() {
        binding.bClose.setOnClickListener {
            navigateUpSafe()
        }
        binding.ivNext.setOnClickListener {
            if (isLast) {
                navigateUpSafe()
                return@setOnClickListener
            }
            binding.vpImageSlider.setCurrentItem(getItem(+1), true)


        }
        binding.ivPrevious.setOnClickListener {
            if (isFirst) {
                navigateUpSafe()
                return@setOnClickListener
            }
            binding.vpImageSlider.setCurrentItem(getItem(-1), true)
        }
    }

    override fun subscribeObservers() {
    }


    private fun getData(): ArrayList<StressSplashModel> {
        val dataList = ArrayList<StressSplashModel>()

        dataList.add(
            StressSplashModel(
                "What is stress?",
                R.drawable.bg_dummy_placeholder,
                "Stress is the body’s natural response to challenges. While commonly seen negatively, healthy levels can enhance focus, memory, and overall performance."
            )
        )
        dataList.add(
            StressSplashModel(
                "What is stress?",
                R.drawable.bg_dummy_placeholder,
                "Stress is the body’s natural response to challenges. While commonly seen negatively, healthy levels can enhance focus, memory, and overall performance."
            )
        )
        dataList.add(
            StressSplashModel(
                "What is stress?",
                R.drawable.bg_dummy_placeholder,
                "Stress is the body’s natural response to challenges. While commonly seen negatively, healthy levels can enhance focus, memory, and overall performance."
            )
        )
        dataList.add(
            StressSplashModel(
                "What is stress?",
                R.drawable.bg_dummy_placeholder,
                "Stress is the body’s natural response to challenges. While commonly seen negatively, healthy levels can enhance focus, memory, and overall performance."
            )
        )
        dataList.add(
            StressSplashModel(
                "What is stress?",
                R.drawable.bg_dummy_placeholder,
                "Stress is the body’s natural response to challenges. While commonly seen negatively, healthy levels can enhance focus, memory, and overall performance."
            )
        )

        return dataList
    }
}

