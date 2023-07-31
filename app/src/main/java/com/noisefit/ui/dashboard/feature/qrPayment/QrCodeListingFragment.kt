package com.noisefit.ui.dashboard.feature.qrPayment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.noisefit.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.AppStaticData
import com.noisefit.databinding.FragmentWorldClockBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit.ui.dashboard.feature.qrPayment.bottomsheets.*
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.UPIQRCode
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


const val REQUEST_LAUNCH_LIBRARY = 384

@AndroidEntryPoint
class QrCodeListingFragment :
    BaseFragment<FragmentWorldClockBinding>(FragmentWorldClockBinding::inflate),
    QuickReplyRowAction {

    private val viewModel: QrCodeListingViewModel by viewModels()
    lateinit var touchHelper: ItemTouchHelper
    private val adapter by lazy { QrCodeListingAdapter(this) }

    @Inject
    lateinit var localDataStore: DataStoredInterface

    var isEditAllowed = true


    private val maxQuickReplies: Int by lazy {
        AppStaticData.getMaxQrCodes(/*localDataStore.getConnectedDevice()*/)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textAddClock.text = getString(R.string.text_add_to_add_qr, maxQuickReplies)
        binding.textView8.text = getString(R.string.text_qr_code)
        binding.textView18.text = getString(R.string.text_qr_code)
        binding.textView18.invisible()
        binding.textNoClock.text = getString(R.string.text_no_qr_codes_added)

        binding.btnAddClockBottom.text = getString(R.string.text_add_qr)

        binding.ivNoClock.setImageResource(R.drawable.ic_qr_code_white)

        setRecycler()
        if (viewModel.qrCodes == null) {
            binding.progressBar.root.visible()
            viewModel.sessionManager.sendQueryAction(QueryAction.GetUPIQRCode)
        }

        if (viewModel.shouldShowInfo()) {
            viewModel.localDataStore.setQrCodeInfoStatus(true)
            navigate(R.id.bottomSheetQrInfo)
        }
    }


    override fun initListener() {

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAddClockBottom.setOnClickListener {
            viewModel.updatePosition = -1
            invokeAddQrCode()
        }
        binding.tvEdit.setOnClickListener {
            setStateEdit()
            //viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.QUICK_REPLY_EDIT_CLICK)
        }
        binding.btnAdd.setOnClickListener {
            viewModel.updatePosition = -1
            invokeAddQrCode()
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.setEditMode(false)
            if (adapter.itemCount == 0) {
                return@setOnClickListener
            }
            binding.progressBar.root.visible()
            val qrCodes = adapter.getCurrentDataSet()
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetUPIQRCode(
                    qrCodes
                )
            )
        }

    }

    override fun subscribeObservers() {
        viewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.UPIQRCodeObtained -> {
                    binding.progressBar.root.gone()

                    it.uPIQRCodes?.forEach { reply ->
                        LOGS.d("qr_code : ${reply.id}  ${reply.title} ${reply.url}")
                    }

                    if (it.uPIQRCodes.isNullOrEmpty()) {
                        binding.textView17.text =
                            getString(R.string.text_add_qr_code_info)
                        setStateNoData()
                    } else {
                        binding.textView17.text =
                            getString(R.string.text_add_qr_code_info)
                        setStateHasData()
                    }
                    if (it.uPIQRCodes == null) {
                        viewModel.qrCodes = ArrayList()
                    } else {
                        it.uPIQRCodes?.let { it1 -> setQrCodesList(it1) }
                    }
                }

                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) { event ->
            event.getContent()?.let {
                when (it) {
                    is UpdateDeviceDataCallback.UPIQRCodeUpdated -> {
                        binding.progressBar.root.gone()
                        if (it.success) {
                            context.showShortToast(getString(R.string.text_updated_successfully))
                            viewModel.sessionManager.sendQueryAction(QueryAction.GetUPIQRCode)
                        } else {
                            context.showShortToast(getString(R.string.text_updated_failed))
                            viewModel.sessionManager.sendQueryAction(QueryAction.GetUPIQRCode)
                        }
                    }
                    is UpdateDeviceDataCallback.ClearUPIQRCodeUpdated -> {
                        binding.progressBar.root.gone()
                        if (it.success) {
                            if (viewModel.removePosition != -1) {
                                adapter.removeItem(viewModel.removePosition)
                                updateCount()
                                viewModel.removePosition = -1
                            }
                            context.showShortToast(getString(R.string.text_updated_successfully))
                        } else {
                            context.showShortToast(getString(R.string.text_updated_failed))
                        }
                    }
                    else -> {
                        LOGS.d("Reply in else")
                    }
                }
            }

        }

        viewModel.editMode.observe(viewLifecycleOwner) {
            if (it) {
                setStateEdit()
            } else {
                setStateDefault()
            }
        }

    }

    private fun setRecycler() {
        binding.rvClocks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClocks.adapter = adapter

        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.rvClocks)

    }

    fun invokeAddQrCode() {
        if (viewModel.qrCodes == null) return
        if (viewModel.qrCodes!!.size >= maxQuickReplies) {
            val alertMessage = getString(R.string.text_max_qr_message, maxQuickReplies)
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.InfoAlertDialog(
                        getString(
                            R.string.text_max_reply_reached
                        ), alertMessage, getString(R.string.text_close)
                    )
                )
            )
            return
        }
        viewModel.setEditMode(true)


        setFragmentResultListener(QR_UPLOAD_VIA) { key, bundle ->
            val mode = bundle.get("uploadMode") as UploadMode
            when (mode) {
                UploadMode.GALLERY -> {
                    val libraryIntent = Intent(Intent.ACTION_PICK)
                    libraryIntent.type = "image/*"
                    startActivityForResult(
                        Intent.createChooser(libraryIntent, null), REQUEST_LAUNCH_LIBRARY
                    )
                }
                UploadMode.LINK -> {
                    addQrViaLink()
                }
            }
        }
        navigate(
            QrCodeListingFragmentDirections.actionQrCodeListingFragmentToBottomSheetUploadQrVia()
        )
    }

    private fun addQrViaLink() {
        setFragmentResultListener(QR_UPLOAD_VIA_LINK) { key, bundle ->
            val isBackPressed = bundle.getBoolean("isBackPressed")

            if (isBackPressed) {
                invokeAddQrCode()
            } else {
                val url = bundle.getString("url")
                showAddNameBottomSheet(url = url)
            }
        }
        navigate(
            QrCodeListingFragmentDirections.actionQrCodeListingFragmentToBottomSheetAddLink()
        )
    }

    private fun showAddNameBottomSheet(qrCode: UPIQRCode? = null, url: String? = null) {
        setFragmentResultListener(QR_UPLOAD_NAME) { key, bundle ->
            val isBackPressed = bundle.getBoolean("isBackPressed")

            if (isBackPressed) {
                if (viewModel.updatePosition == -1) {
                    invokeAddQrCode()
                }
            } else {
                val qrCodeObj = bundle.getParcelable<UPIQRCode>("qrCodeObj")

                if (viewModel.qrCodes == null) {
                    viewModel.qrCodes = ArrayList()
                }

                qrCodeObj?.let {

                    if (qrCode != null) {
                        qrCodeObj.url = qrCode.url
                        qrCodeObj.id = qrCode.id
                    }

                    if (viewModel.updatePosition != -1) {
                        viewModel.qrCodes?.set(viewModel.updatePosition, it)
                        viewModel.updatePosition = -1
                    } else {
                        viewModel.qrCodes!!.add(it)
                    }
                    viewModel.qrCodes?.let { codes ->
                        adapter.setDataSet(codes)
                        updateCount()
                    }
                }

            }
        }

        val urlToSend = url ?: qrCode?.url ?: ""
        navigate(
            QrCodeListingFragmentDirections.actionQrCodeListingFragmentToBottomSheetAddName(
                UPIQRCode(id = null, title = qrCode?.title, url = urlToSend)
            )
        )
    }


    override fun onLongPressed(position: Int) {
        setStateEdit()
    }

    override fun onItemRemoved(position: Int, qrCode: UPIQRCode) {
        viewModel.removePosition = position
        binding.progressBar.root.visible()
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.ClearUPIQRCode(qrCode.id ?: 0)
        )
    }

    override fun onItemClicked(position: Int, qrCode: UPIQRCode) {
        viewModel.updatePosition = position

        setFragmentResultListener(QR_EDIT) { key, bundle ->
            val action = bundle.get("action") as EditQrActions

            when (action) {
                EditQrActions.REMOVE -> {
                    viewModel.updatePosition = -1
                    onItemRemoved(position, qrCode)
                }
                EditQrActions.REPLACE -> {
                    invokeAddQrCode()
                }
                EditQrActions.EDIT_NAME -> {
                    showAddNameBottomSheet(qrCode = qrCode)
                }
            }
        }
        navigate(
            QrCodeListingFragmentDirections.actionQrCodeListingFragmentToBottomSheetEditQr(
                qrCode
            )
        )
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    private fun setStateEdit() {
        binding.tvEdit.gone()
        binding.btnSaveChanges.visible()
        binding.btnAddClockBottom.gone()
        binding.btnAdd.visible()
        adapter.setEditMode(true)
    }

    private fun setStateDefault() {
        if (isEditAllowed) {
            if (adapter.itemCount != 0) {
                binding.tvEdit.visible()
            }
        }

        binding.btnSaveChanges.gone()

        adapter.setEditMode(false)
    }

    private fun setStateNoData() {
        binding.ivNoClock.visible()
        binding.textNoClock.visible()
        binding.textAddClock.visible()
        if (viewModel.editMode.value != true) {
            binding.btnAddClockBottom.visible()
        }
        binding.btnSaveChanges.gone()
        binding.btnAdd.gone()
        binding.tvEdit.gone()
        binding.textView18.invisible()
    }

    private fun setStateHasData() {
        binding.ivNoClock.gone()
        binding.textNoClock.gone()
        binding.textAddClock.gone()
        binding.textView18.visible()

        binding.btnAddClockBottom.gone()
        if (isEditAllowed) {
            binding.btnAdd.visible()
        }
        if (viewModel.editMode.value != true) {
            binding.tvEdit.visible()
        }

    }


    fun updateCount() {
        val dataCount = adapter.itemCount
        if (dataCount == 0) {
            binding.tvCount.invisible()
        } else {
            binding.tvCount.visible()
        }
        binding.tvCount.text = "($dataCount/$maxQuickReplies)"
        if (dataCount == 0) {
            setStateNoData()
        } else {
            setStateHasData()
        }
    }

    fun setQrCodesList(reply: List<UPIQRCode>) {
        viewModel.qrCodes = reply as ArrayList<UPIQRCode>
        adapter.setDataSet(reply)
        updateCount()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                REQUEST_LAUNCH_LIBRARY -> {
                    data?.data?.let {
                        handleImage(it)
                    }
                        ?: uiController.onDisplayError(getString(R.string.text_something_went_wrong_retrying))

                }

            }
        }
    }

    private fun handleImage(data: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(
            requireActivity().contentResolver, data
        )
        val hmsScans = ScanUtil.decodeWithBitmap(
            requireActivity(), bitmap, HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create()
        )
        if (!hmsScans.isNullOrEmpty() && hmsScans[0] != null && !TextUtils.isEmpty(
                hmsScans[0]!!.getOriginalValue()
            )
        ) {
            showAddNameBottomSheet(url = hmsScans[0].getOriginalValue())
        } else {
            uiController.onDisplayError(getString(R.string.text_invalid_qr))
        }
    }
}