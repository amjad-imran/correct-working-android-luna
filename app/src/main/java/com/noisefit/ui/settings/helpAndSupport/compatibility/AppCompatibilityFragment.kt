package com.noisefit.ui.settings.helpAndSupport.compatibility

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.model.AppCompatibility
import com.noisefit.databinding.FragmentAppCompatibilityBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AppCompatibilityFragment : BaseFragment<FragmentAppCompatibilityBinding>(
    FragmentAppCompatibilityBinding::inflate
), AppCompatibilityInteractionListener {

    private val appCompatibilityAdapter by lazy {
        AppCompatibilityAdapter(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    private fun setAdapter() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appCompatibilityAdapter

        }
        appCompatibilityAdapter.setDataSet(getAppCompatibleList())
    }

    override fun initListener() {
        binding.btnAnyOtherQuery.setOnClickListener {
            navigateUpSafe()
        }
        binding.toolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_check_app_compatibility)
        }
        binding.tvShortAns.text = getString(R.string.text_check_the_app_compatible_with_your_device)
    }

    override fun subscribeObservers() {

    }

    override fun onAppCompatibilityClick(packageName: String) {
        ShareUtil.openPlayStore(requireContext(), packageName)
    }


    private fun getAppCompatibleList(): List<com.noisefit_commans.data.model.AppCompatibility> {
        val list = ArrayList<com.noisefit_commans.data.model.AppCompatibility>()
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Ace",
                ShareUtil.PACKAGE_ACE,
                R.drawable.ic_noisefit_ace,
                "NoiseFit Buzz"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "Pulse Buzz",
                ShareUtil.PACKAGE_PRIME,
                R.drawable.ic_noisefit_prime,
                "NoiseFit Prime"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Apex",
                ShareUtil.PACKAGE_APEX,
                R.drawable.ic_noisefit_apex,
                "NoiseFit Core"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Assist",
                ShareUtil.PACKAGE_ASSIST,
                R.drawable.ic_noisefit_assist,
                "ColorFit Pro4 Max"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Sync",
                ShareUtil.PACKAGE_SYNC,
                R.drawable.ic_noisefit_sync,
                "Core 2"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Sync",
                ShareUtil.PACKAGE_SYNC,
                R.drawable.ic_noisefit_sync,
                "Champ 2"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Sync",
                ShareUtil.PACKAGE_SYNC,
                R.drawable.ic_noisefit_sync,
                "Excel One Touch"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Track",
                ShareUtil.PACKAGE_TRACK,
                R.drawable.icon_noisefit_track,
                "Core 2 Buzz"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Assist",
                ShareUtil.PACKAGE_ASSIST,
                R.drawable.ic_noisefit_assist,
                "Pro 3 Alpha"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Track",
                ShareUtil.PACKAGE_TRACK,
                R.drawable.icon_noisefit_track,
                "Agile 2 Buzz"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Apex",
                ShareUtil.PACKAGE_APEX,
                R.drawable.ic_noisefit_apex,
                "Core Oxy"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Assist",
                ShareUtil.PACKAGE_ASSIST,
                R.drawable.ic_noisefit_assist,
                "ColorFit Pro3 Assist"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Assist",
                ShareUtil.PACKAGE_ASSIST,
                R.drawable.ic_noisefit_assist,
                "NoiseFit Champ Kids Band"
            )
        )
        list.add(
            com.noisefit_commans.data.model.AppCompatibility(
                "NoiseFit Assist",
                ShareUtil.PACKAGE_ASSIST,
                R.drawable.ic_noisefit_assist,
                "NoiseFit Active GPS"
            )
        )

        return list
    }
}