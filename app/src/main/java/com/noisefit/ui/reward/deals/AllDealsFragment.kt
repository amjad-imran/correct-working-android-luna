package com.noisefit.ui.reward.deals

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.model.CouponList
import com.noisefit.databinding.FragmentAllDealsBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AllDealsFragment : BaseFragment<FragmentAllDealsBinding>(FragmentAllDealsBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    private val mViewModel: AllDealsViewModel by viewModels()
    private val mAdapter: AllDealsAdapter by lazy {
        AllDealsAdapter(object : AllDealsAdapter.OnDealsItemClickListener {
            override fun onDealsClick(resultData: CouponList) {
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.ALL_DEAL_ITEM_CLICK,
                    HashMap<String, Any>().apply {
                        this["title"] = resultData.title ?: ""
//                        this["brand"] = resultData.brand ?: ""//todo once value came from backend, will uncomment
                    })
                navigate(R.id.voucherDetailsFragment, Bundle().apply {
                    putString("comeFrom", "coupon")
                    putInt("id", resultData.id ?: -1)
                })
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.ALL_DEAL_LANDING_PAGE_VISIT)
        setRecycler()
        mViewModel.getAllDeals()
    }

    private fun setRecycler() {
        with(binding.rvDeals) {
            layoutManager = LinearLayoutManager(binding.rvDeals.context)
            adapter = mAdapter
        }
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_all_deals_page_title)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.textView1.setOnClickListener {
            navigate(AllDealsFragmentDirections.actionAllDealsFragmentToMyVoucherFragment())
        }
        binding.lytToolbar.vCoinsBack.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        mViewModel.dealsList.observe(this) {
            binding.lytToolbar.tvCoins.text = it.points.toString()
            if (it.couponList.isEmpty()) {
                mAdapter.setDataSet(mViewModel.getEmptyCoinData())
            } else {
                mAdapter.setDataSet(it.couponList)
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