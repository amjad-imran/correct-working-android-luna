package com.noisefit.ui.reward.voucher.expired

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentExpiredVoucherBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.reward.voucher.VoucherSharedViewModel
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExpiredVoucherFragment :
    BaseFragment<FragmentExpiredVoucherBinding>(FragmentExpiredVoucherBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    private val mViewModel: ExpiredVoucherViewModel by viewModels()
    private val mSharedViewModel: VoucherSharedViewModel by activityViewModels()
    private val mAdapter: ExpiredVoucherAdapter by lazy {
        ExpiredVoucherAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_VOUCHER_PAGE_EXPIRED_VOUCHER_PAGE_VISIT)
        setRecycler()
        mViewModel.getVoucherListData()
    }

    private fun setRecycler() {
        with(binding.rvExpired) {
            adapter = mAdapter
        }

    }

    private fun updateUi() {
        val expiredVcCount = mAdapter.itemCount
        if (expiredVcCount == 0) {
            binding.rvExpired.gone()
            binding.lytNoTransaction.root.visible()

            if (mSharedViewModel.actVoucherCount.value == 0) {
                binding.lytNoTransaction.textViewTitle.text =
                    getString(R.string.text_you_have_not_bought_voucher)
                binding.lytNoTransaction.textViewMsg.text =
                    getString(R.string.text_complete_task_and_earn)
                binding.lytNoTransaction.imageViewNoChallenge.visible()
                binding.lytNoTransaction.imageViewNoChallenge.setImageResource(R.drawable.ic_empty_voucher)
            } else {
                binding.lytNoTransaction.textViewTitle.text =
                   getString(R.string.text_coupon_not_expired)
                binding.lytNoTransaction.textViewMsg.text =
                    getString(R.string.text_coupon_hurry)
                binding.lytNoTransaction.imageViewNoChallenge.gone()
            }

        } else {
            binding.rvExpired.visible()
            binding.lytNoTransaction.root.gone()
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        mViewModel.voucherList.observe(this) {
            mAdapter.setDataSet(it)
            mSharedViewModel.expVoucherCount.value = it.size
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