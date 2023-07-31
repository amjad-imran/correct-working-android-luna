package com.noisefit.ui.shop

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit_commans.data.model.ShopProduct
import com.noisefit.luna.databinding.FragmentShopSearchBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.shop.adapter.ProductAction
import com.noisefit.ui.shop.adapter.SearchProductAdapter
import com.noisefit.ui.web.WebViewActivity
import dagger.hilt.android.AndroidEntryPoint
import com.noisefit_commans.utils.InsiderAppEvents


@AndroidEntryPoint
class ShopSearchFragment :
    BaseFragment<FragmentShopSearchBinding>(FragmentShopSearchBinding::inflate) {
    private val viewModel: ShopViewModel by viewModels()

    private val shopSearchAdapter: SearchProductAdapter by lazy {
        SearchProductAdapter(object : ProductAction {
            override fun onProductClicked(product: ShopProduct) {

                activity?.let {
                    startActivity(
                        WebViewActivity.getStartIntent(
                            it,
                            product.title,
                            product.getProductUrl()
                        )
                    )
                }

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()


        binding.etSearch.requestFocus()
        val imm: InputMethodManager? =
            requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setRecycler() {
        binding.rvSearch.layoutManager = GridLayoutManager(context, 2)
        binding.rvSearch.adapter = shopSearchAdapter
    }

    override fun initListener() {
        binding.bBack.setOnClickListener {
            navigateUpSafe()
        }

        binding.etSearch.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                uiController.hideSoftKeyboard()
                viewModel.searchProduct(binding.etSearch.text.toString().trim())


                return@OnKeyListener true
            }
            false
        })
    }

    override fun subscribeObservers() {
        viewModel.searchProductResult.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.textNoProduct.visible()
            } else {
                binding.textNoProduct.gone()
            }
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.SEARCH_BAR_RESULTED)
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