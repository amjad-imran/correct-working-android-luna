package com.noisefit.ui.settings.dataSharingVendors

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.FragmentDataSharingVendorDetailBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.dataSharingVendorModels.DataSharingVendorListResponseItem
import com.oreo.data.model.dataSharingVendorModels.Feature
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class DataSharingVendorDetailFragment : BaseFragment<FragmentDataSharingVendorDetailBinding>(FragmentDataSharingVendorDetailBinding::inflate) {

    private val args: DataSharingVendorDetailFragmentArgs by navArgs()
    private val viewModel: DataSharingVendorDetailViewModel by viewModels()

    private val itemsAdapter by lazy {
        DataSharingVendorsDetailAdapter()
    }

    private var isFirstTimeLoaded = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.mainData = args.data

        setUi(viewModel.mainData)
        viewModel.mainData?.features?.let { setRecycler(viewModel.mainData?.consent, it) }
    }

    private fun setUi(data: DataSharingVendorListResponseItem?) {
        binding.lytToolbarWithDetails.tvTitle.text = data?.vendorName
        binding.lytToolbarWithDetails.tvDesc.gone()

        binding.lytToolbarWithDetails.switchMain.apply {
            isChecked = data?.consent ?: false
            visible()
        }
    }

    private fun setRecycler(isChecked: Boolean?, features: List<Feature>) {
        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = itemsAdapter
            addItemDecoration(
                SpacingItemDecoration(20)
            )
        }
        itemsAdapter.isToggleOn = isChecked
        if(isChecked!=true){
            isFirstTimeLoaded = false
        }
        itemsAdapter.updateItems(features)
    }

    class SpacingItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = space

            if (parent.getChildAdapterPosition(view) == parent.adapter?.itemCount?.minus(1)) {
                outRect.bottom = 0
            }
        }
    }

    override fun initListener() {
        binding.lytToolbarWithDetails.switchMain.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isFirstTimeLoaded) {
                isFirstTimeLoaded = false
                return@setOnCheckedChangeListener
            }
            viewModel.submitDataSharingVendorToggleState(isChecked){
                updateFeaturesListToggle(isChecked)
            }
        }

        binding.lytToolbarWithDetails.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    private fun updateFeaturesListToggle(checked: Boolean) {
        viewModel.mainData?.features?.let {
            itemsAdapter.updateToggles(checked)
        }
    }

    override fun subscribeObservers() {
        //
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        //
    }

}