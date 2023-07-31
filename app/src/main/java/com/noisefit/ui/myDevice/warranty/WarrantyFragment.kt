package com.noisefit.ui.myDevice.warranty

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentWarrantyBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WarrantyFragment : BaseFragment<FragmentWarrantyBinding>(FragmentWarrantyBinding::inflate) {

    @Inject
    lateinit var sessionManager: com.noisefit.session.SessionManager

    private val viewModel: WarrantyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getInitialData()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.text_warranty_registration)

        //viewModel.checkWarranty()
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.bSubmit.setOnClickListener {
            uiController.hideSoftKeyboard()
            val marketPlaceId = viewModel.getMarketPlaceId(binding.tvMarkets.text.toString().trim())
            val deviceId = viewModel.getDeviceId(binding.tvDeviceName.text.toString().trim())
            val orderNo = binding.etOrderNo.text.toString().trim()

            if (marketPlaceId == -1 || deviceId == -1) {
                context.showShortToast(getString(R.string.text_something_went_wrong))
                return@setOnClickListener
            }

            if (orderNo.isEmpty()) {
                context.showShortToast(getString(R.string.text_enter_order_number))
                return@setOnClickListener
            }


            viewModel.addWarranty(
                marketPlaceId,
                deviceId,
                binding.etSerialNo.text.toString().trim(),
                orderNo
            )
        }
        binding.textSerialNumber.setOnClickListener {
            uiController.hideSoftKeyboard()
            showSerialNoDialog()
        }
        binding.tvMarkets.setOnClickListener {

            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { value ->
                    binding.tvMarkets.text = value
                }
            }

            val marketList =viewModel.getMarketNameList()
            if(marketList.isNotEmpty()){
                navigate(
                    WarrantyFragmentDirections.actionWarrantyFragmentToValueSelectorBottomSheet(
                        binding.tvMarkets.text.toString(),
                        marketList.toTypedArray(),
                        "Select Marketplace"
                    )
                )
            }else{
                viewModel.getMarketPlaces()
            }
        }

        binding.tvDeviceName.setOnClickListener {

            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { value ->
                    binding.tvDeviceName.text = value
                }
            }

            val deviceList = viewModel.getDeviceNameList()

            if (deviceList.isNotEmpty()) {
                navigate(
                    WarrantyFragmentDirections.actionWarrantyFragmentToValueSelectorBottomSheet(
                        binding.tvDeviceName.text.toString(),
                        deviceList.toTypedArray(),
                        "Select Watch"
                    )
                )
            } else {
                viewModel.getWarrantyWatchList()
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
                WarrantyFragmentDirections.actionWarrantyFragmentToQrCodeScanFragment()
            )

        }
    }

    override fun subscribeObservers() {
        viewModel.marketPlaces.observe(viewLifecycleOwner) {
            val firstItem = it.firstOrNull()
            firstItem?.let { item ->
                binding.tvMarkets.text = item.name
            }
        }

        viewModel.warrantyWatches.observe(viewLifecycleOwner) {
            val firstItem = it.firstOrNull()
            firstItem?.let { item ->
                binding.tvDeviceName.text = item.product_name
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

        /*viewModel.isWarrantyAlreadyRegistered.observe(this) {
            if (it) {
                val alertMessage = "Warranty already registered"
                uiController.onApiErrorReceived(
                    ErrorResponse(
                        UIComponentType.InfoAlertDialog(
                            "Info", alertMessage, getString(R.string.text_got_it)
                        ).apply {
                            callback = object : SingleActionCallback {
                                override fun onClicked() {
                                    findNavController().navigateUp()
                                }
                            }
                        }
                    )
                )
            }
        }*/
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