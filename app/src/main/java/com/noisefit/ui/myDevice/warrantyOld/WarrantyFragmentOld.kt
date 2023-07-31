package com.noisefit.ui.myDevice.warrantyOld

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentWarrantyOldBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WarrantyFragmentOld : BaseFragment<FragmentWarrantyOldBinding>(FragmentWarrantyOldBinding::inflate) {

    @Inject
    lateinit var sessionManager: com.noisefit.session.SessionManager

    private val viewModel: WarrantyViewModelOld by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_warranty_registration)

        sessionManager.connectedDevice.value?.let {
            binding.etDeviceName.setText(it.bluetoothName)
        }



    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.bSubmit.setOnClickListener {
            uiController.hideSoftKeyboard()
            viewModel.addWarranty(
                binding.tvMarkets.text.toString(),
                binding.etSerialNo.text.toString()
            )
        }
        binding.textSerialNumber.setOnClickListener {
            uiController.hideSoftKeyboard()
            showSerialNoDialog()
        }
        binding.tvMarkets.setOnClickListener {
            setFragmentResultListener(MARKET_REQUEST_KEY) { key, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let {
                    binding.tvMarkets.text = it
                }
            }
            viewModel.marketPlaces.value?.let {
                navigate(
                    WarrantyFragmentOldDirections.actionWarrantyFragmentToBottomSheetMarketSelector(
                        it.toTypedArray(),
                        binding.tvMarkets.text.toString()
                    )
                )
            }
        }

        binding.etSerialNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    binding.bSubmit.isEnabled = false
                } else {
                    binding.bSubmit.isEnabled = s.length > 4
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ivBarCode.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { key, bundle ->
                val scanResult = bundle.getString("scanResult")

                binding.etSerialNo.setText(scanResult)
            }
            navigate(
                WarrantyFragmentOldDirections.actionWarrantyFragmentToQrCodeScanFragment()
            )

        }
    }

    override fun subscribeObservers() {
        viewModel.marketPlaces.observe(viewLifecycleOwner) {
            val firstItem = it.firstOrNull()
            firstItem?.let { item ->
                binding.tvMarkets.text = item
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.warrantyRegistered.observe(viewLifecycleOwner) {



            context.showShortToast(getString(R.string.text_warranty_registered))
            navigateUpSafe()
        }
        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }

    fun showSerialNoDialog() {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.dialog_serial_no, null, false)
        builder.setView(view)
        val alertDialog = builder.create()
        view.findViewById<ImageView>(R.id.ivCloseDialog).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()

    }
}