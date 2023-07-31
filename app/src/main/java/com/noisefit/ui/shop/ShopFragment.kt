package com.noisefit.ui.shop

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.MainActivity
import com.noisefit.luna.R
import com.noisefit_commans.data.model.ShopBanner
import com.noisefit_commans.data.model.ShopCategory
import com.noisefit_commans.data.model.ShopProduct
import com.noisefit.luna.databinding.FragmentShopBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.getDeeplinkPathArg
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.shop.adapter.*
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopFragment : BaseFragment<FragmentShopBinding>(
    FragmentShopBinding::inflate
) {

    private val viewModel: ShopViewModel by viewModels()

    private val imageSliderAdapter: BannerSliderAdapter by lazy {
        BannerSliderAdapter(object : BannerAction {
            override fun onBannerClicked(banner: ShopBanner, position: Int) {
                openSliderBannerUrl(banner, position)
            }
        })
    }

    private val categoriesAdapter: CategoriesAdapter by lazy {
        CategoriesAdapter(object : CategoryAction {
            override fun onCategoryClicked(shopCategory: ShopCategory) {
                navigate(
                    ShopFragmentDirections.actionNavigationShopToShopCategoryDataListingFragment(
                        shopCategory
                    )
                )
            }
        })
    }
    private val popularProductAdapter: PopularProductAdapter by lazy {
        PopularProductAdapter(object : ProductAction {
            override fun onProductClicked(product: ShopProduct) {
                activity?.let {
                    startActivity(
                        WebViewActivity.getStartIntent(
                            it,
                            product.title,
                            product.getProductUrl() + "?utm_source=Abanner" + "&utm_medium=Popular-Products" + "&utm_campaign=" + product.title
                        )
                    )
                }
            }
        })
    }
    private val recommendedProductAdapter: ProductRecommendedAdapter by lazy {
        ProductRecommendedAdapter(object : ProductAction {
            override fun onProductClicked(product: ShopProduct) {

                activity?.let {
                    startActivity(
                        WebViewActivity.getStartIntent(
                            it,
                            product.title,
                            product.getProductUrl() + "?utm_source=Abanner" + "&utm_medium=Our-Recommendations" + "&utm_campaign=" + product.title
                        )
                    )
                }
            }

        })
    }

    private val bestSellingAdapter: BestSellingProductAdapter by lazy {
        BestSellingProductAdapter(object : ProductAction {
            override fun onProductClicked(product: ShopProduct) {

                activity?.let {
                    startActivity(
                        WebViewActivity.getStartIntent(
                            it,
                            product.title,
                            product.getProductUrl() + "?utm_source=Abanner" + "&utm_medium=Best-Selling-Products" + "&utm_campaign=" + product.title
                        )
                    )
                }
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            //Parsing deeplink
            val deepLinkKey = it.getDeeplinkPathArg()
            if (!deepLinkKey.isNullOrEmpty()) {
                (activity as MainActivity).setSelectShop()
            }

        }
        setRecycler()
        setImageSlider()
        //viewModel.getShopDashboardData()
        viewModel.getCategories()
        viewModel.getPopularProduct()
        viewModel.getBestSellingProduct()
        viewModel.getRecommendedProduct()


    }

    private fun setRecycler() {
        binding.rvCategories.layoutManager = GridLayoutManager(context, 2)
        binding.rvCategories.adapter = categoriesAdapter

        binding.rvPopularProducts.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPopularProducts.adapter = popularProductAdapter


        binding.rvRecommendedProduct.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommendedProduct.adapter = recommendedProductAdapter

        binding.rvBestSelling.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBestSelling.adapter = bestSellingAdapter


    }

    private fun setImageSlider() {
        binding.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(imageTransformer)
            adapter = imageSliderAdapter
        }
        TabLayoutMediator(binding.imageSliderTabs, binding.vpImageSlider) { _, _ -> }.attach()

    }

    private val imageTransformer = CompositePageTransformer().apply {
        addTransformer(MarginPageTransformer(40))
    }

    override fun initListener() {
        binding.ivSearch.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.SEARCH_BAR_CLICKED)
            navigate(R.id.shopSearchFragment)
        }

        binding.ivBanner1.setOnClickListener {
            viewModel.banner1.value?.let {

                openBannerUrl(it, 1)
            }
        }
        binding.ivBanner2.setOnClickListener {
            viewModel.banner2.value?.let {

                openBannerUrl(it, 2)
            }
        }
    }

    fun openBannerUrl(it: ShopBanner, bannerNo: Int) {
        activity?.let { act ->
            startActivity(
                WebViewActivity.getStartIntent(
                    act,
                    "Shop",
                    it.getFullUrl() + "?utm_source=Abanner" + "&utm_medium=Top-Banner-" + bannerNo + "&utm_campaign="
                )
            )
        }

    }

    fun openSliderBannerUrl(it: ShopBanner, pos: Int) {
        activity?.let { act ->
            startActivity(
                WebViewActivity.getStartIntent(
                    act,
                    "Shop",
                    it.getFullUrl() + "?utm_source=Abanner" + "&utm_medium=Slider-" + (pos + 1) + "&utm_campaign="
                )
            )
        }

    }

    override fun subscribeObservers() {

        viewModel.categoryData.observe(viewLifecycleOwner) {
            binding.scrollViewMain.visible()
            categoriesAdapter.setDataSet(it)
        }
        viewModel.popularProduct.observe(viewLifecycleOwner) {
            popularProductAdapter.setDataSet(it)
        }
        viewModel.recommendedProduct.observe(viewLifecycleOwner) {
            recommendedProductAdapter.setDataSet(it)
        }
        viewModel.bestSellingProduct.observe(viewLifecycleOwner) {
            bestSellingAdapter.setDataSet(it)
        }
        viewModel.banner1.observe(viewLifecycleOwner) {
            Glide.with(requireContext())
                .load(it.img)
                .into(binding.ivBanner1)
        }
        viewModel.banner2.observe(viewLifecycleOwner) {
            Glide.with(requireContext())
                .load(it.img)
                .into(binding.ivBanner2)
        }
        viewModel.sliderBanners.observe(viewLifecycleOwner) {
            imageSliderAdapter.setDataSet(it)
        }
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

}