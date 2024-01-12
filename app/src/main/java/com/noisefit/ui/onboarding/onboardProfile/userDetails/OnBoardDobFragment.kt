package com.noisefit.ui.onboarding.onboardProfile.userDetails

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnBoardDobBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class OnBoardDobFragment :
    BaseFragment<FragmentOnBoardDobBinding>(FragmentOnBoardDobBinding::inflate) {

    private val viewModel: SetupProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_land_on_enter_age_page_visit)
        binding.lytOnBoardProgress.apply {
            pgBr.progress = 40
            tvCount.text = getString(R.string.text_2)
        }

        val c = Calendar.getInstance()
        c.add(Calendar.YEAR, -10)
        binding.datePicker.maxDate = c.timeInMillis

        binding.datePicker.init(
            viewModel.dobYear, viewModel.dobMonth, viewModel.dobDate
        ) { _, year, monthOfYear, dayOfMonth ->
            setDob(year, monthOfYear, dayOfMonth)

        }


        val userDob = viewModel.getLocalUserDob()
        if (userDob.isNotEmpty()) {
            try {
                if (!viewModel.isLocalDateSet) {
                    val dateArray = userDob.split("-")
                    binding.datePicker.updateDate(
                        dateArray[0].toInt(),
                        dateArray[1].toInt() - 1,
                        dateArray[2].toInt()
                    )
                    viewModel.isLocalDateSet = true
                }

            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
    }

    private fun setDob(year: Int, month: Int, day: Int) {
        viewModel.setDob(year, month, day)
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnContinue.setOnClickListener {
            viewModel.saveUserInfoLocally()
//            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CONTINUE_ENTER_AGE_CLICK)
            navigate(R.id.onBoardGenderFragment)
        }
    }

    override fun subscribeObservers() {

    }
}

