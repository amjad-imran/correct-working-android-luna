package com.oreo.ui.helpsupport

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHelpAndSupportBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OHSModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OHelpAndSupportFragment :
    BaseFragment<FragmentOHelpAndSupportBinding>(FragmentOHelpAndSupportBinding::inflate) {
    private val mViewModel: OHealthSupportViewModel by viewModels()
    private val mAdapter: OHealthSupportAdapter by lazy {
        OHealthSupportAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        mViewModel.getHSCategories()

    }

    private fun setRecycler() {
        with(binding.rvQueries) {
            adapter = mAdapter
        }
//        mAdapter.setData(mViewModel.getDummyHelpData())
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_help)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.tvDeviceName.text = mViewModel.deviceN
        binding.tvDeviceVersion.text = mViewModel.osVersion


    }

    override fun subscribeObservers() {
        mViewModel.hsCategoriesData.observe(this) {
            if (it.size > 0) {
                mAdapter.setData(it as ArrayList<OHSModel>)
            }
        }
        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        mViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }


}