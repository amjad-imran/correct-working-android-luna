package com.noisefit.ui.watchface.custom

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.model.CustomGrid
import com.noisefit_commans.data.model.WatchFaceWidgets
import com.noisefit.luna.databinding.FragmentNfhCustomWatchFaceBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.profile.BottomSheetImagePicker
import com.noisefit.ui.watchface.REQUEST_LAUNCH_IMAGE_CAPTURE
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit.ui.watchface.WatchFaceProgressBottomDialog
import com.noisefit.ui.watchface.custom.BottomSheetWidgetSelection.Companion.WIDGET_PICKER_RESULT
import com.noisefit.util.*
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.ScreenUtils
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class NfhCustomWatchFaceFragment :
    BaseFragment<FragmentNfhCustomWatchFaceBinding>(FragmentNfhCustomWatchFaceBinding::inflate),
    GridOptionSelectAction {

    private var outputUri: Uri? = null
    private var photoURI: Uri? = null

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var screenUtils: ScreenUtils

    @Inject
    lateinit var imageUtil: ImageUtil

    @Inject
    lateinit var localDataStore: DataStoredInterface

    private val viewModel: NfhCustomWatchFaceViewModel by viewModels()

    private val adapter: GridOptionsAdapter by lazy {
        GridOptionsAdapter(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setAdapter()

    }

    private fun setAdapter() {
        binding.rvGridOptions.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvGridOptions.adapter = adapter

        adapter.setDataSet(viewModel.getGridOptions())
    }

    override fun initListener() {

        binding.bBack.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivColorPicker.setOnClickListener {
            showColorPickerDialog()
        }
        binding.ivWallpaper.setOnClickListener {
            navigate(NfhCustomWatchFaceFragmentDirections.actionNfhCustomWatchFaceFragmentToBottomSheetImagePicker())
        }
        binding.bReset.setOnClickListener {
            viewModel.reset()
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY)
            binding.ivMain.setImageDrawable(BitmapDrawable(resources, bitmap))
        }
        binding.ivMain.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x
                val y = event.y
                viewModel.onImageClicked(
                    screenUtils.pxToDp(x.toInt(), requireContext()).toFloat(),
                    screenUtils.pxToDp(y.toInt(), requireContext()).toFloat()
                )
            }
            true
        }

        binding.bSave.setOnClickListener {

            viewModel.generateWatchFaceData()?.let { data ->
                sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetWatchFaceCustomHybrid(
                        data
                    )
                )
                showProgressDialog(
                    getString(R.string.text_updating_your_watchface),
                    getString(R.string.text_updating_wf),
                    "Uploading…",
                    null,
                    null
                )
                progressBottomSheet?.setProgress(0)

            }


        }
    }

    override fun subscribeObservers() {

        setFragmentResultListener(BottomSheetImagePicker.IMAGE_PICKER_RESULT) { key, bundle ->
            val value = bundle.getInt("selectedValue")
            if (value == 0) {
                val libraryIntent = Intent(Intent.ACTION_PICK)
                libraryIntent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(libraryIntent, null),
                    REQUEST_LAUNCH_LIBRARY
                )
            } else {
                checkCameraPermission {
                    dispatchTakePictureIntent()
                }
            }
        }

        viewModel.pickWidget.observe(viewLifecycleOwner) {
            it.getContent()?.let { gridType ->
                navigate(
                    NfhCustomWatchFaceFragmentDirections
                        .actionNfhCustomWatchFaceFragmentToBottomSheetWidgetSelection(
                            gridType,
                            viewModel.addedViews[viewModel.selectedPosition].color,
                            viewModel.addedViews[viewModel.selectedPosition].widget
                        )
                )
            }
        }

        setFragmentResultListener(WIDGET_PICKER_RESULT) { key, bundle ->
            val selectedWidget = bundle.getParcelable("selectedWidget") as? WatchFaceWidgets
            var selectedColor = bundle.getString("selectedColor")


            if (selectedColor == null) {
                selectedColor = "#ffffff"
            }

            drawBitmap(selectedColor, selectedWidget)


        }

        sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) { event ->
            event.getContent()?.let {
                if (it is UpdateDeviceDataCallback.CustomizeWatchFaceProgress) {

                    updateWatchFaceStatus(it.watchUpdateStatus)

                }
            }
        }

        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }

    private fun drawBitmap(selectedColor: String, selectedWidget: WatchFaceWidgets?) {
        val watchfaceView = viewModel.addedViews[viewModel.selectedPosition]
        watchfaceView.color = selectedColor
        watchfaceView.widget = selectedWidget


        if (selectedWidget != null) {
            if (selectedWidget.type == -1) return

            val width = watchfaceView.rect.width().toInt()
            val height = watchfaceView.rect.height().toInt()
            val bitmapImage = imageUtil.getBitmapFromAsset(
                requireContext(),
                viewModel.getWidgetIconName(selectedWidget)
            )
                ?: return
            val bitmapNew2 = imageUtil.resize(bitmapImage, height, width)

            val bitmapHeight = bitmapNew2.height
            val bitmapWidth = bitmapNew2.width

            val imageTop = watchfaceView.x + height / 2 - bitmapHeight / 2
            val imageLeft = watchfaceView.y + width / 2 - bitmapWidth / 2

            val clearPaint = Paint()
            clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawRect(watchfaceView.rect, clearPaint)

            canvas.drawRect(watchfaceView.rect, paint)

            canvas.drawBitmap(
                bitmapNew2, imageLeft,
                imageTop, Paint().apply {
                    colorFilter = PorterDuffColorFilter(
                        Color.parseColor(selectedColor),
                        PorterDuff.Mode.SRC_IN
                    )
                }
            )
            //canvas.drawRect(viewModel.addedViews[viewModel.selectedPosition].rect, paint)
            binding.ivMain.setImageDrawable(BitmapDrawable(resources, bitmap))
        }
    }

    private fun updateWatchFaceStatus(watchUpdateStatus: WatchUpdateStatus) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED -> {
                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }
            UpdateStatus.PROGRESS -> {
                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }
            UpdateStatus.COMPLETED -> {
                binding.bSave.visible()
                progressBottomSheet?.dismiss()
                context.showShortToast("Watch Face Transferred")

                navigateUpSafe()
                sessionManager.transferInProgress = false
                //sessionManager.showReview.postValue(Event(true))
                viewModel.earnRewardsPoints()

                val count = localDataStore.getCustomWatchFaceTransferCount()
                val newCount = count + 1
                localDataStore.setCustomWatchFaceTransferCount(newCount)
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.CUSTOM_WATCHFACE_UPDATE_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_update_status"] = true
                    })
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            "custom_watchface_" + viewModel.watchfaceSelectedPos
                        this["watch_face_update_status"] = true
                        this["detail"] = viewModel.watchfaceSelectedPos.toString() + "\n" +
                                "custom_watchface_" + viewModel.watchfaceSelectedPos + "\n" +
                                UpdateStatus.COMPLETED.name
                    }
                )

            }
            UpdateStatus.ERROR -> {
                binding.bSave.visible()
                progressBottomSheet?.dismiss()
                context.showShortToast("Failed")
                sessionManager.transferInProgress = false
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.CUSTOM_WATCHFACE_UPDATE_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_update_status"] = false
                    })
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            "custom_watchface_" + viewModel.watchfaceSelectedPos
                        this["watch_face_update_status"] = false
                        this["detail"] = viewModel.watchfaceSelectedPos.toString() + "\n" +
                                "custom_watchface_" + viewModel.watchfaceSelectedPos + "\n" +
                                UpdateStatus.ERROR.name

                    }
                )
            }
            else -> {}
        }
    }


    private fun checkCameraPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            cameraPermissionResult.launch(Manifest.permission.CAMERA)
        }
    }

    private val cameraPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            dispatchTakePictureIntent()
        } else {
            context.showShortToast("Permission Required")
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        requireContext(), CommonConstants.FILE_PROVIDER, it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_LAUNCH_IMAGE_CAPTURE)
                }
            }
        }
    }

    lateinit var currentPhotoPath: String
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PNG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {

            currentPhotoPath = absolutePath
        }
    }

    private fun showColorPickerDialog() {
        ColorPickerDialog.Builder(context, R.style.AlertDialogTheme)
            .setTitle("Select Background Color")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton("Select",
                ColorEnvelopeListener { envelope, fromUser ->
                    viewModel.backgroundColor = "#${envelope.hexCode}"
                    viewModel.imagePath = null
                    binding.ivBackground.setImageURI(null)
                    binding.ivBackground.setBackgroundColor(envelope.color)
                })
            .setNegativeButton(
                getString(R.string.text_cancel)
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(false) // the default value is true.
            .attachBrightnessSlideBar(true) // the default value is true.
            .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
            .show()
    }

    private fun getDimension(): Pair<Int, Int> {
        return Pair(320, 320)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val dimension = getDimension()
            when (requestCode) {

                REQUEST_LAUNCH_IMAGE_CAPTURE -> {
                    if (photoURI == null) return
                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(photoURI!!, outputUri!!)
                        .withAspectRatio(1f, 1f)
                        .withOptions(uCropOptions)
                        .withMaxResultSize(dimension.first, dimension.second)
                        .start(requireActivity(), this)
                }
                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return

                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(selectedImageUri, outputUri!!)
                        .withAspectRatio(1f, 1f)
                        .withOptions(uCropOptions)
                        .withMaxResultSize(dimension.first, dimension.second)
                        .start(requireActivity(), this)
                }
                UCrop.REQUEST_CROP -> {
                    val resultUri = data?.let { UCrop.getOutput(it) }

                    //viewModel.localImage.value = resultUri
                    resultUri?.let {
                        viewModel.backgroundColor = null
                        binding.ivBackground.setImageURI(null)
                        binding.ivBackground.setImageURI(resultUri)
                        viewModel.imagePath = it.toString()
                    }
                    val selectedImagePath = resultUri.toString()
                }
            }
        }
    }

    val bitmap = Bitmap.createBitmap(270, 270, Bitmap.Config.ARGB_8888)

    val paint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#33000000")
        isAntiAlias = false
        strokeWidth = 5f
    }
    val canvas = Canvas(bitmap)


    override fun onGridSelected(gridData: CustomGrid, position: Int) {
        viewModel.watchfaceSelectedPos = position
        viewModel.getView(gridData.gridType)?.let {
            canvas.drawRect(it, paint)
            binding.ivMain.setImageDrawable(BitmapDrawable(resources, bitmap))
        } ?: context.showShortToast("Not enough space")
    }

    private var progressBottomSheet: WatchFaceProgressBottomDialog? = null

    private fun showProgressDialog(
        title: String, message: String, typeText: String, watchfaceImageUrl: String?,
        watchFaceName: String?
    ) {
        progressBottomSheet = WatchFaceProgressBottomDialog.getInstance(
            title, message, typeText, watchfaceImageUrl,
            watchFaceName
        )
        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager,
            "DownloadBottomSheet"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBottomSheet?.dismissAllowingStateLoss()
    }

}