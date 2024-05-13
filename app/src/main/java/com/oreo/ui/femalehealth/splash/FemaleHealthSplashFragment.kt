package com.oreo.ui.femalehealth.splash

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFemaleHealthSplashBinding
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.StressSplashModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class FemaleHealthSplashFragment :
    BaseFragment<FragmentFemaleHealthSplashBinding>(FragmentFemaleHealthSplashBinding::inflate) {
    private val fmhSplashDescriptionAdapter by lazy {
        FMHSplashDescriptionAdapter()
    }
    private var dataList: ArrayList<StressSplashModel> = ArrayList()

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        dataList.clear()
        dataList.addAll(getData())
        setViewpager()
    }

    private fun setViewpager() {
        binding.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            adapter = fmhSplashDescriptionAdapter
            setOnTouchListener(null)
        }

        dataList.let { fmhSplashDescriptionAdapter.setDataSet(it) }
        binding.vpImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)


            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    dataList.size - 1 ->
                        binding.bNext.text = getString(R.string.text_continue)

                    else -> binding.bNext.text = getString(R.string.text_next)
                }
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
        /*if (position == max - 1) binding.lytProgress.root.gone() else
            binding.lytProgress.root.visible()*/
        binding.lytProgress.apply {
            pgBr.progress = (((position + 1).toFloat() / max) * 100).roundToInt()
            tvCount.text = "0${position + 1}"
        }
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPress()
            }
        }

    override fun initListener() {
        binding.vLeft.setOnClickListener {
            onBackPress()
        }
        binding.vRight.setOnClickListener {
            onNextPress()
        }

        binding.backBtn.setOnClickListener {
            onBackPress()
        }
        binding.bNext.setOnClickListener {
            onNextPress()
        }

    }

    fun onBackPress() {
        val current = binding.vpImageSlider.currentItem
        if (current != 0) {
            binding.vpImageSlider.setCurrentItem(current - 1, true)
        } else {
            navigateUpSafe()
        }
    }

    private fun onNextPress() {
        val current = binding.vpImageSlider.currentItem
        if (current == (dataList.size - 1)) {
            localDataStore.setFMHWalkthroughShown(true)
            navigate(FemaleHealthSplashFragmentDirections.actionFmhSplashFragmentToFragmentFemaleOnboarding())
        } else {
            binding.vpImageSlider.setCurrentItem(current + 1, true)
        }
    }


    override fun subscribeObservers() {

    }

    private fun getData(): List<StressSplashModel> {
        val dataList = ArrayList<StressSplashModel>()

        dataList.add(
            StressSplashModel(
                getString(R.string.text_fmh_w_title_1),
                R.drawable.ic_fmh_w_1,
                getString(R.string.text_fmh_w_content_1)
            )
        )
        dataList.add(
            StressSplashModel(
                getString(R.string.text_fmh_w_title_2),
                R.drawable.ic_fmh_w_2,
                getString(R.string.text_fmh_w_content_2)
            )
        )
        dataList.add(
            StressSplashModel(
                getString(R.string.text_fmh_w_title_3),
                R.drawable.ic_fmh_w_3,
                getString(R.string.text_fmh_w_content_3)
            )
        )
        dataList.add(
            StressSplashModel(
                getString(R.string.text_fmh_w_title_4),
                R.drawable.ic_fmh_w_4,
                getString(R.string.text_fmh_w_content_4)
            )
        )
        return dataList
    }
}