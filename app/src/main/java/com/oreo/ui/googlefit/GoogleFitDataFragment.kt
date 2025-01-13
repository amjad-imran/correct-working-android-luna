package com.oreo.ui.googlefit

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentGoogleFitDataBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleFitDataFragment :
    BaseFragment<FragmentGoogleFitDataBinding>(FragmentGoogleFitDataBinding::inflate) {

    private val viewModel: GoogleFitDataViewModel by viewModels()
    private val mAdapter: GoogleFitDataAdapter by lazy {
        GoogleFitDataAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_select_data)

        binding.rvMain.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMain.adapter = mAdapter

        viewModel.loadData()
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnApprove.setOnClickListener {
            checkAndSendData()

        }

        binding.lytDataSyncStatus.btnRetry.setOnClickListener {
            binding.lytDataSyncStatus.root.gone()
            checkAndSendData()
        }

        binding.lytDataSyncStatus.lytToolbarSync.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    private fun checkAndSendData() {
        val selectedItems = mAdapter.getSelectedData()

        context.showShortToast("Selected Items ${selectedItems.size}")
        if (selectedItems.isNotEmpty()) {
            viewModel.sendDataToServer(selectedItems)
        }
    }

    override fun subscribeObservers() {

        viewModel.unSyncedDataList.observe(this) {
            mAdapter.setDataSet(it)
        }

        viewModel.dataSyncingComplete.observe(this) {
            it.getContent()?.let {
                //todo remove data from adapter
                mAdapter.removeSyncedData(viewModel.success)

                //check remaining data size
                val itemCount = mAdapter.itemCount

                //if greater than 1 show error
                if (itemCount > 0) {
                    showSyncCompleteState(false, "Data sync failed", "")
                } else {
                    val syncMessage = viewModel.getSyncedMessage()
                    showSyncCompleteState(
                        true,
                        "${viewModel.success.size} Items synced",
                        syncMessage
                    )
                }
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

    }

    private fun showSyncCompleteState(state: Boolean, title: String, message: String) {
        binding.lytDataSyncStatus.apply {
            tvSyncMessage.text = title
            if (state) {
                tvMessage.text = message
                tvMessage.visible()
                image.setImageResource(R.drawable.ic_g_fit_success)
            } else {
                tvMessage.gone()
                image.setImageResource(R.drawable.ic_g_fit_failed)
            }
            root.visible()
            btnRetry.setOnClickListener {
                binding.lytDataSyncStatus.root.gone()

            }
        }
    }

}