package com.oreo.ui.device.surgeCase

import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSurgeCaseOnboardingBinding
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.surgeCase.AboutSurgeCaseModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SurgeCaseOnboardingFragment : BaseFragment<FragmentSurgeCaseOnboardingBinding>(FragmentSurgeCaseOnboardingBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
        setAdapterAndTabLayout()
    }

    private fun setAdapterAndTabLayout() {

        val aboutChargerItemsList = ArrayList<AboutSurgeCaseModel>().apply { 
            add(
                AboutSurgeCaseModel(
                    title = getString(R.string.text_about_surge_case_title_1),
                    description = getString(R.string.text_about_surge_case_desc_1),
                    imageCenter = R.drawable.image_about_surge_case_1,
                )
            )

            add(
                AboutSurgeCaseModel(
                    title = getString(R.string.text_about_surge_case_title_2),
                    description = getString(R.string.text_about_surge_case_desc_2),
                    imageTopSticked = R.drawable.image_about_surge_case_2,
                )
            )

            add(
                AboutSurgeCaseModel(
                    title = getString(R.string.text_about_surge_case_title_3),
                    description = getString(R.string.text_about_surge_case_desc_3),
                    imageClosedCharger = R.drawable.image_about_surge_case_3,
                )
            )
        }
        val adapter = AboutChargerAdapter(this, aboutChargerItemsList)
        val viewPager = binding.viewPager
        viewPager.adapter = adapter

        TabLayoutMediator(binding.pagerIndicator, binding.viewPager) { _, _ -> }.attach()
    }

    private fun setUi() {
        val videoUri = Uri.parse("android.resource://" + requireActivity().packageName + "/" + R.raw.portable_charger_onboarding)
        val videoView = binding.videoView

        videoView.setVideoURI(videoUri)
        videoView.start()

        // Optional: Handle video completion
        videoView.setOnCompletionListener {
            // Once the video completes, hide it
            videoView.gone()

            // Show the rest of the UI
            binding.scrollView.visible()
        }
        //

        binding.tvSkip.apply {
            paintFlags = binding.tvSkip.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            text = getString(R.string.text_skip2)
        }


        val meetText = getString(R.string.text_meet)
        val surgeCaseTxt = getString(R.string.text_surge_case)
        val originalText = "$meetText \n$surgeCaseTxt"
        val spannableString = SpannableString(originalText)

        val meetTextStartIdx = spannableString.indexOf(meetText)

        spannableString.setSpan(
            AbsoluteSizeSpan(40, true),
            meetTextStartIdx,
            meetTextStartIdx + meetText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvMeetSurgeCase.text = spannableString
    }

    override fun initListener() {
        binding.tvSkip.setOnClickListener {
            localDataStore.setPortableChargerOnboarding(true)
            navigate(R.id.action_surgeCaseOnboardingFragment_pop)
        }

        binding.btnDone.setOnClickListener {
            localDataStore.setPortableChargerOnboarding(true)
            navigate(R.id.action_surgeCaseOnboardingFragment_pop)
        }
    }

    override fun subscribeObservers() {

    }

}