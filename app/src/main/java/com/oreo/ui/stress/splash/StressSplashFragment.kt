package com.oreo.ui.stress.splash

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentStressSplashBinding
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.StressSplashModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class StressSplashFragment :
    BaseFragment<FragmentStressSplashBinding>(FragmentStressSplashBinding::inflate) {

    private val stressSplashDescriptionAdapter by lazy {
        StressSplashDescriptionAdapter()
    }
    private var dataList: ArrayList<StressSplashModel> = ArrayList()

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        dataList = getData()
        setViewpager()
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                localDataStore.setStressWalkthroughShown(true)
                navigateUpSafe()
            }
        }

    private fun setViewpager() {
        binding.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            adapter = stressSplashDescriptionAdapter

        }

        dataList.let { stressSplashDescriptionAdapter.setDataSet(it) }
        binding.vpImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                setProgress(position)


            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

        binding.vpImageSlider.setCurrentItem(0, false)

    }

    private fun setProgress(position: Int) {
        val max = dataList.size

        binding.lytProgress.apply {
            pgBr.progress = (((position + 1).toFloat() / max) * 100).roundToInt()
            tvCount.text = "0${position + 1}"
        }
    }

    private fun getNext(): Int {
        return binding.vpImageSlider.currentItem + 1
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            localDataStore.setStressWalkthroughShown(true)
            navigateUpSafe()
        }
        binding.bNext.setOnClickListener {
            val current = binding.vpImageSlider.currentItem
            if (current == (dataList.size - 1)) {
                localDataStore.setStressWalkthroughShown(true)
                navigateUpSafe()
                return@setOnClickListener
            }
            binding.vpImageSlider.setCurrentItem(getNext(), true)
        }
    }

    override fun subscribeObservers() {
    }


    private fun getData(): ArrayList<StressSplashModel> {
        val dataList = ArrayList<StressSplashModel>()

        dataList.add(
            StressSplashModel(
                getString(R.string.text_stress_w_title_1),
                R.drawable.image_stress_w_1,
                getString(R.string.text_stress_w_content_1)
            )
        )
        dataList.add(
            StressSplashModel(
                getString(R.string.text_stress_w_title_2),
                R.drawable.image_stress_w_2,
                getString(R.string.text_stress_w_content_2)
            )
        )
        dataList.add(
            StressSplashModel(
                getString(R.string.text_stress_w_title_4),
                R.drawable.image_stress_w_4,
                getString(R.string.text_stress_w_content_4)
            )
        )
        dataList.add(
            StressSplashModel(
                getString(R.string.text_stress_w_title_5),
                R.drawable.image_stress_w_5,
                getString(R.string.text_stress_w_content_5)
            )
        )

        dataList.add(
            StressSplashModel(
                getString(R.string.text_stress_w_title_3),
                R.drawable.image_stress_w_3,
                getString(R.string.text_stress_w_content_3)
            )
        )

        return dataList
    }
}

