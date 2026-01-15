package com.oreo.ui.lifeos.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.setFragmentResult
import com.noisefit.data.local.dataStored.implementation.DataStoredImpl
import com.noisefit.luna.databinding.FragmentLifeOsOnboardFinishBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.lifeos.onboarding.LifeOsOnboardBeginFragment.Companion.LIFE_OS_ONBOARD_BEGIN_KEY
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LifeOsOnboardFinishFragment : BaseFragment<FragmentLifeOsOnboardFinishBinding>(FragmentLifeOsOnboardFinishBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredImpl

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                onBackPress()
            }

        val user = localDataStore.getUser()
        binding.tvUserName.text = user?.firstName ?: "User"

    }

    override fun initListener() {
        binding.btnLetsGo.setOnClickListener {
            sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.lifeos_onboarding_pickup
            )
            navigateUpSafe()
        }
        binding.ivBackBtn.setOnClickListener {
            onBackPress()
        }
    }

    private fun onBackPress(){
        setFragmentResult(
            LIFE_OS_ONBOARD_BEGIN_KEY,
            Bundle().apply {
                putBoolean("isBackClicked", true)
            }
        )
        navigateUpSafe()
    }

    override fun subscribeObservers() {

    }

}