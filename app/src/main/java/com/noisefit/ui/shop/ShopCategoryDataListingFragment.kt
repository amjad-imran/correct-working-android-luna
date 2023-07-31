package com.noisefit.ui.shop

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.model.ShopProduct
import com.noisefit.luna.databinding.FragmentShopCategoryDataListingBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.shop.adapter.ProductAction
import com.noisefit.ui.shop.adapter.SearchProductAdapter
import com.noisefit.ui.web.WebViewActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ShopCategoryDataListingFragment :
    BaseFragment<FragmentShopCategoryDataListingBinding>(FragmentShopCategoryDataListingBinding::inflate) {
    private val viewModel: ShopViewModel by viewModels()
    private val shopSearchAdapter: SearchProductAdapter by lazy {
        SearchProductAdapter(object : ProductAction {

            override fun onProductClicked(product: ShopProduct) {

                val category = viewModel.selectedShopCategory?.title ?: ""
                startActivity(
                    WebViewActivity.getStartIntent(
                        requireActivity(),
                        product.title,
                        product.getProductUrl() + "?utm_source=Abanner" + "&utm_medium=Top-Category-" + category + "&utm_campaign=" + product.title
                    )
                )
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedShopCategory = arguments?.let {
            ShopCategoryDataListingFragmentArgs.fromBundle(it).shopCategory
        }
        binding.tvSort.text = viewModel.selectedSortMethod?.title ?: "Default"
        setRecycler()
        viewModel.selectedShopCategory?.let {
            binding.tvTitle.text = it.title
            viewModel.getProductFromCategory(it.collectionId)


        }

    }

    private fun setRecycler() {
        binding.rvSearch.layoutManager = GridLayoutManager(context, 2)
        binding.rvSearch.adapter = shopSearchAdapter
    }

    override fun initListener() {
        binding.bBack.setOnClickListener {
            navigateUpSafe()
        }
        binding.ivSearch.setOnClickListener {
            navigate(R.id.shopSearchFragment)
        }
        binding.tvSort.setOnClickListener {

            setFragmentResultListener(SHOP_FILTER_KEY) { key, bundle ->
                val sortMode = bundle.getSerializable("selectedMode") as SortModes
                viewModel.selectedSortMethod = sortMode
                binding.tvSort.text = sortMode.title



                viewModel.selectedShopCategory?.let {
                    viewModel.getProductFromCategory(it.collectionId)
                }

            }

            navigate(
                ShopCategoryDataListingFragmentDirections.actionShopCategoryDataListingFragmentToBottomSheetShopProductSort(
                    viewModel.selectedSortMethod ?: SortModes.A_TO_Z
                )
            )
        }

    }

    override fun subscribeObservers() {
        viewModel.searchProductResult.observe(viewLifecycleOwner) {
            shopSearchAdapter.setDataSet(it)
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }
}