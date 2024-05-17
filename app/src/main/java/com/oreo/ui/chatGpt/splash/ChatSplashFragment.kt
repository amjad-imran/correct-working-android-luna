package com.oreo.ui.chatGpt.splash

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
class ChatSplashFragment :
    BaseFragment<FragmentStressSplashBinding>(FragmentStressSplashBinding::inflate) {

    private val stressSplashDescriptionAdapter by lazy {
        ChatSplashDescriptionAdapter()
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
                onBackPress()
            }
        }

    private fun setViewpager() {
        binding.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            adapter = stressSplashDescriptionAdapter
            setOnTouchListener(null)
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

    fun onNextPress() {
        val current = binding.vpImageSlider.currentItem
        if (current == (dataList.size - 1)) {
            localDataStore.setAiChatSplashShown()
            navigate(ChatSplashFragmentDirections.actionChatSplashFragmentToChatGptFragment())
        } else {
            binding.vpImageSlider.setCurrentItem(current + 1, true)
        }
    }

    override fun subscribeObservers() {
    }


    private fun getData(): ArrayList<StressSplashModel> {
        val dataList = ArrayList<StressSplashModel>()

        dataList.add(
            StressSplashModel(
                "Ask Any Question",
                R.raw.anim_chat_onboard_1,
                getString(R.string.text_ai_content_1)
            )
        )
        dataList.add(
            StressSplashModel(
                "Learn More About Your Body",
                R.raw.anim_chat_onboard_1,
                getString(R.string.text_ai_content_2)
            )
        )
        dataList.add(
            StressSplashModel(
                "Your personal fitness coach",
                R.raw.anim_chat_onboard_1,
                getString(R.string.text_ai_content_3)
            )
        )

        return dataList
    }
}

