package com.noisefit.ui.reward.transaction

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentTransactionHistoryBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransactionHistoryFragment :
    BaseFragment<FragmentTransactionHistoryBinding>(FragmentTransactionHistoryBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    private val mViewModel: TransactionViewModel by viewModels()

    private val mAdapter: TransactionHistoryAdapter by lazy {
        TransactionHistoryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.REWARD_TRANSACTION_HISTORY_LANDING_PAGE_VISIT)
        mViewModel.getTransactionHistoryData()
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvTransHistory) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_transaction_history_title)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        mViewModel.transHistoryList.observe(this) {
            if (it.isEmpty()) {
                binding.rvTransHistory.gone()
                binding.lytNoTransaction.root.visible()
                binding.lytNoTransaction.textViewTitle.text =
                    getString(R.string.text_no_transaction_found_title)
                binding.lytNoTransaction.textViewMsg.text =
                    getString(R.string.text_no_transaction_found_msg)
                binding.lytNoTransaction.imageViewNoChallenge.setImageResource(R.drawable.ic_empty_voucher)
            } else {
                mAdapter.setDataSet(it)
                binding.rvTransHistory.visible()
                binding.lytNoTransaction.root.gone()

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