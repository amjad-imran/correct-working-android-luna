package com.noisefit.ui.settings

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSettingsBinding
import com.noisefit.ui.profile.ProfileEditViewModel
import com.noisefit.ui.settings.dataSharingVendors.DataSharingVendorsAdapter
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.dataSharingVendorModels.DataSharingListEnum
import com.oreo.data.model.dataSharingVendorModels.DataSharingVendorListResponseItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel: ProfileEditViewModel by viewModels()

    private val vendorsAdapter by lazy {
        DataSharingVendorsAdapter() {
            handleDataSharingItemClick(it)
        }
    }

    private fun handleDataSharingItemClick(item: DataSharingVendorListResponseItem) {
        when(item.type){
            DataSharingListEnum.GOOGLE_FIT -> {
                navigate(R.id.googleFitFragmentOreo)
            }

            DataSharingListEnum.DYNAMIC_ITEM -> {
                navigate(
                    R.id.dataSharingVendorDetailFragment,
                    bundleOf(
                        "data" to item
                    )
                )
            }

            null -> {}
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_settings)

        setRecycler()
        viewModel.getDataSharingVendorList()
    }

    private fun setRecycler() {
        binding.lytGroup2.rvDataSharingVendors.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = vendorsAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        binding.lytGroup1.tvUnitValue.text = if (viewModel.getUnitValue() == Units.METRIC) {
            getString(R.string.text_metric)
        } else {
            getString(R.string.text_imperial)
        }

    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytGroup1.tvUnits.setOnClickListener {
            navigate(R.id.unitSelectionFragment)
        }
        binding.lytGroup1.tvNotifications.setOnClickListener {
            navigate(R.id.notificationSettingFragment)
        }
       /* binding.lytGroup2.tvGoogleFit.setOnClickListener {
            navigate(R.id.googleFitFragmentOreo)
        }*/


    }

    override fun subscribeObservers() {
        viewModel.vendorsList.observe(this){
            vendorsAdapter.updateItems(it)
        }
    }


}