package com.noisefit.ui.reward.voucher.active


import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentActiveVoucherBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.reward.voucher.VoucherSharedViewModel
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActiveVoucherFragment :
    BaseFragment<FragmentActiveVoucherBinding>(FragmentActiveVoucherBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    private val mViewModel: ActiveVoucherViewModel by viewModels()
    private val mSharedViewModel: VoucherSharedViewModel by activityViewModels()

    private val mAdapter: ActiveVoucherAdapter by lazy {
        ActiveVoucherAdapter(object : ActiveVoucherAdapter.OnRedeemClickListener {
            override fun onItemClick(voucherId: Int) {
                sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_VOUCHER_PAGE_REDEEM_CLICK)
                navigate(R.id.voucherDetailsFragment, Bundle().apply {
                    putString("comeFrom", "voucher")
                    putInt("id", voucherId)
                })
            }


        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        mViewModel.getVoucherListData()
    }

    private fun setRecycler() {
        with(binding.rvActive) {
            adapter = mAdapter
        }


    }

    private fun updateUi() {
        val voucherCount = mAdapter.itemCount
        if (voucherCount == 0) {
            binding.rvActive.gone()
            binding.lytNoTransaction.root.visible()
            binding.lytNoTransaction.textViewTitle.text =
                getString(R.string.text_you_have_not_bought_voucher)
            binding.lytNoTransaction.textViewMsg.text =
                getString(R.string.text_complete_task_and_earn)
            binding.lytNoTransaction.imageViewNoChallenge.setImageResource(R.drawable.ic_empty_voucher)
            binding.tvGetVoucher.text = getString(R.string.text_get_vouchers)
            binding.ivVoucherMore.gone()
            sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_VOUCHER_PAGE_NO_PURCHASE)
        } else {
            binding.rvActive.visible()
            binding.lytNoTransaction.root.gone()
            binding.tvGetVoucher.text = getString(R.string.text_get_more)
            binding.ivVoucherMore.visible()
            sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_VOUCHER_PAGE_ACTIVE_VOUCHER_PAGE_VISIT)
        }
    }

    override fun initListener() {
        binding.tvGetVoucher.setOnClickListener {
            navigate(R.id.allDealsFragment)
        }
    }

    override fun subscribeObservers() {
        mViewModel.voucherList.observe(this) {
            mAdapter.setDataSet(it)
            mSharedViewModel.actVoucherCount.value = it.size
            updateUi()
            mSharedViewModel.setSelected()
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        mViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }


}