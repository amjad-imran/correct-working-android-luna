package com.noisefit.ui.onboarding.onboardProfile.userDetails

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnBoardGenderBinding
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit_commans.models.Gender
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardGenderFragment :
    BaseFragment<FragmentOnBoardGenderBinding>(FragmentOnBoardGenderBinding::inflate) {

    private val viewModel: SetupProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_land_on_enter_gender_page_visit)
        binding.lytOnBoardProgress.apply {
            pgBr.progress = viewModel.getProgress(3)
            tvCount.text = getString(R.string.text_3)
        }


        if (viewModel.gender.value == null) {
            val localGender = viewModel.getLocalUserGender()
            if (localGender.equals("male", true)) {
                viewModel.setGender(Gender.MALE)
            } else if (localGender.equals("female", true)) {
                viewModel.setGender(Gender.FEMALE)
            } else if (localGender.equals("other", true)) {
                viewModel.setGender(Gender.OTHER)
            } else if (localGender.equals("noToSay", true)) {
                viewModel.setGender(Gender.NotToSay)
            }
        }
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnContinue.setOnClickListener {
            viewModel.saveUserInfoLocally()
//            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTINUE_ENTER_GENDER_CLICK)
            navigate(R.id.onBoardHeightFragment)
        }


        binding.radioGender.tvMan.setOnClickListener {
            viewModel.setGender(Gender.MALE)
            logMoEngageEvent(Gender.MALE.type)
        }
        binding.radioGender.tvWoman.setOnClickListener {
            viewModel.setGender(Gender.FEMALE)
            logMoEngageEvent(Gender.FEMALE.type)
        }
        binding.radioGender.tvOther.setOnClickListener {
            viewModel.setGender(Gender.OTHER)
            logMoEngageEvent(Gender.OTHER.type)
        }
        binding.radioGender.tvPreferNoToSay.setOnClickListener {
            viewModel.setGender(Gender.NotToSay)
            logMoEngageEvent(Gender.NotToSay.type)
        }
    }

    private fun logMoEngageEvent(genderValue: String) {
        viewModel.sessionManager.logMoEngageAppEvent(
            MoEngageLunaAppEvents.luna_gender_select_click,
            HashMap<String, Any>().apply {
                this["gender"] = genderValue
            })

    }

    override fun subscribeObservers() {

        viewModel.gender.observe(this) {
            if (it == null) {
                binding.btnContinue.disable()
            } else {
                binding.btnContinue.enable()

                when (it) {
                    Gender.MALE -> setSelectedGender(0)
                    Gender.FEMALE -> setSelectedGender(1)
                    Gender.OTHER -> setSelectedGender(2)
                    Gender.NotToSay -> setSelectedGender(3)
                }

            }
        }
    }

    /**
     * 0->Man
     * 1->Woman
     * 2->Other
     */
    private fun setSelectedGender(selectedGender: Int) {
        when (selectedGender) {
            0 -> {
                binding.radioGender.ivMan.isChecked = true
                binding.radioGender.ivWoman.isChecked = false
                binding.radioGender.ivOther.isChecked = false
                binding.radioGender.ivPreferNoToSay.isChecked = false
            }

            1 -> {
                binding.radioGender.ivMan.isChecked = false
                binding.radioGender.ivWoman.isChecked = true
                binding.radioGender.ivOther.isChecked = false
                binding.radioGender.ivPreferNoToSay.isChecked = false
            }

            2 -> {
                binding.radioGender.ivMan.isChecked = false
                binding.radioGender.ivWoman.isChecked = false
                binding.radioGender.ivOther.isChecked = true
                binding.radioGender.ivPreferNoToSay.isChecked = false
            }

            3 -> {
                binding.radioGender.ivMan.isChecked = false
                binding.radioGender.ivWoman.isChecked = false
                binding.radioGender.ivOther.isChecked = false
                binding.radioGender.ivPreferNoToSay.isChecked = true
            }
        }
    }
}


