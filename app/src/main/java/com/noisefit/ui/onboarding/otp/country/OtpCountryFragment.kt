package com.noisefit.ui.onboarding.otp.country

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.response.Country
import com.noisefit.luna.databinding.FragmentOtpCountryBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.onboarding.auth.AuthViewModel
import com.noisefit.ui.onboarding.otp.OtpViewModel
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit_commans.ui.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class OtpCountryFragment :
    BaseFragment<FragmentOtpCountryBinding>(FragmentOtpCountryBinding::inflate),
    CountryInteractionListener {

    //    private val welcomeViewModel: WelcomeViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()


    private val countryAdapter by lazy {
        CountryAdapter(this)
    }
    private val viewModel: OtpViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setCountries(ArrayList())
        /*viewModel.setCountries(welcomeViewModel.countryList)*/
        setUpCountryList()
    }

    private fun setUpCountryList() {
        binding.lytCountrySelection.rvCountries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = countryAdapter
        }
        countryAdapter.setDataSet(viewModel.countryList)
        binding.lytCountrySelection.lytCountrySearch.etSearchCountry.afterTextChanged {
            countryAdapter.filter.filter(it)
        }

    }

    override fun initListener() {

        binding.container.setOnClickListener {
            if (binding.lytCountrySelection.root.visibility == View.VISIBLE) {
                binding.lytCountrySelection.root.gone()
                binding.tvSubHeading.visible()
            } else {
                binding.lytCountrySelection.root.visible()
                binding.tvSubHeading.gone()
            }
        }
        binding.bContinue.setOnClickListener {

            if (viewModel.isNumberSupported()) {
                navigate(R.id.otpNumberFragment)
            } else {
                // authViewModel.createNewUser()
            }
        }
    }

    override fun subscribeObservers() {

        viewModel.selectedCountry.observe(this) {
            if (!it.code.isNullOrEmpty()) {
                binding.tvSelectedCountry.text = it.name?.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(
                        Locale.getDefault()
                    ) else char.toString()
                }
                binding.lytCountrySelection.root.gone()
                binding.tvSubHeading.visible()
                binding.bContinue.enable()
            }
        }


    }

    override fun onCountrySelected(country: Country) {
        viewModel.setSelectedCountry(country)
    }

}